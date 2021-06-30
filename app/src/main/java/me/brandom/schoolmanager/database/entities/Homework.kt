package me.brandom.schoolmanager.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.text.DateFormat

@Entity(
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
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
) {
    val formattedDate: String get() = DateFormat.getDateInstance(DateFormat.SHORT).format(deadline)
    val formattedTime: String get() = DateFormat.getTimeInstance(DateFormat.SHORT).format(deadline)
    val formattedDateTime: String
        get() = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        ).format(deadline)
}
