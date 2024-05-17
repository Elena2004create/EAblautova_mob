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

        /*val userLogin: EditText = findViewById(R.id.user_login)
        val userPass: EditText = findViewById(R.id.user_pass)
        val registrBtn: Button = findViewById(R.id.registrBtn)*/
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
                    /*
                    var user = User(id = 1, login = login, pass = pass)

                    var db = Db(this, null)
                    db.addUser(user)
                    Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                    userLogin.text.clear()
                    userPass.text.clear()

                     */
                    /*lifecycleScope.launch {
                        var user = User(login = login, pass = pass)
                        userViewModel.addUser(user)

                        Toast.makeText(this@RegistrAct, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                        userLogin.text.clear()
                        userPass.text.clear()
                        }

                     */
                    /*lifecycleScope.launch {
                        val db = App.database
                        val userdao = db.userDao()
                        var user = User(login = login, pass = pass)
                        userdao.insert(user)
                        Log.e("1", userdao.getAll().toString())

                        Toast.makeText(this@RegistrAct, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                        userLogin.text.clear()
                        userPass.text.clear()
                    }

                     */


                    //var db = AppDatabase.getInstance(this)
                    /*
                    var db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
                    var userDao = db.userDao()
                    userDao.addUser(user)
                    Log.d("db", userDao.getUsers().toString())

                    Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                    userLogin.text.clear()
                    userPass.text.clear()

                     */


                }
            }

            linkToAuth.setOnClickListener {
                val intent = Intent(this@RegistrAct, OpenActivity::class.java)
                startActivity(intent)
            }
        }

    }
}