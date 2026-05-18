package com.example.proyectomovilparapersonascondiscapacidad

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapasActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapas)

        // Inicializar el cliente de ubicación de Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 1. Verificar y solicitar permisos de ubicación
        checkLocationPermission()

        // 2. Al tocar el mapa para elegir un destino
        // Busca este bloque en MapasActivity.kt y reemplázalo:
        mMap.setOnMapClickListener { latLng ->
            mMap.clear()

            val geocoder = Geocoder(this, Locale.getDefault())
            // Valor por defecto para que NUNCA esté vacío
            var nombreLugar = "Ubicación seleccionada"

            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    // Usamos getAddressLine(0) que devuelve la dirección completa (Calle, número, ciudad)
                    val direccionCompleta = addresses[0].getAddressLine(0)
                    if (!direccionCompleta.isNullOrEmpty()) {
                        nombreLugar = direccionCompleta
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mMap.addMarker(MarkerOptions().position(latLng).title(nombreLugar))

            val resultIntent = Intent()
            resultIntent.putExtra("lat", latLng.latitude)
            resultIntent.putExtra("lng", latLng.longitude)
            resultIntent.putExtra("lugar", nombreLugar) // Ahora sí lleva texto real
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Si no hay permiso, lo pedimos (Código 1000)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        // Si ya hay permiso, activamos el punto azul y centramos la cámara
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                // Ubicación por defecto si falla el sensor (Lima)
                val defecto = LatLng(-12.046374, -77.042793)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defecto, 15f))
            }
        }
    }

    // Gestionar la respuesta del usuario al permiso
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado. Se mostrará ubicación por defecto.", Toast.LENGTH_SHORT).show()
        }
    }
}