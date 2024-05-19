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

        userViewModel.id = intent.getLongExtra("userId", -1)

        if (savedInstanceState == null) {

            var homeFragment = HomeFragment()
            var mapFragment = MapFragment()
            setFragment(homeFragment)
        }
        else {

            val currentFragmentTag = savedInstanceState.getString("currentFragmentTag")
            val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
            if (currentFragment != null) {
                setFragment(currentFragment)
            }
        }

        val homeBtn: ImageButton = findViewById(R.id.homeBtn)
        val mapBtn: ImageButton = findViewById(R.id.mapBtn)

        val homeFragment = HomeFragment()

        /*val mapFragment = MapFragment()
        mapFragment.setUserLocationListener(homeFragment)*/

        setFragment(homeFragment)

        homeBtn.setOnClickListener(){
            setFragment(homeFragment)
        }

        mapBtn.setOnClickListener(){
            val mapFragment = MapFragment()
            mapFragment.setUserLocationListener(homeFragment)
            setFragment(mapFragment)
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

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