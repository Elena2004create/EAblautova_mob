package com.example.guide

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.guide.databinding.FragmentEditUserProfileBinding
import com.example.guide.databinding.FragmentMapBinding
import kotlinx.coroutines.launch

class EditUserProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditUserProfileBinding
    lateinit var userViewModel: UserViewModel
    //private  val args by navArgs<EditUserProfileFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentEditUserProfileBinding.inflate(layoutInflater)

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
                editLogin.setText(userViewModel.getUserLogin(userViewModel.id))
                editPass.setText(userViewModel.getUserPass(userViewModel.id))
            }

            //editPass.setText(args.currentUser.pass)

            updateUserButton.setOnClickListener(){
                val login = editLogin.text.toString()
                val pass = editPass.text.toString()

                if (login == "" || pass == "")
                    Toast.makeText(requireContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show()
                else {
                    val updateUser = User(userViewModel.id!!, login, pass)
                    userViewModel.updateUser(updateUser)
                    Toast.makeText(requireContext(), "Данные изменены", Toast.LENGTH_LONG).show()
                }
            }
        }

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