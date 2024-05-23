package com.example.guide.data

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
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
import kotlinx.parcelize.Parcelize

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


@Parcelize
@Entity
    (tableName = "places",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)])
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val userId: Long?
): Parcelable


@Parcelize
data class Article(
    val title: String,
    val description: String,
    val url: String
): Parcelable




@Database(entities = [User::class, Note::class, Place::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun placeDao(): PlaceDao
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

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE login = :login AND pass = :pass")
    suspend fun getUser(login: String, pass: String): User?

    @Query("SELECT id FROM users WHERE login = :login AND pass = :pass")
    suspend fun getUserId(login: String, pass: String): Long?

    @Query("SELECT login FROM users WHERE id = :userId")
    suspend fun getUserLogin(userId: Long?): String

    @Query("SELECT pass FROM users WHERE id = :userId")
    suspend fun getUserPass(userId: Long?): String

    @Query("SELECT pass FROM users WHERE login = :login")
    suspend fun getUserPassByLogin(login: String): String?

    @Query("SELECT COUNT(*) FROM users WHERE login = :login")
    suspend fun isUsernameTaken(login: String): Int
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

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places")
    fun getAllPlaces(): LiveData<List<Place>>

    @Query("SELECT * FROM places WHERE userId = :userId")
    fun getPlacesByUserId(userId: Long?): LiveData<List<Place>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlace(place: Place)

    @Update
    suspend fun updatePlace(place: Place)

    @Delete
    suspend fun deletePlace(place: Place)

    @Query("DELETE FROM places WHERE userId = :userId")
    suspend fun deleteAllPlaces(userId: Long?)
}