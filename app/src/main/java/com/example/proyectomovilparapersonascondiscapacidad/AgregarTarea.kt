package com.example.proyectomovilparapersonascondiscapacidad

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import java.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.os.Build


import com.google.android.material.bottomsheet.BottomSheetDialogFragment
// Solo un import de FirebaseDatabase es necesario
import com.google.firebase.database.FirebaseDatabase


class AgregarTarea : BottomSheetDialogFragment() {

    private var horaSeleccionada: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.agregar_tarea, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Importante: para mostrar el apartado de permisos de notificacion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        val etNombre = view.findViewById<EditText>(R.id.etNombreTarea)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarTarea)

        val tvHora = view.findViewById<TextView>(R.id.tvSeleccionarHora)

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

        // Aquí usamos TareaDatos que es el nombre de tu clase modelo
        val nuevaTarea = TareaDatos(id, nombre, false, horaSeleccionada)

        database.child(id).setValue(nuevaTarea)
            .addOnSuccessListener {
                if (horaSeleccionada > System.currentTimeMillis()) {
                    programarNotificacion(nombre, horaSeleccionada)
                }
                Toast.makeText(context, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun programarNotificacion(nombre: String, tiempo: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOMBRE_TAREA", nombre)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tiempo.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            // Intentamos programar la alarma exacta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
                } else {
                    // Si no tiene permiso en Android 12+, pedimos al usuario que lo active
                    val settingsIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(settingsIntent)
                    Toast.makeText(context, "Por favor, permite alarmas exactas para esta app", Toast.LENGTH_LONG).show()

                    // Programamos una normal como respaldo
                    alarmManager.set(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
                }
            } else {
                // Versiones anteriores a Android 12 no necesitan validación extra
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Si falla por seguridad, usamos la aproximada para que al menos llegue
            alarmManager.set(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
            Toast.makeText(context, "Usando recordatorio aproximado por falta de permisos", Toast.LENGTH_SHORT).show()
        }
    }
}