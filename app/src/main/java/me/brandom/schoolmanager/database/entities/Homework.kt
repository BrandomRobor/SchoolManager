package me.brandom.schoolmanager.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"]
    )]
)
data class Homework(
    val name: String,
    val deadline: Long,
    val subjectId: Int,
    val description: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
