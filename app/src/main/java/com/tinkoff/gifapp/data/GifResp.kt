package com.tinkoff.gifapp.data

import com.google.gson.annotations.SerializedName


data class GifResp(
    @SerializedName("result")
    val result : List<GifDTO>?
)