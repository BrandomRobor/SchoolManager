package me.brandom.schoolmanager.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject
import me.brandom.schoolmanager.utils.SortOrder

@Dao
interface HomeworkDao {
    @Insert
    suspend fun insertHomework(homework: Homework): Long

    @Update
    suspend fun updateHomework(homework: Homework)

    @Delete
    suspend fun deleteHomework(homework: Homework)

    @Query(
        "SELECT homework.*, subject.* FROM homework " +
                "INNER JOIN subject ON homework.subjectId = subject.id " +
                "WHERE homework.hwId = :id"
    )
    suspend fun getHomeworkWithSubjectByHwId(id: Int): HomeworkWithSubject

    @Query("SELECT hwId FROM homework WHERE subjectId = :id")
    suspend fun getAllHomeworkIdsWithSubjectId(id: Int): List<Int>

    @Query(
        "SELECT homework.*, subject.* FROM homework " +
                "INNER JOIN subject ON homework.subjectId = subject.id " +
                "ORDER BY homework.hwName"
    )
    fun getAllHomeworkWithSubjectByName(): Flow<List<HomeworkWithSubject>>

    @Query(
        "SELECT homework.*, subject.* FROM homework " +
                "INNER JOIN subject ON homework.subjectId = subject.id " +
                "ORDER BY homework.deadline"
    )
    fun getAllHomeworkWithSubjectByDeadline(): Flow<List<HomeworkWithSubject>>

    fun getAllHomeworkWithSubject(sortOrder: SortOrder) = when (sortOrder) {
        SortOrder.BY_NAME -> getAllHomeworkWithSubjectByName()
        SortOrder.BY_DEADLINE -> getAllHomeworkWithSubjectByDeadline()
    }
}