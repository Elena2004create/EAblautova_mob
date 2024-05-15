package com.example.guide

import android.app.Application
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.yandex.mapkit.GeoObjectCollection
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
    }


    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("12b42db8-bb92-4c01-aac5-16894bdc92b3")
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


class UserViewModel(application: Application): AndroidViewModel(application) {
    var id: Long? = null

    private val repository: UsersRepository
    val registrationResult: MutableLiveData<Boolean> = MutableLiveData()

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

    fun updateUser(user: User){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
        }
    }

    suspend fun getUserLogin(id: Long?): String{
        return repository.getUserLogin(id)

    }

    suspend fun getUserPass(id: Long?) : String{
        return repository.getUserPass(id)
    }

    suspend fun getUserPassByLogin(login: String) : String?{
        return repository.getUserPassByLogin(login)
    }

    fun registerUser(login: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isTaken = repository.isUsernameTaken(login)
            if (isTaken) {
                registrationResult.postValue(false) // Имя пользователя занято
            } else {
                registrationResult.postValue(true) // Успешная регистрация
            }
        }
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


class PlaceViewModel(application: Application): AndroidViewModel(application) {

    private val repository: PlacesRepository
    init {
        val placeDao = AppDatabase.getDataBase(application).placeDao()
        repository = PlacesRepository(placeDao)
    }

    fun getAllPlaces(id: Long?): LiveData<List<Place>>{
        return repository.getAllPlaces(id)
    }
    fun addPlace(place: Place){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlace(place)
        }
    }

    fun updatePlace(place: Place){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlace(place)
        }
    }

    fun deletePlace(place: Place){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlace(place)
        }
    }

    fun deleteAllPlaces(id: Long?){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllPlaces(id)
        }
    }
}



class LocationViewModel : ViewModel() {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    var userLatitude: Double = 0.0
    var userLongitude: Double = 0.0

    var routes: List<DrivingRoute> = emptyList<DrivingRoute>()

    var points: MutableList<GeoObjectCollection.Item> = mutableListOf()

}


