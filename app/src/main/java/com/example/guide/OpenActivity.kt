package com.example.guide


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OpenActivity : AppCompatActivity() {

    lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.open_activity)

        val enterBtn : Button = findViewById(R.id.enter)
        val userLogin: EditText = findViewById(R.id.user_login)
        val userPass: EditText = findViewById(R.id.user_pass)

        /*enterBtn.setOnClickListener {
            mainActOpen()
        }
         */

        //dao.insert(User(1, "sjewyr", "123"))

//       lifecycleScope.launch {
//          var db = App.database
//          var userdao = db.userDao()
//           //userdao.delete(User(1, "sjewyr", "123"))
//           Log.e("1", userdao.getAll().toString())
//      }


        val registrBtn : Button = findViewById(R.id.registrBtn)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        registrBtn.setOnClickListener {
            registrActOpen()
        }
        enterBtn.setOnClickListener {

            var login = userLogin.text.toString().trim()
            var pass = userPass.text.toString().trim()

            if (login == "" || pass == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()

            else {

                lifecycleScope.launch {
                    val user = withContext(Dispatchers.IO) {
                        val db = App.database
                        val userDao = db.userDao()
                        userDao.getUserId(login, pass)
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


                //var db = AppDatabase.getInstance(this)
                /*
                var db = App.database
                var userdao = db.userDao()
                Log.e("1", userdao.getAll().toString())

                val isAuth = userDao.getUser(login, pass)
                Log.d("db", userDao.getUsers().toString())
                if (isAuth?.toInt() != 1){
                    Toast.makeText(this, "Пользователь авторизован", Toast.LENGTH_LONG).show()
                    userLogin.text.clear()
                    userPass.text.clear()
                    mainActOpen()

                }
                else {
                    Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_LONG).show()
                }
                */

            }
        }
    }

    suspend fun mainActOpen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun registrActOpen(){
        val intent = Intent(this, RegistrAct::class.java)
        startActivity(intent)
    }
}