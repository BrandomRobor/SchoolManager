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

    @Query("SELECT * FROM homework WHERE hwId = :id")
    fun getHomeworkById(id: Int): Flow<Homework>

    @Query("SELECT homework.*, subject.* from homework INNER JOIN subject ON homework.subjectId = subject.id ORDER BY homework.hwName")
    fun getAllHomeworkWithSubject(): Flow<List<HomeworkWithSubject>>
}