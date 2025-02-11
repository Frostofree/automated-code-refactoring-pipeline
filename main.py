import os
from handlers import OpenAIHandler, GeminiHandler, LlamaHFHandler, OllamaHandler
from processors import JavaFileProcessor
from utils import log_utils, file_utils
from utils.log_utils import log_llm_interaction  # Add this import
from pipeline import RefactoringPipeline

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

if __name__ == "__main__":
    directory = "../project-1-team-3"
    output_dir = "refactored_output"
    
    # Process with different models
    results = {}
    
    # Process with Gemini
    try:
        gemini_handler = GeminiHandler()
        gemini_processor = JavaFileProcessor(gemini_handler)
        results["Gemini"] = gemini_processor.process_directory(directory)
    except Exception as e:
        print(f"Gemini processing failed: {e}")
        results["Gemini"] = {}
        
    # Process with Llama
    # try:
    #     llama_handler = LlamaHFHandler()
    #     llama_processor = JavaFileProcessor(llama_handler)
    #     results["Llama"] = llama_processor.process_directory(directory)
    # except Exception as e:
    #     print(f"Llama processing failed: {e}")
    #     results["Llama"] = {}
        
    # Process with Ollama
    # try:
    #     ollama_handler = OllamaHandler()
    #     ollama_processor = JavaFileProcessor(ollama_handler)
    #     results["Ollama"] = ollama_processor.process_directory(directory)
    # except Exception as e:
    #     print(f"Ollama processing failed: {e}")
    #     results["Ollama"] = {}
    
    # Save all results
    file_utils.save_refactored_files(results, output_dir, directory)

    # Compare results
    # compare_llms(directory)

    handlers = [
        GeminiHandler()
        # LlamaHFHandler(),
        # OllamaHandler()
    ]
    
    # Run pipeline for each handler
    for handler in handlers:
        pipeline = RefactoringPipeline(handler)
        pr_url = pipeline.run_pipeline(directory)
        if pr_url:
            print(f"Pull Request created: {pr_url}")

