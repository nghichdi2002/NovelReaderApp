package com.namnh.novelreaderapp.item

data class History(

    var id: String? = null, // Để String? vì có thể có trường hợp id chưa được gán
    var title: String? = null, // Để String? vì có thể có trường hợp title chưa được gán
    var imageUrl: String? = null, // Để String? vì có thể có trường hợp imageUrl chưa được gán
    var currentChapter: Long? = null, // Để Long? để tránh lỗi khi đọc từ Firebase
    var latestChapter: Long? = null, // Để Long? để tránh lỗi khi đọc từ Firebase
    var story: Story? = null // Để Story? vì có thể có trường hợp story chưa được gán

)