package me.brandom.schoolmanager.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Subject(
    val name: String,
    val location: String? = null,
    val teacherName: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {
    override fun toString(): String = name
}
