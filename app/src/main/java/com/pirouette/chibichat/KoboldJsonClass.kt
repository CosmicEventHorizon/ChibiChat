package com.pirouette.chibichat

import com.google.gson.annotations.SerializedName

data class KoboldJsonClass (
    @SerializedName("n"                  ) val n                : Int?             = 1,
    @SerializedName("max_context_length" ) val maxContextLength : Int?             = 0,
    @SerializedName("max_length"         ) val maxLength        : Int?             = 0,
    @SerializedName("rep_pen"            ) val repPen           : Float?           = 1.1f,
    @SerializedName("temperature"        ) val temperature      : Float?           = 0.7f,
    @SerializedName("top_p"              ) val topP             : Float?           = 0.92f,
    @SerializedName("top_k"              ) val topK             : Int?             = 0,
    @SerializedName("top_a"              ) val topA             : Int?             = 0,
    @SerializedName("typical"            ) val typical          : Int?             = 1,
    @SerializedName("tfs"                ) val tfs              : Int?             = 1,
    @SerializedName("rep_pen_range"      ) val repPenRange      : Int?             = 300,
    @SerializedName("rep_pen_slope"      ) val repPenSlope      : Float?           = 0.7f,
    @SerializedName("sampler_order"      ) val samplerOrder     : IntArray         = intArrayOf(6, 0, 1, 3, 4, 2, 5),
    @SerializedName("prompt"             ) var prompt           : String?          = "",
    @SerializedName("quiet"              ) val quiet            : Boolean?         = true,
    @SerializedName("stop_sequence"      ) val stopSequence     : ArrayList<String>
)