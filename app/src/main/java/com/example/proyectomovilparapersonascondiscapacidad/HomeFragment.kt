package com.example.proyectomovilparapersonascondiscapacidad

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val requestUbicacionPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            cargarClima()
        } else {
            view?.findViewById<TextView>(R.id.tvClimaTemp)?.text = "Permiso de ubicación denegado"
            view?.findViewById<TextView>(R.id.tvClimaSugerencia)?.text = "El clima no está disponible sin ubicación"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homefragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvFrase = view.findViewById<TextView>(R.id.tvFrase)
        val user = FirebaseAuth.getInstance().currentUser
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuario)
        val ivPerfil = view.findViewById<android.widget.ImageView>(R.id.ivPerfil)

        user?.let {
            val nombre = it.displayName ?: "Usuario"
            tvNombre.text = "¡Bienvenido, $nombre!"

            it.photoUrl?.let { uri ->
                com.bumptech.glide.Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(ivPerfil)
            }
        }

        val frases = listOf(
            "No necesitas hacerlo perfecto, solo dar el siguiente paso.",
            "Tu forma de pensar es diferente, y eso también puede ser una fortaleza.",
            "Está bien avanzar poco a poco; el progreso sigue siendo progreso.",
            "No eres distraído por falta de capacidad, tu mente simplemente funciona de otra manera.",
            "Descansar también es parte del proceso.",
            "Cada pequeña tarea terminada cuenta.",
            "Tu creatividad y energía tienen mucho valor.",
            "No te compares con el ritmo de los demás.",
            "Puedes reorganizarte las veces que necesites.",
            "Un mal día no define todo tu esfuerzo.",
            "Lo importante no es cuánto tardas, sino que sigues intentándolo.",
            "Tu potencial no se mide por tu nivel de concentración.",
            "Está bien pedir ayuda cuando la necesites.",
            "Las metas grandes también se logran con pasos pequeños.",
            "Tu mente puede ser caótica a veces, pero también muy brillante."
        )

        tvFrase.text = "\"${frases.random()}\""

        cargarClima()
        escucharTareaActiva()
    }

    private fun cargarClima() {
        if (!isAdded) return
        val tvTemp = view?.findViewById<TextView>(R.id.tvClimaTemp) ?: return
        val tvSugerencia = view?.findViewById<TextView>(R.id.tvClimaSugerencia) ?: return

        val tienePermiso = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            requestUbicacionPermiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (!isAdded) return@addOnSuccessListener
                if (location != null) {
                    fetchClima(location.latitude, location.longitude)
                } else {
                    tvTemp.text = "Buscando ubicación..."
                    tvSugerencia.text = "Activa el GPS y vuelve a esta pantalla"
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                tvTemp.text = "Error de ubicación"
                tvSugerencia.text = "Verifica la configuración de GPS"
            }
    }

    private fun fetchClima(lat: Double, lon: Double) {
        Thread {
            try {
                val url = URL("https://wttr.in/$lat,$lon?format=j1")
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "FocusApp/1.0")
                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                val response = connection.getInputStream().bufferedReader().readText()
                val json = JSONObject(response)
                val current = json.getJSONArray("current_condition").getJSONObject(0)
                val tempC = current.getString("temp_C")
                val descIngles = current.getJSONArray("weatherDesc").getJSONObject(0).getString("value")
                val humedad = current.getString("humidity")

                val descEspanol = traducirClima(descIngles)
                val (icono, sugerencia) = getIconoYSugerencia(descIngles, tempC.toIntOrNull() ?: 20)

                if (!isAdded) return@Thread
                requireActivity().runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    view?.let { v ->
                        v.findViewById<TextView>(R.id.tvClimaTemp)?.text = "$tempC°C · $descEspanol · 💧$humedad%"
                        v.findViewById<TextView>(R.id.tvClimaIcono)?.text = icono
                        v.findViewById<TextView>(R.id.tvClimaSugerencia)?.text = sugerencia
                    }
                }
            } catch (e: Exception) {
                if (!isAdded) return@Thread
                requireActivity().runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    view?.let { v ->
                        v.findViewById<TextView>(R.id.tvClimaTemp)?.text = "Sin conexión al clima"
                        v.findViewById<TextView>(R.id.tvClimaSugerencia)?.text = "Revisa tu conexión a internet"
                    }
                }
            }
        }.start()
    }

    private fun traducirClima(desc: String): String = when {
        "Sunny" in desc -> "Soleado"
        "Clear" in desc -> "Despejado"
        "Partly cloudy" in desc -> "Parcialmente nublado"
        "Cloudy" in desc -> "Nublado"
        "Overcast" in desc -> "Muy nublado"
        "Heavy rain" in desc -> "Lluvia fuerte"
        "Light rain" in desc -> "Lluvia ligera"
        "Rain" in desc -> "Lluvia"
        "Drizzle" in desc -> "Llovizna"
        "Thunder" in desc || "Thundery" in desc -> "Tormenta"
        "Blizzard" in desc -> "Ventisca"
        "Snow" in desc -> "Nieve"
        "Fog" in desc -> "Niebla"
        "Mist" in desc -> "Neblina"
        "Haze" in desc -> "Bruma"
        "Sleet" in desc -> "Aguanieve"
        "Freezing" in desc -> "Helada"
        else -> desc
    }

    private fun getIconoYSugerencia(desc: String, temp: Int): Pair<String, String> {
        val d = desc.lowercase()
        return when {
            "thunder" in d || "storm" in d -> "⛈️" to "Quédate en casa, enfócate en tus tareas"
            "heavy rain" in d -> "🌧️" to "Lluvia fuerte — mejor estudiar en casa hoy"
            "rain" in d || "drizzle" in d -> "🌦️" to "Buen momento para estudiar en casa"
            "snow" in d || "blizzard" in d -> "❄️" to "Abrígate bien antes de salir"
            "fog" in d || "mist" in d || "haze" in d -> "🌫️" to "Ten cuidado al salir, visibilidad baja"
            "overcast" in d || "cloudy" in d -> "☁️" to "Clima tranquilo, ideal para concentrarte"
            "partly" in d -> "⛅" to "Buen día para una caminata corta"
            "sunny" in d || "clear" in d -> "☀️" to "Ideal para una pausa al aire libre"
            temp > 32 -> "🥵" to "Mucho calor — hidrátate bien hoy"
            temp < 8 -> "🥶" to "Hace frío — abrígate antes de salir"
            else -> "🌤️" to "Buen día para enfocarte en tus metas"
        }
    }

    private fun escucharTareaActiva() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val emailSanitizado = user.email?.replace(".", ",") ?: user.uid
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Tareas")
            .child(emailSanitizado)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tareasActivas = mutableListOf<TareaDatos>()
                for (postSnapshot in snapshot.children) {
                    val tarea = postSnapshot.getValue(TareaDatos::class.java)
                    if (tarea?.estadoTarea == "ACTIVA") {
                        tareasActivas.add(tarea)
                    }
                }
                actualizarUICuadroInicio(tareasActivas)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarUICuadroInicio(tareasActivas: List<TareaDatos>) {
        val contenedor = view?.findViewById<LinearLayout>(R.id.cuadroTareaInicio) ?: return
        contenedor.removeAllViews()

        val label = TextView(context).apply {
            text = "AHORA MISMO"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            letterSpacing = 0.1f
            setTextColor(resources.getColor(R.color.focus_on_secondary_text, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8.dpToPx())
        }
        contenedor.addView(label)

        if (tareasActivas.isEmpty()) {
            val tvSinTarea = TextView(context).apply {
                text = "Sin tarea activa"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(resources.getColor(R.color.focus_on_background, null))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            contenedor.addView(tvSinTarea)
        } else {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            for (tarea in tareasActivas) {
                val tvNombre = TextView(context).apply {
                    text = tarea.nombreTarea
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setTextColor(resources.getColor(R.color.focus_on_background, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val tvHora = TextView(context).apply {
                    text = sdf.format(Date(tarea.horaTarea))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(resources.getColor(R.color.focus_primary, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, 12.dpToPx())
                }
                contenedor.addView(tvNombre)
                contenedor.addView(tvHora)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
