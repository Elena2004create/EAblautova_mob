package com.example.guide

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.provider.Settings
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.guide.databinding.FragmentMapBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.GeoObjectCollection
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.map.SizeChangedListener
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MapFragment : Fragment(), UserLocationObjectListener,  Session.SearchListener,
    DetailsDialogFragment.PlaceSelectionListener {
    //UserLocationObjectListener, Session.SearchListener, CameraListener, LocationListener
    //GeoObjectTapListener, MapObjectTapListener


    lateinit var mapView: MapView
    private var locationManager: LocationManager? = null
    lateinit var locationMapKit: UserLocationLayer
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session
    lateinit var text: String

    private var listener: DetailsDialogFragment.PlaceSelectionListener? = null

    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding
    private val map by lazy { binding.mapView.mapWindow.map }
    private val suggestAdapter = SuggestsListAdapter()
    private lateinit var editQueryTextWatcher: TextWatcher

    private lateinit var pLauncher: ActivityResultLauncher<String>


    private val locationViewModel: LocationViewModel by activityViewModels()


    private val drivingRouteListener = object : DrivingSession.DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            routes = drivingRoutes
            locationViewModel.routes = drivingRoutes
        }

        override fun onDrivingRoutesError(error: Error) {
            when (error) {
                is NetworkError -> requireContext().showToast("Ошибка подключения к интернету")
                else -> requireContext().showToast("Неизвестная ошибка")
            }
        }
    }


    private var routePoints = emptyList<Point>()
        set(value) {
            field = value
            onRoutePointsUpdated()
        }

    private var routes = emptyList<DrivingRoute>()
        set(value) {
            field = value
            onRoutesUpdated()
        }

    private lateinit var drivingRouter: DrivingRouter
    private var drivingSession: DrivingSession? = null
    private lateinit var placemarksCollection: MapObjectCollection
    private lateinit var routesCollection: MapObjectCollection

    var userLocation: userLocationListener? = null


    private val searchResultPlacemarkTapListener = MapObjectTapListener { mapObject, _ ->
        // Show details dialog on placemark tap.
        val selectedObject = (mapObject.userData as? GeoObject)
        SelectedObjectHolder.selectedObject = selectedObject
        val fragment = DetailsDialogFragment()
        fragment.listener = this
        fragment.show(childFragmentManager, null)
        true
    }

    private val cameraListener = CameraListener { _, _, reason, _ ->
        // Updating current visible region to apply research on map moved by user gestures.
        locationMapKit.resetAnchor()
        if (reason == CameraUpdateReason.GESTURES) {
            if (locationViewModel.points.isEmpty()){
                viewModel.setVisibleRegion(map.visibleRegion)
                if (locationViewModel.routes.isNotEmpty())
                    routes = locationViewModel.routes

            }
            else {
                if (locationViewModel.routes.isNotEmpty())
                    routes = locationViewModel.routes

            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationUpdated(p0: Location) {
            mapView.map.move(
                CameraPosition(Point(p0.position.latitude, p0.position.longitude), 15.0f, 0.0f, 0.0f))

            locationViewModel.userLatitude = p0.position.latitude
            locationViewModel.userLongitude = p0.position.longitude

            userLocation?.userLocation(locationViewModel.userLatitude, locationViewModel.userLongitude)

        }

        override fun onLocationStatusUpdated(p0: LocationStatus) {
            Log.d("LocationStatus", LocationStatus.values().toString())
            when (p0) {
                LocationStatus.NOT_AVAILABLE -> {
                    requireContext().showToast("Ошибка загрузки, проверьте подключение к интернету")
                }
                else -> {}
            }



            }
    }

    private val sizeChangedListener = SizeChangedListener { _, _, _ -> updateFocusRect() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(layoutInflater)
        retainInstance = true
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()

        MapKitFactory.initialize(requireContext())

        mapView = binding.mapView

        map.addCameraListener(cameraListener)
        viewModel.setVisibleRegion(map.visibleRegion)

        binding.mapView.mapWindow.addSizeChangedListener(sizeChangedListener)
        updateFocusRect()

        locationManager = MapKitFactory.getInstance().createLocationManager()

        locationMapKit = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

        locationManager!!.requestSingleUpdate(locationListener)

        locationMapKit.isVisible = true

        locationMapKit.setObjectListener(this)

        placemarksCollection = map.mapObjects
        routesCollection = map.mapObjects

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)

        binding.apply {
            listSuggests.adapter = suggestAdapter

            locationBtn.setOnClickListener() {
                locationManager!!.requestSingleUpdate(locationListener)
                userLocation?.userLocation(locationViewModel.userLatitude, locationViewModel.userLongitude)
                /*map.move(
                    CameraPosition(Point(locationViewModel.userLatitude, locationViewModel.userLongitude), 15.0f, 0.0f, 0.0f))

                 */

            }

            foodBtn.setOnClickListener {
                text = "где поесть"
                sumbitQuery(text)
            }
            landmarkBtn.setOnClickListener {
                text = "достопримечательности рядом"
                sumbitQuery(text)
            }
            hotelBtn.setOnClickListener {
                text = "гостинницы"
                sumbitQuery(text)
            }

            editQueryTextWatcher = editQuery.doAfterTextChanged { text ->
                if (text.toString() == viewModel.uiState.value.query) return@doAfterTextChanged
                viewModel.setQueryText(text.toString())
            }

            editQuery.setOnEditorActionListener { _, _, _ ->
                viewModel.startSearch()
                locationViewModel.routes = emptyList()
                locationViewModel.points.clear()
                true
            }

            routeBtn.setOnClickListener(){
                routePoints = emptyList()
                locationViewModel.latitude = 0.0
                locationViewModel.longitude = 0.0
                viewModel.reset()
                routes = emptyList()
                locationViewModel.points.clear()
                locationViewModel.routes = emptyList()
            }

        }

        viewModel.uiState
            .flowWithLifecycle(lifecycle)
            .onEach {

                suggestAdapter.items =
                    (it.suggestState as? SuggestState.Success)?.items ?: emptyList()

                if (it.suggestState is SuggestState.Error) {
                    requireContext().showToast("Ошибка загрузки, проверьте подключение к интернету")
                }


                val successSearchState = it.searchState as? SearchState.Success
                val searchItems = successSearchState?.items ?: emptyList()
                updateSearchResponsePlacemarks(searchItems)
                if (successSearchState?.zoomToItems == true) {
                    focusCamera(
                        searchItems.map { item -> item.point },
                        successSearchState.itemsBoundingBox
                    )
                }

                if (it.searchState is SearchState.Error) {
                    requireContext().showToast("Ошибка загрузки, проверьте подключение к интернету")
                }

                binding.apply {
                    editQuery.apply {
                        if (text.toString() != it.query) {
                            removeTextChangedListener(editQueryTextWatcher)
                            setText(it.query)
                            addTextChangedListener(editQueryTextWatcher)
                        }
                    }
                    /*searchStatus.text =
                        "Search: ${it.searchState.toTextStatus()}; Suggest: ${it.suggestState.toTextStatus()}"
                    searchBtn.isEnabled = it.query.isNotEmpty() && it.searchState == SearchState.Off
                    resetBtn.isEnabled =
                        it.query.isNotEmpty() || it.searchState !is SearchState.Off*/
                    editQuery.isEnabled = it.searchState is SearchState.Off
                }

            }
            .launchIn(lifecycleScope)

        viewModel.subscribeForSuggest().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        viewModel.subscribeForSearch().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)


        retainInstance = true
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()

    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    fun setUserLocationListener(listener: userLocationListener) {
        userLocation = listener
    }

    override fun onResume() {
        checkLocation()

        if (locationViewModel.routes.isNotEmpty() && locationViewModel.points.isNotEmpty())
            routes = locationViewModel.routes

        else if (locationViewModel.points.isNotEmpty() && locationViewModel.routes.isEmpty()){
            updateResponsePlacemarks(locationViewModel.points)
        }
        super.onResume()
    }

    private fun updateSearchResponsePlacemarks(items: List<SearchResponseItem>) {
        map.mapObjects.clear()

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.mapmark)

        items.forEach {
            map.mapObjects.addPlacemark().apply {
                geometry = it.point
                setIcon(imageProvider, IconStyle().apply { scale = 0.5f })
                addTapListener(searchResultPlacemarkTapListener)
                userData = it.geoObject
            }
        }


        if (locationViewModel.routes.isNotEmpty())
            routes = locationViewModel.routes
    }

    private fun updateResponsePlacemarks(items: MutableList<GeoObjectCollection.Item>) {

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.mapmark)

        items.forEach {
            map.mapObjects.addPlacemark().apply {
                geometry = it.obj!!.geometry[0].point!!
                setIcon(imageProvider, IconStyle().apply { scale = 0.5f })
                addTapListener(searchResultPlacemarkTapListener)
                userData = it.obj!!
            }
        }
    }


    private fun focusCamera(points: List<Point>, boundingBox: BoundingBox) {
        if (points.isEmpty()) return

        val position = if (points.size == 1) {
            map.cameraPosition.run {
                CameraPosition(points.first(), zoom, azimuth, tilt)
            }
        } else {
            map.cameraPosition(Geometry.fromBoundingBox(boundingBox))
        }

        map.move(position, Animation(Animation.Type.SMOOTH, 0.5f), null)
    }

    private fun updateFocusRect() {
        val horizontal = resources.getDimension(R.dimen.window_horizontal_padding)
        val vertical = resources.getDimension(R.dimen.window_vertical_padding)
        val window = binding.mapView.mapWindow

        window.focusRect = ScreenRect(
            ScreenPoint(horizontal, vertical),
            ScreenPoint(window.width() - horizontal, window.height() - vertical),
        )
    }



    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationMapKit.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )

        userLocationView.arrow.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.userlocation), IconStyle().apply { scale = 0.5f })

        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onSearchResponse(response: Response) {
        if (!isAdded) {
            // Фрагмент не прикреплен к контексту, просто выходим из метода
            return
        }

        val mapObjects: MapObjectCollection = mapView.map.mapObjects
        mapObjects.clear()
        locationViewModel.points.clear()
        locationViewModel.routes = emptyList()
        viewModel.reset()
            for (searchResult in response.collection.children){
                val resultLocation = searchResult.obj!!.geometry[0].point!!
                locationViewModel.points.add(searchResult)

                if(response != null){
                    mapObjects.addPlacemark().apply {
                        geometry = resultLocation
                        setIcon(ImageProvider.fromResource(requireContext(), R.drawable.mapmark), IconStyle().apply { scale = 0.5f })
                        addTapListener(searchResultPlacemarkTapListener)
                        userData = searchResult.obj!!
                    }

                }
            }
    }


    fun sumbitQuery(query: String){
        searchSession = searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions().apply { searchTypes = SearchType.BIZ.value }, this)
    }

    private fun onRoutePointsUpdated() {
        placemarksCollection.clear()

        if (routePoints.isEmpty()) {
            drivingSession?.cancel()
            routes = emptyList()
            return
        }

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.bullet)
        routePoints.forEach {
            placemarksCollection.addPlacemark().apply {
                geometry = it
                setIcon(imageProvider, IconStyle().apply {
                    scale = 0.5f
                    zIndex = 20f
                })
            }
        }

        if (routePoints.size < 2) return

        val requestPoints = buildList {
            add(RequestPoint(routePoints.first(), RequestPointType.WAYPOINT, null, null))
            addAll(
                routePoints.subList(1, routePoints.size - 1)
                    .map { RequestPoint(it, RequestPointType.VIAPOINT, null, null) })
            add(RequestPoint(routePoints.last(), RequestPointType.WAYPOINT, null, null))
        }

        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()

        drivingSession = drivingRouter.requestRoutes(
            requestPoints,
            drivingOptions,
            vehicleOptions,
            drivingRouteListener,
        )
    }

    private fun onRoutesUpdated() {
        routesCollection.clear()
        if (routes.isEmpty()) return

        routes.forEachIndexed { index, route ->
            routesCollection.addPolyline(route.geometry).apply {
                if (index == 0) styleMainRoute() else styleAlternativeRoute()
            }
        }
    }

    private fun PolylineMapObject.styleMainRoute() {
        zIndex = 10f
        setStrokeColor(ContextCompat.getColor(requireContext(), CommonColors.blue))
        strokeWidth = 5f
        outlineColor = ContextCompat.getColor(requireContext(), CommonColors.black)
        outlineWidth = 3f
    }

    private fun PolylineMapObject.styleAlternativeRoute() {
        zIndex = 5f
        setStrokeColor(ContextCompat.getColor(requireContext(), CommonColors.white))
        strokeWidth = 4f
        outlineColor = ContextCompat.getColor(requireContext(), CommonColors.black)
        outlineWidth = 2f
    }

    override fun onPlaceSelected(latitude: Double, longitude: Double) {
        routePoints = listOf(
            Point(latitude, longitude),
            Point(locationViewModel.userLatitude, locationViewModel.userLongitude)
        )

        locationViewModel.latitude = latitude
        locationViewModel.longitude = longitude
    }

    fun requestLocationPermission(){
        /*if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            locationManager?.requestSingleUpdate(locationListener)

         */

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
        return
        }


    fun checkLocation(){
        if(isLocationEnabled()){
            requestLocationPermission()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    fun permissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            Toast.makeText(activity, "Разрешение получено", Toast.LENGTH_LONG).show()
        }
    }

    fun checkPermission(){
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onSearchError(error: Error) {
        var errorMessage = "Ошибка подключения"
        if (error is RemoteError){
            errorMessage = "Беспроводная ошибка"
        }
        if (error is NetworkError){
            errorMessage = "Проблемы с интернетом"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }


    interface userLocationListener{
        fun userLocation(latitude: Double, longitude: Double)
    }
}

object SelectedObjectHolder {
    var selectedObject: GeoObject? = null
}






