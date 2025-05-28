package com.namnh.novelreaderapp.item

import com.google.firebase.database.Exclude
import java.io.Serializable


data class Chapter(
    var title: String = "",     //tieu de chuong
    var content: String = "",   //noi dung chuong
    var chapterId: String = "", //id chuong
    var storyId: String = ""   //id truyen
) : Serializable

