package com.example.guide.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.guide.MainActivity
import com.example.guide.data.Note
import com.example.guide.models.NoteViewModel
import com.example.guide.R
import com.example.guide.models.UserViewModel
import com.example.guide.databinding.FragmentAddNoteBinding
import kotlinx.coroutines.launch

class AddNoteFragment : Fragment() {
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel
    lateinit var binding: FragmentAddNoteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddNoteBinding.inflate(layoutInflater)

        retainInstance = true
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        val toolbar = binding.toolbar

        toolbar.title = ""
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

        val userTitle = binding.noteTitle
        val userText = binding.noteDesc
        val addBtn = binding.addNoteButton

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

                    userText.text?.clear()
                    userTitle.text?.clear()

                }

            }
        }

    }

}