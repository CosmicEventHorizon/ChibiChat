package com.pirouette.chibichat

import java.io.Serializable

data class OllamaMessage(val role: String, val content: String): Serializable