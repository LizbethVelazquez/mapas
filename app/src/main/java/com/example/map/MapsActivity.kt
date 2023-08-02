package com.example.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.map.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{
//Se declaran las variables
    private var name: String = ""
    private var oriLat: Double = 19.741667
    private var oriLng: Double = -96.820556
    private var desLat: Double = 0.0
    private var desLng: Double = 0.0
    private var apiKey = "AIzaSyA3Yo4tjViD4NdceoNFdZXVNNR9TQvhgvc"
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var binding: ActivityMapsBinding

    val REQUEST_CODE_LOCATION = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        name = intent.extras!!.getString("name").toString()
        desLat = intent.extras!!.getDouble("lat")
        desLng = intent.extras!!.getDouble("lng")

        title = "CENTRO - $name"

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }
    //Se genera el mapa
    private fun getNewMap(){
        
        mapFragment.getMapAsync {
            mMap = it
            val originLocation = LatLng(oriLat, oriLng)
            mMap.addMarker(MarkerOptions().position(originLocation).title("Centro"))
            val destinationLocation = LatLng(desLat, desLng)
            mMap.addMarker(MarkerOptions().position(destinationLocation).title("$name "))
            val urll = getDirectionURL(originLocation, destinationLocation, apiKey)

            GetDirection(urll).execute()
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
        }
    }

//Verificacion de aceptacion de los permisos(Localizacion)
    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::mMap.isInitialized) return
        if (isPermissionsGranted()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::mMap.isInitialized) return
        if(!isPermissionsGranted()){
            mMap.isMyLocationEnabled = false
            Toast.makeText(this@MapsActivity, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mMap.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
// Obtenemos la ubicacion de la persona
    override fun onMyLocationClick(p0: android.location.Location) {
        oriLat = p0.latitude
        oriLng = p0.longitude
        getNewMap()
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val loc = LatLng(oriLat,oriLng)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(loc).title("CENTRO"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18F))

        mMap.setOnMyLocationClickListener(this@MapsActivity)

        enableLocation()
    }
// Se traza la linea de la ruta en el mapa
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }


    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }


    class MapData {
        var routes = ArrayList<Routes>()
    }

    class Routes {
        var legs = ArrayList<Legs>()
    }

    class Legs {
        var distance = Distance()
        var duration = Duration()
        var end_address = ""
        var start_address = ""
        var end_location =Location()
        var start_location = Location()
        var steps = ArrayList<Steps>()
    }

    class Steps {
        var distance = Distance()
        var duration = Duration()
        var end_address = ""
        var start_address = ""
        var end_location =Location()
        var start_location = Location()
        var polyline = PolyLine()
        var travel_mode = ""
        var maneuver = ""
    }

    class Duration {
        var text = ""
        var value = 0
    }

    class Distance {
        var text = ""
        var value = 0
    }

    class PolyLine {
        var points = ""
    }

    class Location{
        var lat =""
        var lng =""
    }


}