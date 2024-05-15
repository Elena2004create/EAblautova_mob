package com.example.guide

import androidx.lifecycle.LiveData
import com.example.guide.Note
import com.example.guide.NoteDao
import com.example.guide.User
import com.example.guide.UserDao

class NotesRepository(private val noteDao: NoteDao) {

    fun getAllNotes(id: Long?): LiveData<List<Note>>{
        return noteDao.getNotesByUserId(id)
    }

    suspend fun addNote(note: Note){
        noteDao.addNote(note)
    }

    suspend fun updateNote(note: Note){
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note){
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotes(id: Long?){
        noteDao.deleteAllNotes(id)
    }
}


class PlacesRepository(private val placeDao: PlaceDao) {

    fun getAllPlaces(id: Long?): LiveData<List<Place>>{
        return placeDao.getPlacesByUserId(id)
    }

    suspend fun addPlace(place: Place){
        placeDao.addPlace(place)
    }

    suspend fun updatePlace(place: Place){
        placeDao.updatePlace(place)
    }

    suspend fun deletePlace(place: Place){
        placeDao.deletePlace(place)
    }

    suspend fun deleteAllPlaces(id: Long?){
        placeDao.deleteAllPlaces(id)
    }
}


class UsersRepository(private val userDao: UserDao) {

    suspend fun insert(user: User){
        userDao.insert(user)
    }

    suspend fun getUserId(login: String, pass: String): Long? {
        return userDao.getUserId(login, pass)
    }
    suspend fun updateUser(user: User){
        userDao.updateUser(user)
    }

    suspend fun getUserLogin(id: Long?) : String{
        return userDao.getUserLogin(id)
    }

    suspend fun getUserPass(id: Long?) : String{
        return userDao.getUserPass(id)
    }

    suspend fun isUsernameTaken(login: String): Boolean {
        return userDao.isUsernameTaken(login) > 0
    }

    suspend fun getUserPassByLogin(login: String) : String?{
        return userDao.getUserPassByLogin(login)
    }

}
