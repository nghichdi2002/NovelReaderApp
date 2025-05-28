package com.namnh.novelreaderapp.item

data class History(

    var id: String = "",
    var title: String = "",
    var imageUrl: String = "",
    var currentChapter: Long? = null,
    var story: Story? = null

)