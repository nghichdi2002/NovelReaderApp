package com.namnh.novelreaderapp.item

import java.io.Serializable


data class Story(
    var id: String,        // ID của truyện
    var title: String,     // Tiêu đề truyện
    var author: String,       // Tác giả
    var description: String,   // Mô tả
    var genre: String, // Thể loại
    var imageUrl: String,      // Ảnh bìa
    var chapters: Map<String, Chapter> = HashMap(), //D sách chương
    var isFavorite: Boolean = false     // Yêu thích
) : Serializable {
    // Constructor mặc định để sử dụng với Firebase
    constructor() : this("", "", "", "", "", "", HashMap())


    // Constructor cho trường hợp tạo đối tượng Favorite
    constructor(
        id: String,
        title: String,
        author: String,
        description: String,
        genre: String,
        imageUrl: String,
        isFavorite: Boolean
    ) :
            this(id, title, author, description, genre, imageUrl, HashMap()) {
        this.isFavorite = isFavorite
    }
}