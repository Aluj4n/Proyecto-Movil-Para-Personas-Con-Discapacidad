package com.example.proyectomovilparapersonascondiscapacidad

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class AgregarTarea : BottomSheetDialogFragment() {

    private var horaSeleccionada: Long = 0
    private var latitudSeleccionada: Double = 0.0
    private var longitudSeleccionada: Double = 0.0
    private var lugarSeleccionado: String = ""

    // 1. El Launcher DEBE estar aquí como propiedad de la clase, fuera de las funciones
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            latitudSeleccionada = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
            longitudSeleccionada = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            lugarSeleccionado = result.data?.getStringExtra("lugar") ?: "Ubicacion Seleccionada"
            Toast.makeText(context, "Lugar: $lugarSeleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.agregar_tarea, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        val etNombre = view.findViewById<EditText>(R.id.etNombreTarea)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarTarea)
        val tvHora = view.findViewById<TextView>(R.id.tvSeleccionarHora)
        val btnMapa = view.findViewById<Button>(R.id.btnSeleccionarUbicacion)

        // Selector de Hora
        tvHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(context, { _, hour, minute ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hour)
                selectedCalendar.set(Calendar.MINUTE, minute)
                selectedCalendar.set(Calendar.SECOND, 0)

                horaSeleccionada = selectedCalendar.timeInMillis
                tvHora.text = String.format("Hora: %02d:%02d", hour, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Abrir Mapa (MapasActivity seguirá en rojo hasta que crees el archivo)
        btnMapa.setOnClickListener {
            val intent = Intent(context, MapasActivity::class.java)
            mapLauncher.launch(intent)
        }

        // Botón Guardar
        btnGuardar.setOnClickListener {
            val nombreTarea = etNombre.text.toString().trim()
            if (nombreTarea.isNotEmpty()) {
                guardarEnFirebase(nombreTarea)
            } else {
                Toast.makeText(context, "Por favor, escribe una tarea", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEnFirebase(nombre: String) {
        val database = FirebaseDatabase.getInstance().getReference("Tareas")
        val id = database.push().key ?: return

        // Usamos los 6 parámetros definidos en tu TareaDatos.kt
        val nuevaTarea = TareaDatos(
            id = id,
            nombre = nombre,
            completada = false,
            hora = horaSeleccionada,
            latitud = latitudSeleccionada,
            longitud = longitudSeleccionada,
            nombreLugar = lugarSeleccionado
        )

        database.child(id).setValue(nuevaTarea)
            .addOnSuccessListener {
                if (horaSeleccionada > System.currentTimeMillis()) {
                    programarNotificacion(nombre, horaSeleccionada, lugarSeleccionado)
                }
                Toast.makeText(context, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun programarNotificacion(nombre: String, tiempo: Long, lugar: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOMBRE_TAREA", nombre)
            putExtra("LUGAR_TAREA", lugar)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tiempo.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
                } else {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(settingsIntent)
                    Toast.makeText(context, "Permite alarmas exactas para notificaciones puntuales", Toast.LENGTH_LONG).show()
                    alarmManager.set(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
        }
    }
}