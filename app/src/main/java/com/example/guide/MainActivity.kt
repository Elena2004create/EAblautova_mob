package com.example.guide

import android.content.Context
import android.os.Bundle
import android.se.omapi.Session
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.LocationManager

class MainActivity : AppCompatActivity(){

    lateinit var userViewModel: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Получите логин и пароль пользователя
        userViewModel.id = intent.getLongExtra("userId", -1)
        //Log.d("1", userViewModel.id.toString())

        if (savedInstanceState == null) {
            // Создаем экземпляры фрагментов только если активити создается впервые
            var homeFragment = HomeFragment()
            var mapFragment = MapFragment()
            setFragment(homeFragment)
            Log.d("notes", "че за х")
        }
        else {
            // Восстанавливаем текущий фрагмент из savedInstanceState, если он был сохранен
            val currentFragmentTag = savedInstanceState.getString("currentFragmentTag")
            val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            if (currentFragment != null) {
                setFragment(currentFragment)
            }
            Log.d("notes", "че за х")
        }

        val homeBtn: ImageButton = findViewById(R.id.homeBtn)
        val mapBtn: ImageButton = findViewById(R.id.mapBtn)

        val homeFragment = HomeFragment()

        setFragment(homeFragment)

        homeBtn.setOnClickListener(){
            setFragment(homeFragment)
        }

        mapBtn.setOnClickListener(){
            val mapFragment = MapFragment()
            setFragment(mapFragment)
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем тег текущего фрагмента, чтобы мы могли восстановить его при повороте экрана
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame)
        if (currentFragment != null) {
            outState.putString("currentFragmentTag", currentFragment.tag)
        }
    }
    fun setFragment(fragment: Fragment){
        val ft : FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame, fragment)
        ft.commit()
    }
}