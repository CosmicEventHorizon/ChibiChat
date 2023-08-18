package com.pirouette.chibichat

import java.io.Serializable

data class Message(val msgContent: String, val user: Int): Serializable