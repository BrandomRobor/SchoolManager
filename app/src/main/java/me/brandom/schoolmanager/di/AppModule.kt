package me.brandom.schoolmanager.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.brandom.schoolmanager.database.AppDatabase
import me.brandom.schoolmanager.utils.ApplicationScope
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
        .build()

    @Provides
    fun providesSubjectDao(database: AppDatabase) = database.subjectDao()

    @Provides
    fun providesHomeworkDao(database: AppDatabase) = database.homeworkDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}