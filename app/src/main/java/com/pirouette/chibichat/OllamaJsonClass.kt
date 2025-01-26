package com.pirouette.chibichat

import com.google.gson.annotations.SerializedName

data class OllamaJsonClass (
    @SerializedName("model") var model           : String?          = "",
    @SerializedName("messages") val messages     : ArrayList<OllamaMessage>,
    @SerializedName("stream") val stream     : Boolean = false

)