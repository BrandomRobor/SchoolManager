package me.brandom.schoolmanager.database.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import me.brandom.schoolmanager.database.AppDatabase
import me.brandom.schoolmanager.database.entities.Homework
import me.brandom.schoolmanager.database.entities.Subject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class HomeworkDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: HomeworkDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.homeworkDao()

        // Needed to avoid database constraint error
        val subject = Subject("Test subject")
        runBlockingTest {
            database.subjectDao().insertSubject(subject)
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * This is absolutely messy...
     * But the tests pass!
     */

    @Test
    fun insertHomework() = runBlockingTest {
        val homework = Homework("Test homework", 1626210104047, 1, hwId = 1)
        dao.insertHomework(homework)

        val allHomework = dao.getAllHomeworkWithSubject().first()
        assert(allHomework.map { it.homework }.contains(homework))
    }

    @Test
    fun updateHomework() = runBlockingTest {
        val homework = Homework("Test homework", 1626210104047, 1, hwId = 1)
        dao.insertHomework(homework)

        val updatedHw = homework.copy(hwName = "Updated homework", deadline = 1626212755745)
        dao.updateHomework(updatedHw)

        val allHomework = dao.getAllHomeworkWithSubject().first()
        assert(allHomework[0].homework != homework)
    }

    @Test
    fun deleteHomework() = runBlockingTest {
        val homework = Homework("Test homework", 1626210104047, 1, hwId = 1)
        dao.insertHomework(homework)

        dao.deleteHomework(homework)
        val allHomework = dao.getAllHomeworkWithSubject().first()
        assert(allHomework.isEmpty())
    }

    @Test
    fun getAllHomeworkWithSubjects() = runBlockingTest {
        val homework1 = Homework("Test homework #1", 1626210104047, 1, hwId = 1)
        val homework2 = Homework("Test homework #2", 1626211895482, 1, hwId = 2)
        val homework3 = Homework("Test homework #3", 1626211905520, 1, hwId = 3)

        dao.insertHomework(homework1)
        dao.insertHomework(homework2)
        dao.insertHomework(homework3)

        val allHomework = dao.getAllHomeworkWithSubject().first()
        assert(allHomework.map { it.homework }.containsAll(listOf(homework1, homework2, homework3)))
    }
}