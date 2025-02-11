import requests
from .base_handler import LLMHandler


import re
import json
import os
import requests
from .base_handler import LLMHandler

# Directly import the log decorator
def log_llm_interaction(func):
    import functools
    import time

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
        prompt = f"""You are an expert software architect and code reviewer with years of experience in detecting software design issues. Your task is to analyze the following code and identify design smells and anti-patterns that may impact maintainability, scalability, or readability.

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
{code}"""

        try:
            response = self._generate(prompt)
            return self._parse_response(response)
        except Exception as e:
            print(f"Error in smell detection: {e}")
            return {"smells": []}

    def refactor(self, code: str, smell_description: str) -> str:
        prompt = f"""You are a code refactoring assistant specializing in improving software design by addressing structural issues. Your task is to refactor the given code to eliminate the following design smell:

    ❌ Problem: {smell_description}

    🔧 Refactoring Goals:

        Improve maintainability, readability, and modularity.
        Apply appropriate design patterns or SOLID principles where relevant.
        Ensure the refactored code remains functionally equivalent to the original.

    🚀 Your response should contain only the improved version of the code, with no additional explanation.

    Here is the original code:{code}"""

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
            text = text.strip()
            text = re.sub(r'```json\s*', '', text)  # Remove markdown code markers
            text = re.sub(r'```\s*', '', text)  # Remove closing markdown markers

            # Extract only the JSON part
            match = re.search(r'\[.*\]', text, re.DOTALL)  # Match list format
            if match:
                json_str = match.group(0)
                # print(f"🛠 Extracted JSON:\n{json_str}\n")

                # Fix common formatting issues
                json_str = json_str.replace("suggested_ fix", "suggested_fix")

                try:
                    # Convert array into an object with "smells" key
                    return {"smells": json.loads(json_str)}
                except json.JSONDecodeError as e:
                    print(f"JSON Decode Error: {e}")
                    print(f"Faulty JSON:\n{json_str}\n")
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
    """
    Deprecated class for Ollama code refactoring.
    """
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