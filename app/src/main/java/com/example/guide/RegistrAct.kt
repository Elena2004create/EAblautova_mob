package com.example.guide

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class RegistrAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registr)

        val userLogin: EditText = findViewById(R.id.user_login)
        val userPass: EditText = findViewById(R.id.user_pass)
        val registrBtn: Button = findViewById(R.id.registrBtn)

        registrBtn.setOnClickListener {
            var login = userLogin.text.toString().trim()
            var pass = userPass.text.toString().trim()

            if (login == "" || pass == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()

            else {
                var user = User(login, pass)

                var db = Db(this, null)
                db.addUser(user)
                Toast.makeText(this, "Пользователь добавлен", Toast.LENGTH_LONG).show()

                userLogin.text.clear()
                userPass.text.clear()
            }
        }
        val authBtn: TextView = findViewById(R.id.link_to_auth)

        authBtn.setOnClickListener {
            val intent = Intent(this, OpenActivity::class.java)
            startActivity(intent)
        }
    }
}