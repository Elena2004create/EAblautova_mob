package com.example.guide

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.guide.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

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

                adviceBtn.setOnClickListener(){
                    var fragment = AdviceFragment()
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

    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}