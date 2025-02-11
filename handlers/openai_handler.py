import requests
from .base_handler import LLMHandler

class OpenAIHandler(LLMHandler):
    def __init__(self, model="gpt-4"):
        self.model = model
        self.api_key = os.getenv("OPENAI_API_KEY")
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