package me.brandom.schoolmanager.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SubjectWithHomeworks(
    @Embedded val subject: Subject,
    @Relation(parentColumn = "id", entityColumn = "subjectId")
    val homework: List<Homework>
)
