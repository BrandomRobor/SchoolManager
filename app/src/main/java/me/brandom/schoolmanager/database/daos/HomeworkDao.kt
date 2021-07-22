package me.brandom.schoolmanager.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.HomeworkWithSubject

@Dao
interface HomeworkDao {
    @Insert
    suspend fun insertHomework(homework: Homework): Long

    @Update
    suspend fun updateHomework(homework: Homework)

    @Delete
    suspend fun deleteHomework(homework: Homework)

    @Query("SELECT homework.*, subject.* FROM homework JOIN subject ON homework.subjectId = subject.id WHERE homework.hwId = :id")
    suspend fun getHomeworkWithSubjectByHwId(id: Int): HomeworkWithSubject

    @Query("SELECT homework.*, subject.* from homework INNER JOIN subject ON homework.subjectId = subject.id ORDER BY homework.hwName")
    fun getAllHomeworkWithSubject(): Flow<List<HomeworkWithSubject>>
}