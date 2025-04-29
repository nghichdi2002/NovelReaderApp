package com.namnh.novelreaderapp.item

import java.io.Serializable


class Chapter : Serializable  {
    var title: String = ""         // Tiêu đề chương
    var content: String = ""       // Nội dung chương
    var numberOfWords: Int = 0     // Số từ của chương
    var author: String = ""        // Tác giả của chương
    var genre: String = ""         // Thể loại chương
    var chapterId: String = ""     // ID của chương
    var storyId: String = ""       // ID của truyện
    var uid: String = ""           // UID của chương

    constructor()  // Constructor trống cần thiết cho Firebase

    constructor(title: String, content: String, numberOfWords: Int, author: String, genre: String, chapterId: String, storyId: String) {
        this.title = title
        this.content = content
        this.numberOfWords = numberOfWords
        this.author = author
        this.genre = genre
        this.chapterId = chapterId
        this.storyId = storyId
    }

    constructor(title: String, content: String, author: String, genre: String, chapterId: String, storyId: String) {
        this.title = title
        this.content = content
        this.author = author
        this.genre = genre
        this.chapterId = chapterId
        this.storyId = storyId
    }

    constructor(title: String, content: String, chapterId: String, storyId: String) {
        this.title = title
        this.content = content
        this.chapterId = chapterId
        this.storyId = storyId
    }
}