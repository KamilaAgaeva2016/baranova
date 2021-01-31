package com.tinkoff.gifapp.data

import com.google.gson.annotations.SerializedName

class GifDTO(
    @SerializedName("id")
    val id : String?,
    @SerializedName("description")
    val description : String?,
    @SerializedName("gifURL")
    val gifURL : String?
)