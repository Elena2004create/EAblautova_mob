package com.example.guide.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guide.data.AppDatabase
import com.example.guide.data.Note
import com.example.guide.data.Place
import com.example.guide.data.User
import com.yandex.mapkit.GeoObjectCollection
import com.yandex.mapkit.directions.driving.DrivingRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {
    var id: Long? = null

    var repository: UsersRepository
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

    val repository: NotesRepository
    init {
        val noteDao = AppDatabase.getDataBase(application).noteDao()
        repository = NotesRepository(noteDao)
    }

    fun getAllNotes(id: Long?): LiveData<List<Note>> {
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

    fun getAllPlaces(id: Long?): LiveData<List<Place>> {
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

    var userCountry: String = ""
    var userCity: String = ""

}
