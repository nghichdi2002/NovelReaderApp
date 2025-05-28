package com.namnh.novelreaderapp.item

import com.google.firebase.database.Exclude
import java.io.Serializable


data class Story(
    var id: String = "",        // ID của truyện
    var title: String = "",     // Tiêu đề truyện
    var author: String = "",       // Tác giả
    var description: String = "",   // Mô tả
    var genre: String = "", // Thể loại
    var imageUrl: String = "",      // Ảnh bìa
    var chapters: Map<String, Chapter> = HashMap(), //D sách chương
    var viewCount: Long = 0,
    @get:Exclude var isFavorite: Boolean = false     // Yêu thích
) : Serializable


