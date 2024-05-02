package com.example.guide

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(){

    lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var homefr = HomeFragment()

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Получите логин и пароль пользователя
        userViewModel.id = intent.getLongExtra("userId", -1)
        Log.d("1", userViewModel.id.toString())

        var ft : FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame, homefr)
        ft.commit()
        val btn1 : Button? = homefr.view?.findViewById(R.id.placeBtn)

        btn1?.setOnClickListener {
            var fragment = PlacesFragment()
            var ft1 : FragmentTransaction = supportFragmentManager.beginTransaction()
            ft1.replace(R.id.frame, fragment)
            ft1.commit()
        }



    }
}