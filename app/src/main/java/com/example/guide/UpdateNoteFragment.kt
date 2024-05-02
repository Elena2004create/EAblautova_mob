package com.example.guide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.guide.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class UpdateNoteFragment : Fragment() {

    private  val args by navArgs<UpdateNoteFragmentArgs>()
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_update_note, container, false)


        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesFragment)
        }

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        val userTitle: TextView = view.findViewById(R.id.update_title)
        val userText: TextView = view.findViewById(R.id.update_desc)
        val updateBtn: Button = view.findViewById(R.id.update_note_button)

        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        userTitle.setText(args.currentNote.title)
        userText.setText(args.currentNote.text)

        updateBtn.setOnClickListener(){
            val title = userTitle.text.toString()
            val text = userText.text.toString()

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

        return view
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