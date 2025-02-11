import torch
from transformers import AutoTokenizer, AutoModelForCausalLM
from .base_handler import LLMHandler
import re
import json

class LlamaHFHandler(LLMHandler):
    def __init__(self, model_name="TinyLlama/TinyLlama-1.1B-Chat-v1.0"):
        try:
            # Load tokenizer
            self.tokenizer = AutoTokenizer.from_pretrained(
                model_name,
                token= "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"
            )
            self.tokenizer.pad_token = self.tokenizer.eos_token
            
            # Configure model loading with memory optimizations
            model_config = {
                "token": "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",
                "torch_dtype": torch.float16,  # Use half precision
                "load_in_8bit": True,  # Load in 8-bit precision
                "device_map": "auto",  # Automatically handle device placement
            }
            
            print("Loading model with 8-bit precision...")
            self.model = AutoModelForCausalLM.from_pretrained(
                model_name,
                **model_config
            )
            
            # Clear CUDA cache
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
                print(f"GPU Memory allocated: {torch.cuda.memory_allocated() / 1024**2:.2f} MB")
                print(f"GPU Memory cached: {torch.cuda.memory_reserved() / 1024**2:.2f} MB")
            
            self.device = "cuda" if torch.cuda.is_available() else "cpu"
            print(f"Using device: {self.device}")
            
        except Exception as e:
            print(f"Error initializing Llama model: {e}")
            raise

    def detect_smells(self, code: str) -> dict:
        prompt = f"""[INST] Design Smell Detection Prompt

You are an expert software architect and code reviewer with years of experience in detecting software design issues. Your task is to analyze the following code and identify design smells and anti-patterns that may impact maintainability, scalability, or readability.

🔍 What to focus on:

    Code structure: Look for violations of SOLID principles, unnecessary complexity, or lack of modularity.
    Design smells: Identify issues such as God objects, tight coupling, poor abstraction, duplicated logic, unnecessary dependencies, or low cohesion.
    Patterns & Best Practices: Highlight deviations from well-established design principles (e.g., improper use of design patterns, violation of encapsulation).

📌 Response Format:
Return a structured JSON object with the following details for each detected issue:

    "smell_name": The name of the design smell (e.g., "God Object", "Feature Envy").
    "description": A clear explanation of why it is a problem.
    "location": Where the issue occurs in the code (e.g., method name, class name).
    "suggested_fix": A brief suggestion on how to improve the design.

Here is the code for review:
{code}
[/INST]"""

        try:
            response = self._generate(prompt)
            return self._parse_response(response)
        except Exception as e:
            print(f"Error in smell detection: {e}")
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        prompt = f"""[INST] 
Refactor Prompt

    You are a code refactoring assistant specializing in improving software design by addressing structural issues. Your task is to refactor the given code to eliminate the following design smell:

    ❌ Problem: {smell_description}

    🔧 Refactoring Goals:

        Improve maintainability, readability, and modularity.
        Apply appropriate design patterns or SOLID principles where relevant.
        Ensure the refactored code remains functionally equivalent to the original.

    🚀 Your response should contain only the improved version of the code, with no additional explanation.

    Here is the original code:{code}
    [/INST] 
    """

        try:
            response = self._generate(prompt)
            print(response)
            return self._extract_code(response)
        except Exception as e:
            print(f"Error in refactoring: {e}")
            return code

    def _generate(self, prompt: str) -> str:
        try:
            # Clear cache before generation
            if torch.cuda.is_available():
                torch.cuda.empty_cache()

            
            inputs = self.tokenizer(
                prompt,
                return_tensors="pt",
                truncation=True,
                max_length=1024  # Limit input length
            )
            
            # Move inputs to device
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            with torch.no_grad():
                outputs = self.model.generate(
                    **inputs,
                    max_length=4096*2,  # Limit output length
                    temperature=0.7,
                    top_p=0.95,
                    do_sample=True,
                    pad_token_id=self.tokenizer.pad_token_id,
                    num_return_sequences=1,
                    use_cache=True
                )
            
            response = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
            
            # Clear cache after generation
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
                
            return response.replace(prompt, "").strip()
        except Exception as e:
            print(f"Error in generation: {e}")
            return ""

    def _parse_response(self, text: str) -> dict:
        """Parse the response text into a structured format."""
        try:
            # Clean up the text to extract just the JSON part
            text = text.strip()
            # Remove any markdown formatting
            text = re.sub(r'```json\s*', '', text)
            text = re.sub(r'```\s*', '', text)
            
            # Find JSON object
            match = re.search(r'\{.*\}', text, re.DOTALL)
            if match:
                json_str = match.group(0)
                # Parse JSON
                try:
                    return json.loads(json_str)
                except json.JSONDecodeError:
                    print(f"Invalid JSON: {json_str}")
                    return {"smells": []}
            return {"smells": []}
        except Exception as e:
            print(f"Error parsing response: {e}")
            return {"smells": []}

    def _extract_code(self, text: str) -> str:
        """Extract code from the response text."""
        # Remove any markdown code blocks if present
        text = re.sub(r'```\w*\n', '', text)
        text = text.replace('```', '')
        return text.strip()

    def __del__(self):
        """Cleanup when the handler is destroyed"""
        try:
            if hasattr(torch, 'cuda') and torch.cuda.is_available():
                torch.cuda.empty_cache()
        except Exception as e:
            print(f"Error during CUDA cleanup: {e}")