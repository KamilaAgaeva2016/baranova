package com.tinkoff.gifapp.domain

/**
 * Класс конкретной гифки для отображения на UI
 */
data class GifItem (
    val id : String,
    val description : String,
    val gifURL : String
)
