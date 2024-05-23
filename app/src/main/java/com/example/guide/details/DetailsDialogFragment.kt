package com.example.guide.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.guide.MainActivity
import com.example.guide.data.Place
import com.example.guide.databinding.LayoutDetailsDialogBinding
import com.example.guide.models.LocationViewModel
import com.example.guide.models.PlaceViewModel
import com.example.guide.models.UserViewModel
import com.example.guide.utils.goneOrRun
import kotlinx.coroutines.launch

class DetailsDialogFragment : DialogFragment() {
    private lateinit var binding: LayoutDetailsDialogBinding

    var listener: PlaceSelectionListener? = null

    lateinit var locationViewModel: LocationViewModel

    lateinit var userViewModel: UserViewModel
    lateinit var placeViewModel: PlaceViewModel

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDetailsDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)

        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        val activity = activity as? MainActivity
        // Получаем экземпляр ViewModel из MainActivity
        if (activity != null) {
            userViewModel = activity.userViewModel
        }
        Log.d("1", userViewModel.id.toString())

        viewModel.uiState()?.let {
            binding.apply {

                currentLatitude = it.location!!.latitude
                currentLongitude = it.location.longitude

                textTitle.text = it.title
                textSubtitle.text = it.descriptionText
                textPlace.text = "${it.location?.latitude}, ${it.location?.longitude}"
                /*textUri.goneOrRun(it.uri) {
                    text = it
                }*/

                when (val state = it.typeSpecificState) {
                    is TypeSpecificState.Business -> {
                        layoutBusinessInfo.isVisible = true
                        textType.text = "Организация:"
                        textBusinessName.text = state.name
                        textBusinessWorkingHours.goneOrRun(state.workingHours) {
                            text = it
                        }
                        textBusinessCategories.text = state.categories
                        textBusinessPhones.text = state.phones
                        textBusinessLinks.goneOrRun(state.link) {
                            text = it
                        }
                    }
                    is TypeSpecificState.Toponym -> {
                        layoutToponymInfo.isVisible = true
                        textType.text = "Топоним:"
                        textToponymAddress.text = state.address
                    }
                    TypeSpecificState.Undefined -> {
                        textType.isVisible = false
                    }
                }

                addPlaceBtn.setOnClickListener() {
                    var title = textTitle.text.toString().trim()
                    var text = textSubtitle.text.toString().trim()

                    lifecycleScope.launch {
                        placeViewModel.addPlace(Place(name = title, address = text, userId = userViewModel.id))
                        Toast.makeText(requireContext(), "Место добавлено", Toast.LENGTH_LONG)
                            .show()
                    }

                }
                routeBtn.setOnClickListener(){
                    locationViewModel.latitude = currentLatitude
                    locationViewModel.longitude = currentLongitude
                    listener?.onPlaceSelected(currentLatitude, currentLongitude)
                    dismiss()

                }

            }
        }
    }

    interface PlaceSelectionListener {
        fun onPlaceSelected(latitude: Double, longitude: Double)
    }

}
