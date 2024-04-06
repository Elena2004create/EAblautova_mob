package com.example.guide

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var homefr = HomeFragment()

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