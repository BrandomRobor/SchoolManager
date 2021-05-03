package me.brandom.schoolmanager.database.entities

import androidx.room.ColumnInfo
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
    val hwName: String,
    val deadline: Long,
    @ColumnInfo(index = true)
    val subjectId: Int,
    val description: String? = null,
    @PrimaryKey(autoGenerate = true)
    val hwId: Int = 0
)
