import os

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