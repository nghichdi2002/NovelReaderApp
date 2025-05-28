package com.namnh.novelreaderapp.item

import com.google.firebase.database.Exclude
import java.io.Serializable


data class Comment (
    var id: String= "",
    var storyId: String= "",
    var userId: String= "",
    var content: String= "",
    var timestamp: Long = 0,
    var parentId: String= "",
    @get:Exclude var username: String= "",
    @get:Exclude var avatarUrl: String? = null
) : Serializable
