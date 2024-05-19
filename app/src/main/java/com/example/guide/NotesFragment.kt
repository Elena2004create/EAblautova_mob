package com.example.guide

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guide.databinding.FragmentHomeBinding
import com.example.guide.databinding.FragmentNotesBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() {
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel
    var notes: List<Note> = emptyList()
    lateinit var binding: FragmentNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentNotesBinding.inflate(layoutInflater)

        retainInstance = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)


        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val adapter = NotesAdapter()

        if (savedInstanceState != null) {
            notes = savedInstanceState.getParcelableArrayList("notes") ?: emptyList()
            adapter.setData(notes)
        }

        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        noteViewModel.getAllNotes(userViewModel.id).observe(viewLifecycleOwner, Observer { notes ->
            this.notes = notes
            adapter.setData(notes)
        })


        val addBtn: FloatingActionButton = binding.floatingActionButton
        addBtn.setOnClickListener(){
            findNavController().navigate(R.id.action_notesFragment_to_addNoteFragment)
        }

        setHasOptionsMenu(true)

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_menu){
            deleteAllNotes(userViewModel.id)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllNotes(id: Long?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Да"){ _, _ ->
            noteViewModel.deleteAllNotes(id)
            Toast.makeText(requireContext(), "Удалены все заметки", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("Нет"){ _, _ ->

        }
        builder.setTitle("Удалить все заметки?")
        builder.create().show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохранение списка заметок в Bundle при уничтожении фрагмента
        outState.putParcelableArrayList("notes", ArrayList(notes))
    }
}