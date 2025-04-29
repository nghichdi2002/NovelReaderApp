package com.namnh.novelreaderapp.item

import com.google.firebase.database.Exclude


data class Comment (
    var id: String? = null,
    var storyId: String? = null,
    var userId: String? = null,
    var content: String? = null,
    var timestamp: Long = 0,
    @get:Exclude var username: String? = null,
    @get:Exclude var avatarUrl: String? = null

)
