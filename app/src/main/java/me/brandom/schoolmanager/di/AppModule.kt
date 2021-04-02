package me.brandom.schoolmanager.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.brandom.schoolmanager.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_db"
    )
        // This line is added only for testing
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providesSubjectDao(database: AppDatabase) = database.subjectDao()

    @Provides
    @Singleton
    fun providesHomeworkDao(database: AppDatabase) = database.homeworkDao()
}