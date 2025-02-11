import os
import json
import re
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

import json
import re

class LLMHandler:
    """Base class for LLM integrations"""
    
    def detect_smells(self, code: str) -> dict:
        raise NotImplementedError
        
    def refactor(self, code: str, smell_description: str) -> str:
        raise NotImplementedError

    def _parse_response(self, text: str) -> dict:
        """
        Extracts JSON from Markdown-style code blocks and ensures correct formatting.
        Handles cases where response is an array (`[...]`) instead of an object (`{...}`).
        """
        try:
            # print(f" Raw Response Text:\n{text}\n")

            # Extract JSON from Markdown-style block
            json_match = re.search(r'```json\s*([\s\S]+?)\s*```', text)
            if json_match:
                text = json_match.group(1).strip()  # Get only the JSON content

            # Attempt JSON parsing
            parsed_json = json.loads(text)

            # If it's a list, wrap it in a dictionary for consistency
            if isinstance(parsed_json, list):
                return {"smells": parsed_json}

            return parsed_json

        except json.JSONDecodeError as e:
            print(f"❌ Failed to parse JSON response: {e}")
            print(f"❌ Faulty JSON:\n{text}\n")
            return {"smells": [text]}
