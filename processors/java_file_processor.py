import os
import difflib
from handlers.base_handler import LLMHandler

class JavaFileProcessor:
    def __init__(self, llm_handler: LLMHandler):
        self.llm = llm_handler

    def process_directory(self, directory: str):
        results = {}
        for root, _, files in os.walk(directory):
            for filename in files:
                if filename.endswith(".java"):
                    file_path = os.path.join(root, filename)
                    print("=" * 80)
                    print(f"Processing file: {file_path}")
                    
                    try:
                        with open(file_path, "r", encoding='utf-8') as file:
                            code = file.read()
                            smells = self.llm.detect_smells(code)
                            refactored_code = code
                        
                            # Process each smell, handling missing fields gracefully
                            for smell in smells.get("smells", []):
                                # Skip if no description available
                                if not smell.get("description"):
                                    print(f"Warning: Smell detected but no description provided in {filename}")
                                    continue
                                
                                refactored_code = self.llm.refactor(refactored_code, smell["description"])
                        
                            # Store results using relative path
                            relative_path = os.path.relpath(file_path, directory)
                            results[relative_path] = {
                                "smells": smells,
                                "refactored_code": refactored_code,
                                "diff": self._generate_diff(code, refactored_code)
                            }
                    except Exception as e:
                        print(f"Error processing {file_path}: {e}")
        
        return results

    def _generate_diff(self, original: str, refactored: str) -> str:
        diff_lines = list(difflib.unified_diff(
            original.splitlines(),
            refactored.splitlines(),
            lineterm=''
        ))
        return '\n'.join(diff_lines) if diff_lines else "No changes made"