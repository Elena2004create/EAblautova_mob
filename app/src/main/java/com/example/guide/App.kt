package com.example.guide

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
    }



    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }


}
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val login: String,
    val pass: String
)

@Entity
    (tableName = "notes",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val userId: Long?
)


@Database(entities = [User::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun NoteDao(): NoteDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS users_temp (login TEXT NOT NULL, pass TEXT NOT NULL)")

                // Копируем данные из старой таблицы во временную таблицу
                database.execSQL("INSERT INTO users_temp (login, pass) SELECT login, pass FROM users")

                // Удаляем старую таблицу
                database.execSQL("DROP TABLE users")

                // Создаем новую таблицу с добавлением столбца id как PRIMARY KEY с автоинкрементом
                database.execSQL("CREATE TABLE IF NOT EXISTS users (login TEXT NOT NULL, pass TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

                // Копируем данные из временной таблицы в новую таблицу
                database.execSQL("INSERT INTO users (login, pass) SELECT login, pass FROM users_temp")

                // Удаляем временную таблицу
                database.execSQL("DROP TABLE users_temp")
            }
        }
    }
}


@Dao
interface UserDao {
    @Query("SELECT * FROM users")
     suspend fun getAll(): List<User>

    @Insert
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)


    @Query("SELECT * FROM users WHERE login = :login AND pass = :pass")
    suspend fun getUser(login: String, pass: String): User?

    @Query("SELECT id FROM users WHERE login = :login AND pass = :pass")
    suspend fun getUserId(login: String, pass: String): Long?
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getNotesByUserId(userId: Long): List<Note>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}


class UserViewModel : ViewModel() {
    var id: Long? = null
}


