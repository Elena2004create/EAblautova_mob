package com.example.guide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class OpenActivity : AppCompatActivity() {
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

        val registrBtn : Button = findViewById(R.id.registrBtn)

        registrBtn.setOnClickListener {
            registrActOpen()
        }

        enterBtn.setOnClickListener {
            var login = userLogin.text.toString().trim()
            var pass = userPass.text.toString().trim()

            if (login == "" || pass == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()

            else {
                var user = User(login, pass)

                var db = Db(this, null)
                val isAuth = db.getUser(login, pass)
                if (isAuth){
                    Toast.makeText(this, "Пользователь авторизован", Toast.LENGTH_LONG).show()
                    userLogin.text.clear()
                    userPass.text.clear()
                    mainActOpen()
                }
                else {
                    Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_LONG).show()
                }


            }
        }
    }

    fun mainActOpen(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun registrActOpen(){
        val intent = Intent(this, RegistrAct::class.java)
        startActivity(intent)
    }
}