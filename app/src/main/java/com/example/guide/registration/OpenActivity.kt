package com.example.guide.registration


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.guide.MainActivity
import com.example.guide.databinding.OpenActivityBinding
import com.example.guide.models.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OpenActivity : AppCompatActivity() {

    lateinit var userViewModel: UserViewModel
    lateinit var binding: OpenActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = OpenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.apply {
            registrBtn.setOnClickListener {
                registrActOpen()
            }
            enterBtn.setOnClickListener {

                var login = userLogin.text.toString().trim()
                var pass = userPass.text.toString().trim()

                if (login == "" || pass == "")
                    Toast.makeText(this@OpenActivity, "Не все поля заполнены", Toast.LENGTH_LONG).show()

                else {
                    lifecycleScope.launch {
                        val user = withContext(Dispatchers.IO) {
                            userViewModel.getUserId(login, pass)

                        }

                        if (user != null){
                            Toast.makeText(this@OpenActivity, "Пользователь авторизован", Toast.LENGTH_LONG).show()
                            userViewModel.id = user
                            userLogin.text.clear()
                            userPass.text.clear()
                            val intent = Intent(this@OpenActivity, MainActivity::class.java).apply {
                                putExtra("userId", userViewModel.id)
                            }
                            startActivity(intent)

                        }
                        else {
                            Toast.makeText(this@OpenActivity, "Пользователь не авторизован", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            passText.setOnClickListener(){
                val dialog = PassDialogFragment(this@OpenActivity)
                dialog.show(supportFragmentManager, null)
            }
        }
    }

    fun registrActOpen(){
        val intent = Intent(this, RegistrAct::class.java)
        startActivity(intent)
    }
}