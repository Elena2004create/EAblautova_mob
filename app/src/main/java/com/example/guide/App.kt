package com.example.guide

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
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
import androidx.room.Update
import com.example.guide.NotesRepository
import com.example.guide.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
    }


    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
            .build()
    }


}

@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val login: String,
    val pass: String
): Parcelable

@Parcelize
@Entity
    (tableName = "notes",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val userId: Long?
): Parcelable


@Database(entities = [User::class, Note::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDataBase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            Log.d("base", "base")
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "database")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
        /*
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS users_temp (login TEXT NOT NULL, pass TEXT NOT NULL)")

                // Копируем данные из старой таблицы во временную таблицу
                database.execSQL("INSERT INTO users_temp (login, pass) SELECT login, pass FROM users")

                // Удаляем старую таблицу
                database.execSQL("DROP TABLE users")

                // Создаем новую таблицу с добавлением столбца id как PRIMARY KEY с автоинкрементом
                database.execSQL("CREATE TABLE IF NOT EXISTS users (login TEXT NOT NULL, pass TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                        "    title TEXT NOT NULL,\n" +
                        "    text TEXT NOT NULL,\n" +
                        "    userId INTEGER,\n" +
                        "    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE)")

                // Копируем данные из временной таблицы в новую таблицу
                database.execSQL("INSERT INTO users (login, pass) SELECT login, pass FROM users_temp")

                // Удаляем временную таблицу
                database.execSQL("DROP TABLE users_temp")
            }
        }

         */
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
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    fun getNotesByUserId(userId: Long?): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotes(userId: Long?)
}


class UserViewModel(application: Application): AndroidViewModel(application) {
    var id: Long? = null

    private val repository: UsersRepository

    init {
        val userDao = AppDatabase.getDataBase(application).userDao()
        repository = UsersRepository(userDao)
    }

    fun addUser(user: User){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(user)
        }
    }

    suspend fun getUserId(login: String, pass: String): Long? {
            return repository.getUserId(login, pass)
    }
    fun setUserId(id: Long){
        this.id = id
    }
}

class NoteViewModel(application: Application): AndroidViewModel(application) {

    private val repository: NotesRepository
    init {
        val noteDao = AppDatabase.getDataBase(application).noteDao()
        repository = NotesRepository(noteDao)
    }

    fun getAllNotes(id: Long?): LiveData<List<Note>>{
        return repository.getAllNotes(id)
    }
    fun addNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNote(note)
        }
    }

    fun updateNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun deleteAllNotes(id: Long?){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotes(id)
        }
    }
}


