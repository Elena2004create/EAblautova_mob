package com.example.guide

import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlacesFragment : Fragment(), PlacesAdapter.Listener {

    lateinit var userViewModel: UserViewModel
    lateinit var placeViewModel: PlaceViewModel
    var places: MutableList<Place> = emptyList<Place>().toMutableList()
    lateinit var adapter: PlacesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_places, container, false)

        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        adapter = PlacesAdapter(this)
        if (savedInstanceState != null) {
            places = savedInstanceState.getParcelableArrayList("places") ?: emptyList<Place>().toMutableList()
            adapter.setData(places)
            //Log.d("notes", notes[0].title)
        }


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Получение данных из savedInstanceState, если они есть




        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)
        /*
        noteViewModel.getAllNotes(userViewModel.id).observe(viewLifecycleOwner, Observer {note ->
            adapter.setData(note)
        })

         */

        placeViewModel.getAllPlaces(userViewModel.id).observe(viewLifecycleOwner, Observer { places ->
            this.places = places.toMutableList()
            adapter.setData(places.toMutableList())
            //Log.d("notes", notes[0].text)
        })


        //Log.d("1", userViewModel.id.toString())

        setHasOptionsMenu(true)


        // Получите логин и пароль пользователя


        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_menu){

            if (item.itemId == R.id.delete_menu){

            }
            return super.onOptionsItemSelected(item)

        }
        return super.onOptionsItemSelected(item)
    }

    /*
    private fun deletePlace() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Да"){ _, _ ->
            placeViewModel.deletePlace()
            Toast.makeText(requireContext(), "Удалена заметка: ${args.currentNote.title}", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateNoteFragment_to_notesFragment)
        }
        builder.setNegativeButton("Нет"){ _, _ ->

        }
        builder.setTitle("Удалить ${args.currentNote.title}?")
        builder.create().show()
    }

     */

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохранение списка заметок в Bundle при уничтожении фрагмента
        outState.putParcelableArrayList("places", ArrayList(places))
    }

    override fun onClick(place: Place) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Да"){ _, _ ->
            placeViewModel.deletePlace(place)
            Toast.makeText(requireContext(), "Удалено место: ${place.name}", Toast.LENGTH_LONG).show()

        }
        builder.setNegativeButton("Нет"){ _, _ ->

        }
        builder.setTitle("Удалить ${place.name}?")
        builder.create().show()
    }

}