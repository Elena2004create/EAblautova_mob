package com.example.guide.places

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guide.MainActivity
import com.example.guide.R
import com.example.guide.data.Place
import com.example.guide.databinding.FragmentPlacesBinding
import com.example.guide.models.PlaceViewModel
import com.example.guide.models.UserViewModel

class PlacesFragment : Fragment(), PlacesAdapter.Listener {

    lateinit var userViewModel: UserViewModel
    lateinit var placeViewModel: PlaceViewModel
    var places: MutableList<Place> = emptyList<Place>().toMutableList()
    lateinit var adapter: PlacesAdapter
    lateinit var binding: FragmentPlacesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlacesBinding.inflate(layoutInflater)

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

        val toolbar = binding.toolbar

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        adapter = PlacesAdapter(this)
        if (savedInstanceState != null) {
            places = savedInstanceState.getParcelableArrayList("places") ?: emptyList<Place>().toMutableList()
            adapter.setData(places)
        }

        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)

        placeViewModel.getAllPlaces(userViewModel.id).observe(viewLifecycleOwner, Observer { places ->
            this.places = places.toMutableList()
            adapter.setData(places.toMutableList())
        })

        setHasOptionsMenu(true)

    }


    private fun deleteAllPlaces(id: Long?) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Да"){ _, _ ->
            placeViewModel.deleteAllPlaces(id)
            Toast.makeText(requireContext(), "Удалены все места", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("Нет"){ _, _ ->

        }
        builder.setTitle("Удалить все места?")
        builder.create().show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_menu){
            deleteAllPlaces(userViewModel.id)
        }
        return super.onOptionsItemSelected(item)
    }

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