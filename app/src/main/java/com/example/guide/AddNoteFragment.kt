package com.example.guide

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.guide.MainActivity
import com.example.guide.Note
import com.example.guide.NoteViewModel
import com.example.guide.R
import com.example.guide.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class AddNoteFragment : Fragment() {
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_add_note, container, false)

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

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
        Log.d("1", userViewModel.id.toString())

        val userTitle: TextView = view.findViewById(R.id.note_title)
        val userText: TextView = view.findViewById(R.id.note_desc)
        val addBtn: Button = view.findViewById(R.id.add_note_button)

        addBtn.setOnClickListener() {
            var title = userTitle.text.toString().trim()
            var text = userText.text.toString().trim()

            if (title == "" || text == "")
                Toast.makeText(requireContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show()
            else {

                lifecycleScope.launch {
                    noteViewModel.addNote(Note(title = title, text = text, userId = userViewModel.id))
                    Toast.makeText(requireContext(), "Заметка добавлена", Toast.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.action_addNoteFragment_to_notesFragment)

                    userTitle.text = ""
                    userText.text = ""
                }
                /*
                lifecycleScope.launch {
                    val note = withContext(Dispatchers.IO) {
                        val db = App.database
                        val noteDao = db.noteDao()
                        noteDao.addNote(Note(title = title, text = text, userId = userViewModel.id))
                        Toast.makeText(requireContext(), "Заметка добавлена", Toast.LENGTH_LONG)
                            .show()
                    }
                }

                 */

            }
        }

        return view
    }

}