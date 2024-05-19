package com.example.guide


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.guide.databinding.ActivityRegistrBinding
import com.example.guide.databinding.OpenActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrAct : AppCompatActivity() {
    lateinit var userViewModel: UserViewModel
    lateinit var binding: ActivityRegistrBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.apply {
            registrBtn.setOnClickListener {
                var login = userLogin.text.toString().trim()
                var pass = userPass.text.toString().trim()
                userViewModel.registerUser(login, pass)

                if (login == "" || pass == "")
                    Toast.makeText(this@RegistrAct, "Не все поля заполнены", Toast.LENGTH_LONG).show()

                else {
                    userViewModel.registrationResult.observe(this@RegistrAct, Observer { success ->
                        if (success) {

                            lifecycleScope.launch {
                                var user = User(login = login, pass = pass)
                                userViewModel.addUser(user)

                                Toast.makeText(this@RegistrAct, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                                userLogin.text.clear()
                                userPass.text.clear()

                                val intent = Intent(this@RegistrAct, OpenActivity::class.java)
                                startActivity(intent)
                            }

                        } else {
                            Toast.makeText(this@RegistrAct, "Такой логин уже занят", Toast.LENGTH_SHORT).show()
                        }
                    })


                }
            }

            linkToAuth.setOnClickListener {
                val intent = Intent(this@RegistrAct, OpenActivity::class.java)
                startActivity(intent)
            }
        }

    }
}