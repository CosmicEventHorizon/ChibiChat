package com.pirouette.chibichat
import java.io.Serializable

data class SavedData(val name: String, val savedDataArray: ArrayList<Message>, val promptListSaved: MutableList<String>): Serializable