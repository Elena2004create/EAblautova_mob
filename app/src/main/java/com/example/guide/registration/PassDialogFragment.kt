package com.example.guide.registration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.guide.databinding.LayoutPassDialogBinding
import com.example.guide.models.UserViewModel
import kotlinx.coroutines.launch

class PassDialogFragment(context: Context) : DialogFragment() {
    private lateinit var binding: LayoutPassDialogBinding
    lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPassDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as? OpenActivity
        // Получаем экземпляр ViewModel из OpenActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }

        binding.apply {
            shPassBtn.setOnClickListener(){
                val login = insertLogin.text.toString().trim()
                lifecycleScope.launch() {
                    val password = userViewModel.getUserPassByLogin(login)
                    if (password != null) {
                        shPass.text = password
                    } else {
                        // Пароль не найден, показать сообщение об ошибке
                        Toast.makeText(context, "Пароль не найден", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }
}