import functools
import time

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