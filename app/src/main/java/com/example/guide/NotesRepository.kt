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


class UsersRepository(private val userDao: UserDao) {

    suspend fun insert(user: User){
        userDao.insert(user)
    }

    suspend fun getUserId(login: String, pass: String): Long? {
        return userDao.getUserId(login, pass)
    }
}
