package com.example.guide.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.guide.MainActivity
import com.example.guide.data.Note
import com.example.guide.R
import com.example.guide.databinding.FragmentUpdateNoteBinding
import com.example.guide.models.NoteViewModel
import com.example.guide.models.UserViewModel

class UpdateNoteFragment : Fragment() {

    private  val args by navArgs<UpdateNoteFragmentArgs>()
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel
    lateinit var binding: FragmentUpdateNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUpdateNoteBinding.inflate(layoutInflater)

        retainInstance = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.toolbar

        toolbar.title = ""
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesFragment)
        }

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)


        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        binding.apply {
            updateTitle.setText(args.currentNote.title)
            updateDesc.setText(args.currentNote.text)

            updateUserButton.setOnClickListener(){
                val title = updateTitle.text.toString()
                val text = updateDesc.text.toString()

                if (title == "" || text == "")
                    Toast.makeText(requireContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show()
                else {
                    val updatedNote = Note(args.currentNote.id, title, text, userViewModel.id)
                    noteViewModel.updateNote(updatedNote)
                    Toast.makeText(requireContext(), "Заметка изменена", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_updateNoteFragment_to_notesFragment)
                }
            }
            setHasOptionsMenu(true)
        }

    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_menu){
            deleteNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNote() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Да"){ _, _ ->
            noteViewModel.deleteNote(args.currentNote)
            Toast.makeText(requireContext(), "Удалена заметка: ${args.currentNote.title}", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesFragment)
        }
        builder.setNegativeButton("Нет"){ _, _ ->

        }
        builder.setTitle("Удалить ${args.currentNote.title}?")
        builder.create().show()
    }
}