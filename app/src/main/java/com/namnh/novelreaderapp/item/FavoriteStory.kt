package com.namnh.novelreaderapp.item

data class FavoriteStory(
    var id: String = "",
    var title: String = "",
    var imageUrl: String = "",
    var story: Story? = null
)