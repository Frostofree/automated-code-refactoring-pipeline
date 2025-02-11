import os
import difflib
import json
import time
import requests
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM
from github import Github, InputGitTreeElement
from dotenv import load_dotenv
import re

# Load environment variables
load_dotenv()

import functools

def log_llm_interaction(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        print(f"\n=== LLM CALL: {func.__name__} ===")
        print(f"Input: {kwargs.get('code', '')[:200]}...")
        start_time = time.time()
        result = func(*args, **kwargs)
        duration = time.time() - start_time
        print(f"Duration: {duration:.2f}s")
        print(f"Result: {str(result)[:200]}...")
        return result
    return wrapper

class LLMHandler:
    """Base class for LLM integrations"""
    def detect_smells(self, code: str) -> dict:
        raise NotImplementedError
        
    def refactor(self, code: str, smell_description: str) -> str:
        raise NotImplementedError

    def _parse_response(self, text: str) -> dict:
        """
        Extracts JSON from Markdown-style code blocks and parses it.
        Example input:
        ```json
        {"smells": [{"type": "God Class", "description": "..."}]}
        ```
        """
        try:
            # Extract JSON from Markdown code block
            json_match = re.search(r'```json\s*({.*?})\s*```', text, re.DOTALL)
            if json_match:
                return json.loads(json_match.group(1))
            else:
                # If no Markdown block, assume the entire text is JSON
                return json.loads(text)
        except json.JSONDecodeError:
            print(f"Failed to parse JSON response: {text}")
            return {"smells": []}

class EnhancedLLMHandler(LLMHandler):
    def __init__(self, base_handler: LLMHandler):
        self.base_handler = base_handler
        self.smell_patterns = self._load_smell_patterns()
    
    def _load_smell_patterns(self):
        return {
            "God Class": {
                "detection": {
                    "patterns": [
                        "class with excessive methods or fields",
                        "class handling multiple responsibilities",
                        "class with low cohesion",
                        "class with many instance variables"
                    ],
                    "metrics": {
                        "method_count_threshold": 15,
                        "field_count_threshold": 10,
                        "complexity_threshold": 50
                    }
                },
                "refactoring": [
                    "Extract Class",
                    "Extract Interface",
                    "Move Method",
                    "Extract Subsystem"
                ],
                "severity": "HIGH"
            },
            "Feature Envy": {
                "detection": {
                    "patterns": [
                        "method using more features from another class",
                        "method with many external dependencies",
                        "method accessing foreign data frequently"
                    ],
                    "metrics": {
                        "foreign_method_calls_threshold": 5,
                        "foreign_field_access_threshold": 3
                    }
                },
                "refactoring": [
                    "Move Method",
                    "Extract Method",
                    "Move Field"
                ],
                "severity": "MEDIUM"
            },
            "Data Class": {
                "detection": {
                    "patterns": [
                        "class with only getters and setters",
                        "class without behavior",
                        "class acting as data container"
                    ],
                    "metrics": {
                        "behavior_ratio_threshold": 0.2
                    }
                },
                "refactoring": [
                    "Move Method",
                    "Encapsulate Field",
                    "Add Behavior"
                ],
                "severity": "LOW"
            },
            "Long Method": {
                "detection": {
                    "patterns": [
                        "method with too many lines",
                        "method with multiple responsibilities",
                        "method with high complexity"
                    ],
                    "metrics": {
                        "line_count_threshold": 30,
                        "complexity_threshold": 10
                    }
                },
                "refactoring": [
                    "Extract Method",
                    "Replace Temp with Query",
                    "Introduce Parameter Object"
                ],
                "severity": "MEDIUM"
            },
            "Duplicate Code": {
                "detection": {
                    "patterns": [
                        "identical or similar code blocks",
                        "repeated code patterns",
                        "copy-pasted code segments"
                    ],
                    "metrics": {
                        "similarity_threshold": 0.8,
                        "minimum_lines": 5
                    }
                },
                "refactoring": [
                    "Extract Method",
                    "Pull Up Method",
                    "Form Template Method"
                ],
                "severity": "HIGH"
            },
            "Inappropriate Intimacy": {
                "detection": {
                    "patterns": [
                        "classes with excessive dependencies",
                        "classes accessing private members",
                        "bidirectional associations"
                    ],
                    "metrics": {
                        "dependency_threshold": 7,
                        "private_access_threshold": 3
                    }
                },
                "refactoring": [
                    "Move Method",
                    "Move Field",
                    "Change Bidirectional to Unidirectional"
                ],
                "severity": "MEDIUM"
            },
            "Switch Statements": {
                "detection": {
                    "patterns": [
                        "repeated switch on type code",
                        "conditional logic duplication",
                        "type-based conditionals"
                    ],
                    "metrics": {
                        "case_count_threshold": 3,
                        "occurrence_threshold": 2
                    }
                },
                "refactoring": [
                    "Replace Conditional with Polymorphism",
                    "Replace Type Code with Subclasses",
                    "Replace Type Code with State/Strategy"
                ],
                "severity": "MEDIUM"
            },
            "Primitive Obsession": {
                "detection": {
                    "patterns": [
                        "overuse of primitive types",
                        "using strings for complex data",
                        "primitive-type parameters"
                    ],
                    "metrics": {
                        "primitive_parameter_threshold": 3,
                        "primitive_field_threshold": 5
                    }
                },
                "refactoring": [
                    "Replace Data Value with Object",
                    "Replace Type Code with Class",
                    "Introduce Parameter Object"
                ],
                "severity": "LOW"
            },
            "Large Class": {
                "detection": {
                    "patterns": [
                        "class with too many lines",
                        "class with too many responsibilities",
                        "low cohesion class"
                    ],
                    "metrics": {
                        "line_count_threshold": 300,
                        "method_count_threshold": 20
                    }
                },
                "refactoring": [
                    "Extract Class",
                    "Extract Subclass",
                    "Extract Interface"
                ],
                "severity": "HIGH"
            },
            "Message Chains": {
                "detection": {
                    "patterns": [
                        "long chain of method calls",
                        "navigation through multiple objects",
                        "law of demeter violations"
                    ],
                    "metrics": {
                        "chain_length_threshold": 3
                    }
                },
                "refactoring": [
                    "Hide Delegate",
                    "Extract Method",
                    "Move Method"
                ],
                "severity": "MEDIUM"
            },
            "Refused Bequest": {
                "detection": {
                    "patterns": [
                        "inherited methods not used",
                        "overridden methods throwing exceptions",
                        "empty method implementations"
                    ],
                    "metrics": {
                        "unused_inheritance_threshold": 0.5
                    }
                },
                "refactoring": [
                    "Replace Inheritance with Delegation",
                    "Extract Superclass",
                    "Push Down Method"
                ],
                "severity": "HIGH"
            },
            "Divergent Change": {
                "detection": {
                    "patterns": [
                        "class changed for different reasons",
                        "multiple change triggers",
                        "mixed responsibilities"
                    ],
                    "metrics": {
                        "change_reason_threshold": 2
                    }
                },
                "refactoring": [
                    "Extract Class",
                    "Move Method",
                    "Split Class"
                ],
                "severity": "MEDIUM"
            }
        }

    def detect_smells(self, code: str) -> dict:
        print("EnhancedLLMHandler: Detecting smells...")  # Debugging
        prompt = self._enhance_detection_prompt(code)
        response = self.base_handler.detect_smells(code)
        validated_response = self._validate_smells(response)
        print(f"EnhancedLLMHandler: Detected smells: {validated_response}")  # Debugging
        return validated_response
    
    def _enhance_detection_prompt(self, code: str) -> str:
        return f"""Analyze the following code for software design smells and anti-patterns, focusing strictly on code structure and design. Identify issues related to:

    Class Responsibilities & Coupling - Are classes adhering to the Single Responsibility Principle? Are there signs of excessive dependencies?
    Inheritance & Polymorphism - Is inheritance used appropriately, or are there signs of deep hierarchies, misuse, or unnecessary complexity?
    Encapsulation & Data Leaks - Are internal details properly hidden, or are there public fields/methods exposing too much?
    Modularity & Separation of Concerns - Are components well-structured, or is there high interdependence and lack of cohesion?
    Code Duplication & Redundancy - Are there repeated patterns or logic that should be abstracted or modularized?

    For each identified issue, provide:

    Specific Location (class, method, or line reference)
    Severity Level (Low, Medium, High)
    Impact on Maintainability (How it affects readability, extensibility, or scalability)
    Suggested Refactoring (Pattern or technique to improve design)

Code to Analyze:
{code}"""

    def refactor(self, code: str, smell_description: str) -> str:
        print(f"EnhancedLLMHandler: Refactoring for smell: {smell_description}")  # Debugging
        smell_type = self._identify_smell_type(smell_description)
        if smell_type in self.smell_patterns:
            refactoring_steps = self.smell_patterns[smell_type]["refactoring"]
            print(f"EnhancedLLMHandler: Applying refactoring steps: {refactoring_steps}")  # Debugging
            return self._apply_refactoring_steps(code, smell_type, refactoring_steps)
        return self.base_handler.refactor(code, smell_description)
    
    def _apply_refactoring_steps(self, code: str, smell_type: str, steps: list) -> str:
        prompt = f"""Refactor this code to fix {smell_type} using these steps:
        {', '.join(steps)}

        Consider:
        1. Maintain existing functionality
        2. Preserve encapsulation
        3. Follow SOLID principles
        4. Ensure backward compatibility
        5. Provide meaningful variable and method names
        6. Ensure code readability and maintainability

        Original code:
        {code}"""   
        return self.base_handler.refactor(code, prompt)
    
    def _validate_smells(self, response: dict) -> dict:
        validated_smells = []
        for smell in response.get("smells", []):
            if self._is_valid_smell(smell):
                validated_smells.append(smell)
            else:
                print(f"EnhancedLLMHandler: Invalid smell detected: {smell}")  # Debugging
        return {"smells": validated_smells}
    
    def _is_valid_smell(self, smell: dict) -> bool:
        required_fields = ["type", "description"]
        return all(field in smell and smell[field] for field in required_fields)
    
    def _identify_smell_type(self, description: str) -> str:
        for smell_type, pattern in self.smell_patterns.items():
            if any(keyword in description.lower() for keyword in pattern["detection"]["patterns"]):
                return smell_type
        return "Unknown"

class OpenAIHandler(LLMHandler):
    def __init__(self, model="gpt-4o"):
        self.model = model
        self.api_key = "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"
        if not self.api_key:
            raise ValueError("OPENAI_API_KEY environment variable is not set.")

    def detect_smells(self, code: str) -> dict:
        try:
            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": self.model,
                    "messages": [{
                        "role": "user",
                        "content": f"Analyze this Java code for design smells. Return JSON response:\n{code}"
                    }]
                }
            )
            response.raise_for_status()
            response_data = response.json()
            return self._parse_response(response_data["choices"][0]["message"]["content"])
        except requests.exceptions.RequestException as e:
            print(f"OpenAI API request failed: {e}")
            return {"smells": []}
        except KeyError as e:
            print(f"Unexpected response format: {e}")
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        try:
            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": self.model,
                    "messages": [{
                        "role": "user",
                        "content": f"Refactor this Java code to fix {smell_description}. Return only the refactored code:\n{code}"
                    }]
                }
            )
            response.raise_for_status()
            response_data = response.json()
            return response_data["choices"][0]["message"]["content"]
        except requests.exceptions.RequestException as e:
            print(f"OpenAI API request failed: {e}")
            return code
        except KeyError as e:
            print(f"Unexpected response format: {e}")
            return code

