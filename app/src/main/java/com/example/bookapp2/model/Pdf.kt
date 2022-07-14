package com.example.bookapp2.model

data class Pdf(
    var uid : String = "",
    var id : String = "",
    var title : String = "",
    var description: String = "",
    var categoryId: String = "",
    var url: String = "",
    var timestamp: Long = 0,
    var viewCount : Long = 0,
    var downloadCount : Long = 0,
    var isFavorite : Boolean = false
)