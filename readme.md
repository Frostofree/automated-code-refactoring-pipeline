# LLM Code Refactoring Tool

A Python-based tool that uses multiple Large Language Models (LLMs) to detect code smells and automatically refactor Java code.

## Features

- Multi-LLM support (Gemini, OpenAI, Llama, Ollama)
- Automated code smell detection
- Intelligent code refactoring suggestions
- GitHub integration for automated PR creation
- Comparative analysis between different LLMs
- Enhanced pattern-based smell detection

## Project Structure

```
.
├── handlers/          # LLM integration implementations
├── pipeline/         # GitHub integration and refactoring pipeline
├── processors/       # File processing and analysis
├── utils/           # Helper utilities
└── refactored_output/# Generated refactored code
```

## Prerequisites

- Python 3.8+
- Git
- Access tokens for supported LLMs
- Ollama (optional, for local LLM support)

## Environment Variables

```
OPENAI_API_KEY=your_key
GEMINI_API_KEY=your_key
GITHUB_TOKEN=your_token
HF_TOKEN=your_huggingface_token
```

## Installation

```bash
git clone [repository-url]
cd llm-refactoring-tool
pip install -r requirements.txt
```

## Usage

```bash
python main.py
```

## Advanced Configuration

The tool supports various configuration options in each handler:

- Custom model selection
- Temperature and sampling parameters
- Response formatting preferences
- Pattern matching thresholds

## Supported Code Smells

- God Class
- Feature Envy
- Long Method
- Data Class
- Duplicate Code
- And more...

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

MIT License