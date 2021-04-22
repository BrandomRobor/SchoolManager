package me.brandom.schoolmanager.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.SubjectWithHomeworks

@Dao
interface HomeworkDao {
    @Insert
    suspend fun insertHomework(homework: Homework)

    @Update
    suspend fun updateHomework(homework: Homework)

    @Delete
    suspend fun deleteHomework(homework: Homework)

    @Transaction
    @Query("SELECT * FROM subject")
    fun getAllSubjectsWithHomework(): Flow<List<SubjectWithHomeworks>>

    @Query("SELECT * FROM homework WHERE id = :id")
    fun getHomeworkById(id: Int): Flow<Homework>

    @Query("SELECT COUNT(name) FROM homework")
    suspend fun getHomeworkCount(): Int
}