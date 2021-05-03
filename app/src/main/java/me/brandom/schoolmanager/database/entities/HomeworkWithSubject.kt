package me.brandom.schoolmanager.database.entities

import androidx.room.Embedded

data class HomeworkWithSubject(
    @Embedded val homework: Homework,
    @Embedded val subject: Subject
)
