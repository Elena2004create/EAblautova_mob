package com.example.guide

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
import com.yandex.mapkit.map.RotationType
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
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize


class MapFragment : Fragment(), UserLocationObjectListener,  Session.SearchListener,
    DetailsDialogFragment.PlaceSelectionListener {
    //UserLocationObjectListener, Session.SearchListener, CameraListener, LocationListener
    //GeoObjectTapListener, MapObjectTapListener


    lateinit var mapView: MapView
    private var locationManager: LocationManager? = null
    //private var locationListener: LocationListener? = null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    lateinit var locationMapKit: UserLocationLayer
    lateinit var search: EditText
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session
    lateinit var text: String

    private var listener: DetailsDialogFragment.PlaceSelectionListener? = null


    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding
    //private val map by lazy { binding.mapView.mapWindow.map }
    private lateinit var map: Map
    private val suggestAdapter = SuggestsListAdapter()
    private lateinit var editQueryTextWatcher: TextWatcher


    private val locationViewModel: LocationViewModel by activityViewModels()


    private val drivingRouteListener = object : DrivingSession.DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            routes = drivingRoutes
            locationViewModel.routes = drivingRoutes
        }

        override fun onDrivingRoutesError(error: Error) {
            when (error) {
                is NetworkError -> requireContext().showToast("Routes request error due network issues")
                else -> requireContext().showToast("Routes request unknown error")
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
                //sumbitQuery(text)
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

            Log.d("locationnnn", locationViewModel.userLatitude.toString())

        }

        override fun onLocationStatusUpdated(p0: LocationStatus) {

        }
    }

    private val sizeChangedListener = SizeChangedListener { _, _, _ -> updateFocusRect() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMapBinding.inflate(layoutInflater)
        requestLocationPermission()
        retainInstance = true
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MapKitFactory.initialize(requireContext())

        mapView = binding.mapView
        map = binding.mapView.mapWindow.map

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

            searchBtn.setOnClickListener { viewModel.startSearch() }
            resetBtn.setOnClickListener {
                viewModel.reset()
                routePoints = emptyList()
                locationViewModel.latitude = 0.0
                locationViewModel.longitude = 0.0
                routes = emptyList()
                locationViewModel.points.clear()
                locationViewModel.routes = emptyList()
            }

            locationBtn.setOnClickListener() {
                locationManager!!.requestSingleUpdate(locationListener)
                map.move(
                    CameraPosition(Point(locationViewModel.userLatitude, locationViewModel.userLongitude), 15.0f, 0.0f, 0.0f))

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
            Log.d("blablabla", locationViewModel.latitude.toString())

            /*if (locationViewModel.latitude != 0.0 && locationViewModel.longitude != 0.0)
                routePoints = listOf(
                    Point(latitude, longitude),
                    Point(locationViewModel.latitude, locationViewModel.longitude)
                )

             */


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
                    requireContext().showToast("Suggest error, check your network connection")
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
                    requireContext().showToast("Search error, check your network connection")
                }

                binding.apply {
                    editQuery.apply {
                        if (text.toString() != it.query) {
                            removeTextChangedListener(editQueryTextWatcher)
                            setText(it.query)
                            addTextChangedListener(editQueryTextWatcher)
                        }
                    }
                    Log.d("searchStatus", it.searchState.toTextStatus())
                    searchStatus.text =
                        "Search: ${it.searchState.toTextStatus()}; Suggest: ${it.suggestState.toTextStatus()}"
                    searchBtn.isEnabled = it.query.isNotEmpty() && it.searchState == SearchState.Off
                    resetBtn.isEnabled =
                        it.query.isNotEmpty() || it.searchState !is SearchState.Off
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
        Log.i("Lifecycle", "On Stop Called")
        requestLocationPermission()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()

    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        Log.i("Lifecycle", "On Stop Called")
        super.onStop()
    }


    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохранение списка точек маршрута при сворачивании фрагмента
        outState.putParcelable("routePoints", (locationViewModel))

    }

     */

    override fun onResume() {

        if (locationViewModel.routes.isNotEmpty() && locationViewModel.points.isNotEmpty())
            routes = locationViewModel.routes

        else if (locationViewModel.points.isNotEmpty() && locationViewModel.routes.isEmpty()){
            updateResponsePlacemarks(locationViewModel.points)
        }
        super.onResume()
        Log.i("Lifecycle", "On Resume Called")
    }

    // При сворачивании окна сначала вызывается функция onPause
    override fun onPause() {
        super.onPause()
        Log.i("Lifecycle", "On Pause Called")
    }


    // Activity закрылось полностью, это произошло либо по выбору пользователя, либо через код - вызвав функцию finish(), либо не хватило свободной оперативной памяти для открытого приложения и ОС Android очистила память удалив оттуда свернутые приложения.
    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle", "On Destroy Called")
    }
    /*
    override fun onDestroy() {
        placemarksCollection.clear()
        routesCollection.clear()
        super.onDestroy()
    }

     */


    private fun updateSearchResponsePlacemarks(items: List<SearchResponseItem>) {
        map.mapObjects.clear()
        //locationViewModel.points.clear()

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.mapmark)
        /*mapView.map.move(
            CameraPosition(Point(locationViewModel.userLatitude, locationViewModel.userLongitude), 15.0f, 0.0f, 0.0f))
         */

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
        //map.mapObjects.clear()

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

        Log.d("mapview", mapView.width.toString())
        userLocationView.arrow.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.userlocation), IconStyle().apply { scale = 0.5f })


        //locationViewModel.userLatitude = userLocationView.arrow.geometry.latitude
        //locationViewModel.userLongitude = userLocationView.arrow.geometry.longitude

        /*
        Log.d("beeeee", locationViewModel.userLatitude.toString())
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon("icon", ImageProvider.fromResource(requireContext(), R.drawable.mapmark), IconStyle().
        setAnchor(PointF(0f, 0f)).setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(1f))

        picIcon.setIcon("pin", ImageProvider.fromResource(requireContext(), R.drawable.nothing), IconStyle().
        setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE).setZIndex(1f).setScale(0.5f))

         */


        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }

    override fun onSearchResponse(response: Response) {
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
        Log.d("points", locationViewModel.points.toString())


    }


    fun sumbitQuery(query: String){
        searchSession = searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions().apply { searchTypes = SearchType.BIZ.value }, this)
        Log.d("query", query)
    }

    /*override fun onPlaceSelected() {
        routePoints = listOf(
            Point(59.929576, 30.291737),
            Point(59.954093, 30.305770)
            /*listOf(
            Point(latitude, longitude),
            Point(locationViewModel.latitude, locationViewModel.longitude),

             */
        )
        Log.d("ерунда", routePoints[1].latitude.toString())
    }

     */



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
            /*listOf(
            Point(latitude, longitude),
            Point(locationViewModel.latitude, locationViewModel.longitude),

             */
        )

        locationViewModel.latitude = latitude
        locationViewModel.longitude = longitude
        Log.d("ерунда", routePoints[0].latitude.toString())
    }

    fun requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            locationManager?.requestSingleUpdate(locationListener)
            return
        }
    }


    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная ошибка"
        if (error is RemoteError){
            errorMessage = "Беспроводная ошибка"
        }
        if (error is NetworkError){
            errorMessage = "Проблемы с интернетом"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }
}


