package com.example.guide

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() {
    lateinit var userViewModel: UserViewModel

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
        val addBtn: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        addBtn.setOnClickListener(){
            findNavController().navigate(R.id.action_notesFragment_to_addNoteFragment)
            Log.d("1", "work?")
        }
        //Log.d("1", userViewModel.id.toString())



        return view
    }


}