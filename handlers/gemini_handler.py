import requests
from .base_handler import LLMHandler

class GeminiHandler(LLMHandler):
    def __init__(self, model="gemini-pro"):
        self.model = model
        self.api_key = "AIzaSyADPQsmGSVDQdmMuaNhLoZx9lJispvpqKw"
        self.detect_smells_prompt = f"""Design Smell Detection Prompt

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

                Here is the code for review:"""

        if not self.api_key:
            raise ValueError("GEMINI_API_KEY environment variable is not set.")

    def refactor_prompt(self, smell_description: str) -> str:  
        """Returns a prompt for refactoring based on the detected smell."""
        return f"""Refactor Prompt

                You are a code refactoring assistant specializing in improving software design by addressing structural issues. Your task is to refactor the given code to eliminate the following design smell:

                Problem: {smell_description}

                Refactoring Goals:

                    Improve maintainability, readability, and modularity.
                    Apply appropriate design patterns or SOLID principles where relevant.
                    Ensure the refactored code remains functionally equivalent to the original.

                🚀 Your response should contain only the improved version of the code, with no additional explanation.

                Here is the original code:"""

    def detect_smells(self, code: str) -> dict:
        try:
            response = requests.post(
                f"https://generativelanguage.googleapis.com/v1beta/models/{self.model}:generateContent",
                headers={"Content-Type": "application/json"},
                params={"key": self.api_key},
                json={
                    "contents": [{
                        "parts": [{
                            "text": f"{self.detect_smells_prompt}\n{code}"
                        }]
                    }],
                    "safetySettings": [
                        {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "BLOCK_NONE"}
                    ]
                }
            )
            response.raise_for_status()
            response_data = response.json()
            
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
                            "text": f"{self.refactor_prompt(smell_description)}\n{code}"
                        }]
                    }],
                    "safetySettings": [
                        {"category": "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE"},
                        {"category": "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold": "BLOCK_NONE"}
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
