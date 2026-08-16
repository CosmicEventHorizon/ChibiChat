package com.pirouette.chibichat

import com.google.gson.annotations.SerializedName

data class OllamaJsonClass (
    @SerializedName("model") var model           : String?          = "",
    @SerializedName("messages") val messages     : ArrayList<OllamaMessage>,
    @SerializedName("stream") val stream     : Boolean = false

)

data class OllamaTagsJsonClass(
    @SerializedName("models") val models: ArrayList<OllamaModelJsonClass> = arrayListOf()
)

data class OllamaModelJsonClass(
    @SerializedName("name") val name: String = ""
)