class GeminiHandler(LLMHandler):
    def __init__(self, model="gemini-pro"):
        self.model = model
        self.api_key = "AIzaSyADPQsmGSVDQdmMuaNhLoZx9lJispvpqKw"
        if not self.api_key:
            raise ValueError("GEMINI_API_KEY environment variable is not set.")

    def detect_smells(self, code: str) -> dict:
        try:
            response = requests.post(
                f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent",
                headers={"Content-Type": "application/json"},
                params={"key": self.api_key},
                json={
                    "contents": [{
                        "parts": [{
                            "text": f"You are a code reviewer. Analyze this code for software design smells and patterns. Focus only on code structure and design. Return a JSON response with found issues:\n{code}"
                        }]
                    }],
                    "safetySettings": [
                        {
                            "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_HATE_SPEECH",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_HARASSMENT",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                            "threshold": "BLOCK_NONE"
                        }
                    ]
                }
            )
            response.raise_for_status()
            response_data = response.json()

            print("=============================")
            print(response_data)
            print("=============================")

            
            # Check if response was blocked by safety filters
            if 'candidates' in response_data and response_data['candidates'][0].get('finishReason') == 'SAFETY':
                print("Response was blocked by safety filters. Using empty response.")
                return {"smells": []}
                
            # Check if we have valid content
            if ('candidates' in response_data and 
                len(response_data['candidates']) > 0 and 
                'content' in response_data['candidates'][0] and 
                'parts' in response_data['candidates'][0]['content'] and 
                len(response_data['candidates'][0]['content']['parts']) > 0):
                
                return self._parse_response(response_data['candidates'][0]['content']['parts'][0]['text'])
            else:
                print("Unexpected response structure")
                return {"smells": []}
                
        except requests.exceptions.RequestException as e:
            print(f"Gemini API request failed: {e}")
            return {"smells": []}
        except KeyError as e:
            print(f"Unexpected response format from Gemini: {e}")
            print("Full response:", response_data)
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        try:
            response = requests.post(
                f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent",
                headers={"Content-Type": "application/json"},
                params={"key": self.api_key},
                json={
                    "contents": [{
                        "parts": [{
                            "text": f"You are a code refactoring assistant. Refactor this code to fix the following issue: {smell_description}. Return only the refactored code:\n{code}"
                        }]
                    }],
                    "safetySettings": [
                        {
                            "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_HATE_SPEECH",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_HARASSMENT",
                            "threshold": "BLOCK_NONE"
                        },
                        {
                            "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                            "threshold": "BLOCK_NONE"
                        }
                    ]
                }
            )
            response.raise_for_status()
            response_data = response.json()
            
            # Check if response was blocked by safety filters
            if 'candidates' in response_data and response_data['candidates'][0].get('finishReason') == 'SAFETY':
                print("Response was blocked by safety filters. Returning original code.")
                return code
                
            # Check if we have valid content
            if ('candidates' in response_data and 
                len(response_data['candidates']) > 0 and 
                'content' in response_data['candidates'][0] and 
                'parts' in response_data['candidates'][0]['content'] and 
                len(response_data['candidates'][0]['content']['parts']) > 0):
                
                return response_data['candidates'][0]['content']['parts'][0]['text']
            else:
                print("Unexpected response structure")
                return code
                
        except requests.exceptions.RequestException as e:
            print(f"Gemini API request failed: {e}")
            return code
        except KeyError as e:
            print(f"Unexpected response format from Gemini: {e}")
            print("Full response:", response_data)
            return code

