package me.brandom.schoolmanager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import me.brandom.schoolmanager.database.daos.HomeworkDao
import me.brandom.schoolmanager.database.daos.SubjectDao
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.Subject

@Database(entities = [Subject::class, Homework::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun homeworkDao(): HomeworkDao
}