import os
import time
from github import Github, InputGitTreeElement
from processors.java_file_processor import JavaFileProcessor
from handlers.base_handler import LLMHandler

class RefactoringPipeline:
    def __init__(self, llm_handler: LLMHandler):
        self.llm = llm_handler
        github_token = os.getenv("GITHUB_TOKEN", "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")
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

if __name__ == "__main__":
    directory = "../project-1-team-3"
    handlers = [
        LLMHandler()
    ]
    
    # Run pipeline for each handler
    for handler in handlers:
        pipeline = RefactoringPipeline(handler)
        pr_url = pipeline.run_pipeline(directory)
        print(f"Pull Request: {pr_url}")