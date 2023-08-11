package com.pirouette.chibichat

import com.google.gson.annotations.SerializedName

data class JsonClass (
    @SerializedName("n"                  ) val n                : Int?              = 1,
    @SerializedName("max_context_length" ) val maxContextLength : Int?              = 0,
    @SerializedName("max_length"         ) val maxLength        : Int?              = 0,
    @SerializedName("rep_pen"            ) val repPen           : Double?           = 1.1,
    @SerializedName("temperature"        ) val temperature      : Double?           = 0.7,
    @SerializedName("top_p"              ) val topP             : Double?           = 0.92,
    @SerializedName("top_k"              ) val topK             : Int?              = 0,
    @SerializedName("top_a"              ) val topA             : Int?              = 0,
    @SerializedName("typical"            ) val typical          : Int?              = 1,
    @SerializedName("tfs"                ) val tfs              : Int?              = 1,
    @SerializedName("rep_pen_range"      ) val repPenRange      : Int?              = 300,
    @SerializedName("rep_pen_slope"      ) val repPenSlope      : Double?           = 0.7,
    @SerializedName("sampler_order"      ) val samplerOrder     : IntArray          = intArrayOf(6, 0, 1, 3, 4, 2, 5),
    @SerializedName("prompt"             ) var prompt           : String?           = "",
    @SerializedName("quiet"              ) val quiet            : Boolean?          = true,
    @SerializedName("stop_sequence"      ) val stopSequence     : ArrayList<String>
)