class LlamaHFHandler(LLMHandler):
    def __init__(self, model_name="TinyLlama/TinyLlama-1.1B-Chat-v1.0"):
        try:
            # Load tokenizer
            self.tokenizer = AutoTokenizer.from_pretrained(
                model_name,
                token="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"
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
        prompt = f"""[INST] You are a code reviewer. Analyze the following Java code for design smells and patterns.
Return your response in EXACTLY this JSON format, with no additional text:
{{"smells": [
    {{"type": "smell type here", "description": "detailed description here"}}
]}}

Code to analyze:
{code}
[/INST]"""

        try:
            response = self._generate(prompt)
            return self._parse_response(response)
        except Exception as e:
            print(f"Error in smell detection: {e}")
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        prompt = f"""[INST] You are a code refactoring assistant. Refactor the following Java code to fix this issue:
Issue to fix: {smell_description}

Original code:
{code}

Return only the refactored code without any explanations or markdown formatting.
[/INST]"""

        try:
            response = self._generate(prompt)
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
                    max_length=1024,  # Limit output length
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

class OllamaHandler(LLMHandler):
    def __init__(self, model_name="deepseek-r1:32b", base_url="http://localhost:11434"):
        self.model_name = model_name
        self.base_url = base_url
        # Test connection
        try:
            response = requests.get(f"{self.base_url}/api/version")
            print(f"Connected to Ollama version: {response.text}")
            print(f"Using model: {self.model_name}")
        except Exception as e:
            print(f"Warning: Could not connect to Ollama server: {e}")
            print("Make sure Ollama is running on your machine")

    def detect_smells(self, code: str) -> dict:
        prompt = f"""You are a code reviewer. Analyze the following Java code for design smells.
Return your response in EXACTLY this JSON format, with no additional text:
{{"smells": [
    {{"type": "smell type here", "description": "detailed description here"}}
]}}

Code to analyze:
{code}"""

        try:
            response = self._generate(prompt)
            return self._parse_response(response)
        except Exception as e:
            print(f"Error in smell detection: {e}")
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        prompt = f"""You are a code refactoring assistant. Refactor the following Java code to fix this issue:
Issue to fix: {smell_description}

Original code:
{code}

Return only the refactored code without any explanations or markdown formatting."""

        try:
            response = self._generate(prompt)
            return self._extract_code(response)
        except Exception as e:
            print(f"Error in refactoring: {e}")
            return code

    def _generate(self, prompt: str) -> str:
        try:
            response = requests.post(
                f"{self.base_url}/api/generate",
                json={
                    "model": self.model_name,
                    "prompt": prompt,
                    "stream": False,
                    "temperature": 0.7,
                    "top_p": 0.95,
                    "max_tokens": 2048
                },
                timeout=60
            )
            response.raise_for_status()
            return response.json().get("response", "")
        except requests.exceptions.RequestException as e:
            print(f"Ollama API error: {e}")
            return ""
        except Exception as e:
            print(f"Unexpected error in Ollama generation: {e}")
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


class OllamaCodeHandler(OllamaHandler):
    def __init__(self, model_name="codellama:7b", base_url="http://localhost:11434"):
        super().__init__(model_name, base_url)
        self.temperature = 0.3  # More deterministic output
        self.top_p = 0.9

    @log_llm_interaction
    def detect_smells(self, code: str) -> dict:
        prompt = f"""<PRE> {code} <SUF>
[INST] Analyze this Java code for code smells. Consider:
- SOLID violations
- Design pattern misuse
- Code duplication
- Maintainability issues

Return JSON format:
{{
  "smells": [
    {{
      "type": "smell_type",
      "description": "specific_issue",
      "location": "class:method" 
    }}
  ]
}} [/INST]"""
        return self._process_ollama_request(prompt)

    @log_llm_interaction
    def refactor(self, code: str, smell_description: str) -> str:
        prompt = f"""<PRE> {code} <SUF>
[INST] Refactor this code to fix: {smell_description}
- Preserve functionality
- Improve readability
- Follow Java best practices
- Add comments if needed

Return only the refactored code without explanations [/INST]"""
        return self._process_ollama_request(prompt, is_code=True)

    def _process_ollama_request(self, prompt: str, is_code=False):
        try:
            response = requests.post(
                f"{self.base_url}/api/generate",
                json={
                    "model": self.model_name,
                    "prompt": prompt,
                    "stream": False,
                    "temperature": self.temperature,
                    "top_p": self.top_p,
                    "format": "json" if not is_code else None,
                },
                timeout=120
            )
            response.raise_for_status()
            result = response.json()["response"]
            
            if is_code:
                return self._extract_code(result)
            return self._parse_ollama_json(result)
            
        except Exception as e:
            print(f"Ollama error: {str(e)}")
            return {"smells": []} if not is_code else code

    def _parse_ollama_json(self, text: str) -> dict:
        try:
            # Handle CodeLlama's tendency to add explanation text
            json_str = re.search(r'\{.*\}', text, re.DOTALL).group()
            return json.loads(json_str)
        except Exception as e:
            print(f"JSON parse error: {str(e)}")
            return {"smells": []}

# class JavaFileProcessor:
#     def __init__(self, llm_handler: LLMHandler):
#         self.llm = llm_handler

#     def process_directory(self, directory: str):
#         results = {}
#         for filename in os.listdir(directory):
#             if filename.endswith(".java"):
#                 file_path = os.path.join(directory, filename)
#                 with open(file_path, "r") as file:
#                     code = file.read()
#                     smells = self.llm.detect_smells(code)
#                     refactored_code = code
#                     for smell in smells.get("smells", []):
#                         refactored_code = self.llm.refactor(refactored_code, smell["description"])
#                     results[filename] = {
#                         "smells": smells,
#                         "refactored_code": refactored_code,
#                         "diff": self._generate_diff(code, refactored_code)
#                     }
#         return results

#     def _generate_diff(self, original: str, refactored: str) -> str:
#         return '\n'.join(difflib.unified_diff(
#             original.splitlines(),
#             refactored.splitlines(),
#             lineterm=''
#         ))

MAX_FILE_SIZE = 1000000  # 1 MB


class RefactoringPipeline:
    def __init__(self, llm_handler: LLMHandler):
        self.llm = llm_handler
        github_token = "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"
        if not github_token:
            raise ValueError("GITHUB_TOKEN not set")
        self.github = Github(github_token)
        self.repo = self.github.get_repo("SE-course-serc/project-1-team-3")

    def run_pipeline(self, directory: str):
        try:
            # Process files
            processor = JavaFileProcessor(self.llm)
            results = processor.process_directory(directory)
            
            # Get base branch reference
            default_branch = self.repo.default_branch
            base_ref = self.repo.get_git_ref(f"heads/{default_branch}")
            base_commit = self.repo.get_commit(base_ref.object.sha)

            # Create new branch
            branch_name = f"llm-refactor-{int(time.time())}"
            self.repo.create_git_ref(
                ref=f"refs/heads/{branch_name}",
                sha=base_commit.sha
            )

            # Create tree entries
            tree_entries = []
            for root, _, files in os.walk(directory):
                for filename in files:
                    if not filename.endswith('.java'):
                        continue
                        
                    # Get relative path from the repository root
                    full_path = os.path.join(root, filename)
                    repo_relative_path = os.path.relpath(full_path, directory)
                    
                    # Check if file has refactoring results
                    if filename in results:
                        content = results[filename]["refactored_code"]
                        
                        # Create blob
                        blob = self.repo.create_git_blob(
                            content=content,
                            encoding="utf-8"
                        )
                        
                        # Add tree entry
                        tree_entries.append(InputGitTreeElement(
                            path=repo_relative_path,
                            mode='100644',
                            type='blob',
                            sha=blob.sha
                        ))

            if not tree_entries:
                raise ValueError("No files to commit")

            # Create new tree
            new_tree = self.repo.create_git_tree(tree_entries, base_commit.commit.tree)
            
            # Create commit
            new_commit = self.repo.create_git_commit(
                message="Automated LLM Refactoring",
                tree=new_tree,
                parents=[base_commit.commit]
            )

            # Update branch reference
            branch_ref = self.repo.get_git_ref(f"heads/{branch_name}")
            branch_ref.edit(new_commit.sha)

            # Create pull request
            pr = self.repo.create_pull(
                title=f"LLM Refactoring {time.strftime('%Y-%m-%d')}",
                body="Automated code improvements from LLM analysis",
                head=branch_name,
                base=default_branch
            )

            return pr.html_url

        except Exception as e:
            print(f"Pipeline failed: {str(e)}")
            return None


def compare_llms(directory: str):
    """Compare results from different LLM handlers"""
    gemini_handler = GeminiHandler()
    llama_handler = LlamaHFHandler()
    ollama_handler = OllamaHandler()
    
    processors = {
        "Gemini": JavaFileProcessor(gemini_handler),
        "Llama": JavaFileProcessor(llama_handler),
        "Ollama": JavaFileProcessor(ollama_handler)
    }
    
    results = {}
    
    # Process with each LLM
    for name, processor in processors.items():
        try:
            results[name] = processor.process_directory(directory)
            print(f"\n{name} processing completed successfully")
        except Exception as e:
            print(f"\n{name} processing failed: {e}")
            results[name] = {}
    
    # Compare results
    for filename in set().union(*[r.keys() for r in results.values()]):
        print(f"\nComparison for {filename}:")
        print("=" * 80)
        
        for name in processors.keys():
            if filename in results.get(name, {}):
                print(f"\n{name} Analysis:")
                
                # Print detected smells
                smells = results[name][filename].get("smells", {}).get("smells", [])
                if smells:
                    print("\nDetected Smells:")
                    for smell in smells:
                        print(f"- Type: {smell.get('type', 'Unknown')}")
                        print(f"  Description: {smell.get('description', 'No description provided')}")
                else:
                    print("\nNo smells detected")
                
                # Print diff if available
                if "diff" in results[name][filename]:
                    print("\nCode Changes:")
                    print(results[name][filename]["diff"])
            else:
                print(f"\n{name}: No results available")
        
        print("\n" + "=" * 80)

class JavaFileProcessor:
    def __init__(self, llm_handler: LLMHandler):
        self.llm = llm_handler

    def process_directory(self, directory: str):
        results = {}
        for filename in os.listdir(directory):
            if filename.endswith(".java"):
                file_path = os.path.join(directory, filename)
                with open(file_path, "r") as file:
                    code = file.read()
                    smells = self.llm.detect_smells(code)

                    print("=====================")
                    print(smells)
                    print("=====================")
                    refactored_code = code
                    
                    # Process each smell, handling missing fields gracefully
                    for smell in smells.get("smells", []):
                        # Skip if no description available
                        if not smell.get("description"):
                            print(f"Warning: Smell detected but no description provided in {filename}")
                            continue
                            
                        refactored_code = self.llm.refactor(refactored_code, smell["description"])
                    
                    results[filename] = {
                        "smells": smells,
                        "refactored_code": refactored_code,
                        "diff": self._generate_diff(code, refactored_code)
                    }
        return results

    def _generate_diff(self, original: str, refactored: str) -> str:
        diff_lines = list(difflib.unified_diff(
            original.splitlines(),
            refactored.splitlines(),
            lineterm=''
        ))
        return '\n'.join(diff_lines) if diff_lines else "No changes made"

def save_refactored_files(results_by_model: dict, output_dir: str, original_dir: str):
    """
    Save refactored files while maintaining the original directory structure for each LLM.
    
    Args:
        results_by_model (dict): Dictionary with model names as keys and their results as values
        output_dir (str): Base output directory
        original_dir (str): Original source code directory to mirror structure from
    """
    def get_relative_path(file_path: str, base_path: str) -> str:
        """Get the relative path of a file with respect to the base path"""
        try:
            return os.path.relpath(file_path, base_path)
        except ValueError:
            # If files are on different drives, just return the full path
            return file_path

    # Walk through the original directory structure
    for root, dirs, files in os.walk(original_dir):
        # Process only Java files
        java_files = [f for f in files if f.endswith('.java')]
        if not java_files:
            continue

        # Get the relative path from the original directory
        rel_path = get_relative_path(root, original_dir)

        # Process each model's results
        for model_name, results in results_by_model.items():
            # Create model-specific directory with the same structure
            model_dir = os.path.join(output_dir, model_name.lower())
            target_dir = os.path.join(model_dir, rel_path)
            os.makedirs(target_dir, exist_ok=True)

            # Save refactored files in their corresponding locations
            for java_file in java_files:
                # Construct the relative path for the file
                file_rel_path = os.path.join(rel_path, java_file)
                
                # Check if we have results for this file
                if file_rel_path in results:
                    output_path = os.path.join(target_dir, java_file)
                    try:
                        with open(output_path, "w", encoding='utf-8') as file:
                            file.write(results[file_rel_path]["refactored_code"])
                        print(f"Saved {model_name} refactored file: {output_path}")
                    except Exception as e:
                        print(f"Error saving {model_name} refactored file {file_rel_path}: {e}")
                else:
                    print(f"No refactoring results found for {file_rel_path} in {model_name}")

# Example usage in main:
if __name__ == "__main__":
    directory = "project-1-team-3"
    output_dir = "refactored_output"
    
    # Process with different models
    results = {}
    
    # Process with Gemini
    try:
        gemini_handler = GeminiHandler()
        # enhanced_gemini_handler = EnhancedLLMHandler(gemini_handler)
        gemini_processor = JavaFileProcessor(gemini_handler)
        results["Gemini"] = gemini_processor.process_directory(directory)
    except Exception as e:
        print(f"Gemini processing failed: {e}")
        results["Gemini"] = {}
        
    # # Process with Llama
    # try:
    #     llama_handler = LlamaHFHandler()
    #     enhanced_llama_handler = EnhancedLLMHandler(llama_handler)
    #     llama_processor = JavaFileProcessor(enhanced_llama_handler)
    #     results["Llama"] = llama_processor.process_directory(directory)
    # except Exception as e:
    #     print(f"Llama processing failed: {e}")
    #     results["Llama"] = {}
        
    # # Process with Ollama
    # try:
    #     # ollama_handler = OllamaHandler()
    #     # enhanced_ollama_handler = EnhancedLLMHandler(ollama_handler)

    #     ollama_handler = OllamaCodeHandler(model_name="codellama:7b")
    #     enhanced_handler = EnhancedLLMHandler(ollama_handler)
    #     enhanced_ollama_processor = JavaFileProcessor(enhanced_ollama_handler)
    #     results["Ollama"] = ollama_processor.process_directory(directory)
    # except Exception as e:
    #     print(f"Ollama processing failed: {e}")
    #     results["Ollama"] = {}
    
    # # Save all results
    save_refactored_files(results, output_dir, directory)

    # # Compare results
    # compare_llms(directory)

    # # Run GitHub pipeline (optional)
    # try:
    #     pipeline = RefactoringPipeline(enhanced_llama_handler)
    #     pr_url = pipeline.run_pipeline(directory)
    #     print(f"Created PR: {pr_url}")
    # except Exception as e:
    #     print(f"GitHub pipeline failed: {e}")