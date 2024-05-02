package com.example.guide

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guide.MainActivity
import com.example.guide.NoteViewModel
import com.example.guide.R
import com.example.guide.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() {
    lateinit var userViewModel: UserViewModel
    lateinit var noteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val adapter = NotesAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        noteViewModel.getAllNotes(userViewModel.id).observe(viewLifecycleOwner, Observer {note ->
            adapter.setData(note)
        })


        val addBtn: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        addBtn.setOnClickListener(){
            findNavController().navigate(R.id.action_notesFragment_to_addNoteFragment)
            Log.d("1", "work?")
        }
        //Log.d("1", userViewModel.id.toString())

        setHasOptionsMenu(true)


        return view
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


}