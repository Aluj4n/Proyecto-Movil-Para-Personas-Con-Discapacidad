package com.example.proyectomovilparapersonascondiscapacidad

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.model.Place

class MapasActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables para guardar la selección actual
    private var lastSelectedLatLng: LatLng? = null
    private var lastSelectedName: String = "Ubicación seleccionada"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapas)

        // 1. Inicializar Places (Asegúrate de que la API KEY sea válida y tenga PLACES API activada)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDR33k0gSWd0KOHk0vU04xOIS9xfmx2QRM")
        }

        // 2. Obtener el fragmento
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // 3. Configurar qué datos pedir (IMPORTANTE: Si pides campos de más y no tienes facturación, fallará)
        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        ))

        // 4. Filtro por país (ayuda a que cargue más rápido y sea más preciso)
        autocompleteFragment.setCountry("PE")

        // 5. Manejar la selección
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                if (latLng != null && ::mMap.isInitialized) {
                    mMap.clear()
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    mMap.addMarker(MarkerOptions().position(latLng).title(place.name))

                    // Guardar selección
                    lastSelectedLatLng = latLng
                    lastSelectedName = place.name ?: "Lugar seleccionado"
                }
            }

            override fun onError(status: Status) {
                // ESTO TE DIRÁ POR QUÉ NO BUSCA
                val errorMsg = "Error de búsqueda: ${status.statusMessage} (${status.statusCode})"
                Log.e("MAPAS_ERROR", errorMsg)
                Toast.makeText(this@MapasActivity, errorMsg, Toast.LENGTH_LONG).show()

                // Si sale "Status{statusCode=PLACES_API_INVALID_APP_CHECK_TOKEN}" o similar,
                // es un tema de permisos en tu Google Cloud Console.
            }
        })

        // Inicializar el cliente de ubicación de Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el botón de confirmación
        val btnConfirmar = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirmarUbicacion)
        btnConfirmar.setOnClickListener {
            if (lastSelectedLatLng != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("lat", lastSelectedLatLng!!.latitude)
                resultIntent.putExtra("lng", lastSelectedLatLng!!.longitude)
                resultIntent.putExtra("lugar", lastSelectedName)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, selecciona un punto en el mapa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        checkLocationPermission()

        // Al tocar el mapa directamente
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            val geocoder = Geocoder(this, Locale.getDefault())
            var nombreLugar = "Ubicación seleccionada"

            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val direccionCompleta = addresses[0].getAddressLine(0)
                    if (!direccionCompleta.isNullOrEmpty()) {
                        nombreLugar = direccionCompleta
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mMap.addMarker(MarkerOptions().position(latLng).title(nombreLugar))

            // Guardar para confirmar después
            lastSelectedLatLng = latLng
            lastSelectedName = nombreLugar
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                val defecto = LatLng(-12.046374, -77.042793)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defecto, 15f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
        }
    }
}