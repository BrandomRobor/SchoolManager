package me.brandom.schoolmanager.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Entity(
    foreignKeys = [ForeignKey(
        entity = Subject::class,
        parentColumns = ["id"],
        childColumns = ["subjectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class Homework(
    val hwName: String,
    val deadline: OffsetDateTime,
    @ColumnInfo(index = true)
    val subjectId: Int,
    val description: String? = null,
    val isComplete: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val hwId: Int = 0
) : Parcelable {
    val formattedDate: String
        get() = deadline.toZonedDateTime()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    val formattedTime: String
        get() = deadline.toZonedDateTime()
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val formattedDateTime: String
        get() = deadline.toZonedDateTime()
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
}
