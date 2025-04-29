package com.namnh.novelreaderapp.item

data class FavoriteStory(
    var id: String? = null,
    var title: String? = null,
    var imageUrl: String? = null,
    var currentChapter: Long = 0,  // Chap đang đọc
    var latestChapter: Long = 0, // Chap mới nhất
    var story: Story? = null
)