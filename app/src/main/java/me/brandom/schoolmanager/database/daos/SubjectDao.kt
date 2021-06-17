package me.brandom.schoolmanager.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.brandom.schoolmanager.database.entities.Subject

@Dao
interface SubjectDao {
    @Insert
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subject WHERE id = :id")
    suspend fun deleteSubjectById(id: Int)

    @Query("SELECT * FROM subject ORDER BY name")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subject WHERE id = :id")
    fun getSubjectById(id: Int): Flow<Subject>

    @Query("SELECT COUNT(name) FROM subject")
    fun getSubjectCount(): Flow<Int>
}