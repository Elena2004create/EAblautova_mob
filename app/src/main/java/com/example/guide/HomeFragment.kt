package com.example.guide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var placesBtn : Button? = view?.findViewById(R.id.placeBtn)

        placesBtn?.setOnClickListener {
            var fragment = PlacesFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .commit()
        }

        var notesBtn : Button? = view?.findViewById(R.id.noteBtn)
        notesBtn?.setOnClickListener {
            var fragment = NavHostFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame, fragment)
                .commit()
        }


        return view
    }

}