/*
        var view = inflater.inflate(R.layout.fragment_map, container, false)

        retainInstance = true

        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude") ?: 0.0
            longitude = savedInstanceState.getDouble("longitude")
            Log.d("sssssh", latitude.toString())
        }
        Log.d("sssssh", latitude.toString())



        MapKitFactory.initialize(requireContext())
        mapView = view.findViewById(R.id.mapView)


        var locationBtn: Button = view.findViewById(R.id.locationBtn)

        var foodBtn: Button = view.findViewById(R.id.foodBtn)
        text = "где поесть"

        requestLocationPermission()
        Log.d("locationnnnn", "")

        foodBtn.setOnClickListener(){
            sumbitQuery(text)
        }

        locationManager = MapKitFactory.getInstance().createLocationManager()

        locationMapKit = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)

        //locationManager!!.requestSingleUpdate(this)
        
        locationBtn.setOnClickListener(){

            locationManager!!.requestSingleUpdate(this)
            /*
            Log.d("locationnnnn", "")
            locationManager?.requestSingleUpdate(object : LocationListener {

                override fun onLocationUpdated(p0: Location) {
                    mapView.map.move(
                        CameraPosition(Point(p0.position.latitude, p0.position.longitude), 15.0f, 0.0f, 0.0f))
                    /*latitude = p0.position.latitude
                    longitude = p0.position.longitude

                    saveCoordinates(latitude, longitude)

                     */


                    Log.d("locationnnnn", p0.position.latitude.toString())

                }

                override fun onLocationStatusUpdated(p0: LocationStatus) {
                    showMessage(p0.toString())
                    Log.d("locationnnnn", p0.toString())

                }
            })
            */
        }





        /*mapView.map.move(
            CameraPosition(Point(0.0, 0.0), 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f), null)


         */

        locationMapKit.isVisible = true
        //mapView.map.move(CameraPosition(Point(90.0, 80.0), 15.0f, 0.0f, 0.0f))
        Log.d("shutup", latitude.toString())
        locationMapKit.setObjectListener(this)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapView.map.addCameraListener(this)

        //mapView.map.addTapListener(this)



        /*

        requestLocationPermission()

        var userLocation = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocation.isVisible = true

         */


        return view
    }

    fun sumbitQuery(query: String){
        searchSession = searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions().apply { searchTypes = SearchType.BIZ.value }, this)
        Log.d("query", query)
    }


    private fun saveCoordinates(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mapView = view.findViewById<MapView>(R.id.mapView)

    }

     */

    override fun onSaveInstanceState(outState: Bundle) {

        // Сохранение списка заметок в Bundle при уничтожении фрагмента
        outState.putDouble("latitude", latitude)
        outState.putDouble("longitude", longitude)
        super.onSaveInstanceState(outState)
    }


    fun requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            return
        }
    }

    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        locationListener?.let { locationManager?.unsubscribe(it) }
        super.onStart()
        Log.d("locationnnnn", "start")
    }

    override fun onPause() {
        locationListener?.let { locationManager?.unsubscribe(it) }
        super.onPause()
        Log.d("locationnnnn", "pause")
    }
    override fun onResume() {
        locationListener?.let { locationManager?.unsubscribe(it) }
        super.onResume()
        Log.d("locationnnnn", "resume")
    }


    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        locationListener?.let { locationManager?.unsubscribe(it) }
        super.onStop()
        Log.d("locationnnnn", "stop")
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationMapKit.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )
        Log.d("mapview", mapView.width.toString())
        userLocationView.arrow.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.userlocation))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon("icon", ImageProvider.fromResource(requireContext(), R.drawable.mapmark), IconStyle().
        setAnchor(PointF(0f, 0f)).setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(1f))

        picIcon.setIcon("pin", ImageProvider.fromResource(requireContext(), R.drawable.nothing), IconStyle().
        setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE).setZIndex(1f).setScale(0.5f))


        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {

    }




    override fun onSearchResponse(response: Response) {
        val mapObjects: MapObjectCollection = mapView.map.mapObjects
        mapObjects.clear()
        for (searchResult in response.collection.children){
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            Log.d("result", searchResult.obj!!.geometry[0].toString())
            Log.d("response", response.collection.children.toString())
            if(response != null){
                    mapObjects.addPlacemark().apply {
                        geometry = resultLocation
                        setIcon(ImageProvider.fromResource(requireContext(), R.drawable.mapmark))
                        addTapListener(searchResultPlacemarkTapListener)
                        userData = searchResult.obj!!
                }
                //var metadata = searchResult.obj!!.metadataContainer.getItem(BusinessObjectMetadata::class.java)
                //Log.d("searchResult", placeMap[searchResult.obj!!].toString())
                Log.d("mark", mapObjects.toString())
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная ошибка"
        if (error is RemoteError){
            errorMessage = "Беспроводная ошибка"
        }
        if (error is NetworkError){
            errorMessage = "Проблемы с интернетом"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished){
            sumbitQuery(text)
        }
    }

    override fun onLocationUpdated(p0: Location) {
        mapView.map.move(
            CameraPosition(Point(p0.position.latitude, p0.position.longitude), 15.0f, 0.0f, 0.0f))
    }

    override fun onLocationStatusUpdated(p0: LocationStatus) {
        showMessage(p0.toString())
        Log.d("locationnnnn", p0.toString())
    }

    /*
    override fun onObjectTap(p0: GeoObjectTapEvent): Boolean {

        val tappedGeoObject = p0.geoObject

        //val businessObjectMetadata = tappedGeoObject.metadataContainer.getItem( BusinessObjectMetadata::class.java)
        /*Log.d("searchResult",
            placeMap.contains(tappedGeoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java).objectId)
                .toString()
        )

         */
            //Log.d("tappedGeoObject", businessObjectMetadata?.address.toString())

            // Здесь вы можете получить информацию о нажатом объекте и показать её пользователю
            val objectName = tappedGeoObject.name ?: "Unknown"
            val objectAddr = placeMap[tappedGeoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java).objectId]?.address?.formattedAddress.toString() ?: "Unknown"

            // Ваш код для отображения информации о нажатом объекте
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.place_dialog, null)
            val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView).setTitle("Информация о месте")

            val placeName = dialogView.findViewById<TextView>(R.id.placeName)
            val placeAddress = dialogView.findViewById<TextView>(R.id.placeAddress)
            placeName.text = objectName
            placeAddress.text = objectAddr

            val dialog = dialogBuilder.create()
            dialog.show()

        // Возвращаем true, чтобы показать, что событие обработано
        return true
    }



    override fun onMapObjectTap(p0: MapObject, p1: Point): Boolean {
        TODO("Not yet implemented")
    }


     */

}




 */


object SelectedObjectHolder {
    var selectedObject: GeoObject? = null
}






