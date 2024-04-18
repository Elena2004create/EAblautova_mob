package com.example.guide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddNoteFragment : Fragment() {
    lateinit var userViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_add_note, container, false)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_addNoteFragment_to_notesFragment)
        }
        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }
        //Log.d("1", userViewModel.id.toString())

        val userTitle: EditText = view.findViewById(R.id.note_name)
        val userText: EditText = view.findViewById(R.id.note_text)
        val addBtn: Button = view.findViewById(R.id.add_note_button)

        addBtn.setOnClickListener() {
            var title = userTitle.text.toString().trim()
            var text = userText.text.toString().trim()

            if (title == "" || text == "")
                Toast.makeText(requireContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show()
            else {

                lifecycleScope.launch {
                    val note = withContext(Dispatchers.IO) {
                        val db = App.database
                        val noteDao = db.NoteDao()
                        noteDao.addNote(Note(title = title, text = text, userId = userViewModel.id))
                        Toast.makeText(requireContext(), "Заметка добавлена", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                userTitle.text.clear()
                userText.text.clear()
            }
        }

        return view
    }

}