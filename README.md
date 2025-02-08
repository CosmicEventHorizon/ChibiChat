# ChibiChat 📱🗣️

ChibiChat is an Android application designed to communicate with Ollama or Koboldcpp API endpoints. It's licensed under the [GNU General Public License version 3](https://github.com/CosmicEventHorizon/ChibiChat/blob/main/LICENSE).

<img src="https://github.com/Blood-Pirouette/ChibiChat/blob/main/images/miku.png" width="100" height="100">

<img src="https://github.com/Blood-Pirouette/ChibiChat/blob/main/images/screenshot.jpg" width="400" height="900">

## Getting Started ⚙️

This app communicates with Ollama or Koboldcpp API endpoints, which can be hosted locally or remotely on a phone or computer device.

### Prerequisites 📋

- For Koboldcpp:
  - Follow the instructions on [LostRuin's GitHub](https://github.com/LostRuins/koboldcpp) to run it on your computer device.
  - Alternatively, you can build koboldcpp on Termux using an automated script hosted in this repo.

- For Ollama:
  - Follow the instructions on [Ollama's GitHub](https://github.com/ollama/ollama).
  - **Note:** You need to expose your Ollama server to your network to access it remotely, as outlined in their [FAQ](https://github.com/ollama/ollama/blob/main/docs/faq.md#how-do-i-configure-ollama-server).


## Usage 🔧

### Ollama

1. **Installation:**
   - Download and install the latest APK from the [Releases](https://github.com/Blood-Pirouette/ChibiChat/releases) section.
   - Start your server on the computer or Android device, note down its IP address and port.

2. **Setup:**
   - Open ChibiChat, go to settings, select Ollama, and press "Save".
   - In Ollama settings:
     - Type your server's IP address (e.g., `192.168.1.100`) under the "IP Address of the Server" field.
     - Enter your server's port number (e.g., `11434`) in the "Port" field.
     - Add the model installed on your server under the "Model" field, then press "Save".

 ### Koboldcpp

1. **Installation:**
   - Follow the same steps as Ollama installation.

2. **Setup:**
   - Open ChibiChat, go to settings, type in your server's IP address (e.g., `192.168.1.100`) and port number (e.g., `5001`), then press "Save".


## Options 🛠️

In Koboldcpp settings:

- **User_Identifier:** Defaults to `USER:`.
- **AI_Identifier:** Defaults to `ASSISTANT:`.
- **System Prompt:** A template for the initial context. Example: `"A chat between a curious user and an assistant..."`.
- **Context Prompt:** Conversation context for the model, ending with Assistant's response.
- **Stop Token:** Tokens that Koboldcpp uses to detect when the AI has finished its turn. Separate tokens with commas (e.g., `USER:, ASSISTANT:`).
- **End String:** Any "extra" tokens added by the AI at the end of its message. Separate them with commas (e.g., `USER:, ASSISTANT:, ENDTOKEN`).

## Launch Koboldcpp Server Locally on Android 📱💻

1. **Installation:**
   - An option menu "Launch Android Server" is present for running a local server easily.
   - Termux is required for installation.
2. **Running the Server:**
   - Copy and paste the command into Termux and wait for the server to start.
   - Press "Load Recommended Settings" to load optimal settings for the model.
   - Currently, only Alpaca 7B is listed as an option, but this will be expanded upon.

**Note:** At least 8GB of RAM is needed to run this model locally. Creating a swap file is recommended if your device is rooted.

## To-Do 🔄

- Add support for other AI endpoints such as llama.cpp
- Improve UI and add more functionalities like copy-paste text.
- Implement support for streaming API for text generation
- Implement import/export settings functionality.
- General improvements and bug fixes.