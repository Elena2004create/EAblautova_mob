package com.example.guide

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.guide.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


class HomeFragment : Fragment(), MapFragment.userLocationListener {

    lateinit var userViewModel: UserViewModel
    private lateinit var binding: FragmentHomeBinding
    private val API_KEY = "5def7490-194b-440a-94ac-f580561cf26a"
    private val locationViewModel: LocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        retainInstance = true
        return binding.root
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val activity = activity as? MainActivity
            // Получаем экземпляр ViewModel из MainActivity
            if (activity != null) {
                userViewModel = activity.userViewModel
            }

            binding.apply {
                lifecycleScope.launch {
                    userName.setText(userViewModel.getUserLogin(userViewModel.id))
                }


                placeBtn.setOnClickListener {
                    var fragment = PlacesFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                noteBtn.setOnClickListener {
                    var fragment = NavHostFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                editBtn.setOnClickListener {
                    var fragment = EditUserProfileFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                adviceBtn.setOnClickListener(){
                    var fragment = AdviceFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, fragment)
                        .commit()
                }

                exitBtn.setOnClickListener(){
                    val intent = Intent(requireContext(), OpenActivity::class.java)
                    startActivity(intent)
                }
                if (locationViewModel.userCountry == "" || locationViewModel.userCity == ""){
                    location.setText("Местоположение не определено")
                }
                else{
                    location.setText("${locationViewModel.userCountry}, ${locationViewModel.userCity}")
                }


            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


    private fun getLocationInfo(latitude: Double, longitude: Double) {
        val client = OkHttpClient()
        val url = "https://geocode-maps.yandex.ru/1.x/?format=json&geocode=$longitude,$latitude&apikey=$API_KEY"
        val request: Request = Request.Builder()
            .url(url)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    if (jsonResponse != null) {
                        parseLocationInfo(jsonResponse)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseLocationInfo(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)
            val response = jsonObject.getJSONObject("response")
            val geoObjectCollection = response.getJSONObject("GeoObjectCollection")
            val featureMember = geoObjectCollection.getJSONArray("featureMember")
            if (featureMember.length() > 0) {
                val geoObject = featureMember.getJSONObject(0).getJSONObject("GeoObject")
                val metaDataProperty = geoObject.getJSONObject("metaDataProperty")
                val geocoderMetaData = metaDataProperty.getJSONObject("GeocoderMetaData")
                val address = geocoderMetaData.getJSONObject("Address")
                val components = address.getJSONArray("Components")
                var country = ""
                var city = ""
                for (i in 0 until components.length()) {
                    val component = components.getJSONObject(i)
                    val kind = component.getString("kind")
                    if (kind == "country") {
                        country = component.getString("name")
                    } else if (kind == "locality") {
                        city = component.getString("name")
                    }
                }
                locationViewModel.userCountry = country
                locationViewModel.userCity = city
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun userLocation(latitude: Double, longitude: Double) {
        if (latitude != 0.0 && longitude != 0.0){
            getLocationInfo(latitude, longitude)
        }
    }
}