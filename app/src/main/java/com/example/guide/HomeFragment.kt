package com.example.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.guide.databinding.FragmentAddNoteBinding
import com.example.guide.databinding.FragmentEditUserProfileBinding
import com.example.guide.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

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

    lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        retainInstance = true
        return binding.root
    }

        // Inflate the layout for this fragment
        /*
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

         */
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val activity = activity as? MainActivity
            // Получаем экземпляр ViewModel из MainActivity
            if (activity != null) {
                userViewModel = activity.userViewModel
            }

            binding.apply {
                lifecycleScope.launch {
                    userName.setText(userViewModel.getUserLogin(userViewModel.id))
                }


                placeBtn.setOnClickListener {
                    var fragment = PlacesFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                noteBtn.setOnClickListener {
                    var fragment = NavHostFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                editBtn.setOnClickListener {
                    var fragment = EditUserProfileFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

}