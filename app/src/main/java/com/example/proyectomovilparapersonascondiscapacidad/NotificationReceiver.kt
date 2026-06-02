package com.example.proyectomovilparapersonascondiscapacidad

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val tareaId = intent.getStringExtra("TAREA_ID") ?: return
        val usuarioId = intent.getStringExtra("USUARIO_ID") ?: return
        val nombreTarea = intent.getStringExtra("NOMBRE_TAREA") ?: "Tarea"
        val horaProgramada = intent.getLongExtra("HORA_PROGRAMADA", System.currentTimeMillis())
        val accion = intent.action

        val database = FirebaseDatabase.getInstance().getReference("Tareas").child(usuarioId).child(tareaId)

        database.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists() && accion != "ACCION_MARCAR_COMPLETADA" && accion != "ACCION_POSPONER_TAREA") {
                pendingResult.finish()
                return@addOnSuccessListener
            }

            val estadoActual = snapshot.child("estadoTarea").value as? String

            when (accion) {
                "ACCION_INICIAR_TAREA" -> {
                    // Solo iniciar si la tarea sigue PENDIENTE
                    if (estadoActual == "PENDIENTE") {
                        database.child("estadoTarea").setValue("ACTIVA")
                        mostrarNotificacionSimple(context, "Tarea iniciada", nombreTarea)
                        
                        val tiempoPregunta = horaProgramada + 3600000 
                        programarPreguntaParaDespues(context, tareaId, usuarioId, nombreTarea, tiempoPregunta)
                    }
                }

                "ACCION_PREGUNTAR_CULMINACION" -> {
                    // Solo preguntar si la tarea sigue ACTIVA
                    if (estadoActual == "ACTIVA") {
                        mostrarNotificacionConBotones(context, tareaId, usuarioId, nombreTarea, horaProgramada)
                    }
                }

                "ACCION_MARCAR_COMPLETADA" -> {
                    database.removeValue()
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(tareaId.hashCode())
                }

                "ACCION_POSPONER_TAREA" -> {
                    val unDiaEnMillis = 24 * 60 * 60 * 1000L
                    val nuevaHora = horaProgramada + unDiaEnMillis

                    val updates = hashMapOf<String, Any>(
                        "horaTarea" to nuevaHora,
                        "estadoTarea" to "PENDIENTE"
                    )
                    database.updateChildren(updates).addOnSuccessListener {
                        reprogramarParaManana(context, tareaId, usuarioId, nombreTarea, nuevaHora)
                    }

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(tareaId.hashCode())
                }
            }
            pendingResult.finish()
        }.addOnFailureListener {
            pendingResult.finish()
        }
    }

    private fun reprogramarParaManana(context: Context, tId: String, uId: String, nombre: String, tiempo: Long) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACCION_INICIAR_TAREA"
            putExtra("TAREA_ID", tId)
            putExtra("USUARIO_ID", uId)
            putExtra("NOMBRE_TAREA", nombre)
            putExtra("HORA_PROGRAMADA", tiempo)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tId.hashCode(), // Usar hashCode del ID para que sea consistente
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempo, pendingIntent)
        }
    }

    private fun mostrarNotificacionSimple(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tareas_seguimiento"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Estado de Tareas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun mostrarNotificacionConBotones(context: Context, tId: String, uId: String, nombre: String, horaOrig: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tareas_preguntas"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Consultas de Tareas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para SÍ, COMPLETADA
        val intentSi = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACCION_MARCAR_COMPLETADA"
            putExtra("TAREA_ID", tId)
            putExtra("USUARIO_ID", uId)
        }
        val pendingSi = PendingIntent.getBroadcast(
            context, 
            tId.hashCode(), 
            intentSi, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent para NO COMPLETADA (Posponer)
        val intentNo = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACCION_POSPONER_TAREA"
            putExtra("TAREA_ID", tId)
            putExtra("USUARIO_ID", uId)
            putExtra("HORA_PROGRAMADA", horaOrig)
        }
        val pendingNo = PendingIntent.getBroadcast(
            context,
            (tId + "no").hashCode(),
            intentNo,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notif_titulo_pregunta))
            .setContentText(context.getString(R.string.notif_cuerpo_pregunta, nombre))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notif_accion_si), pendingSi)
            .addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notif_accion_no), pendingNo)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(tId.hashCode(), notification)
    }

    private fun programarPreguntaParaDespues(context: Context, tId: String, uId: String, nombre: String, tiempoPregunta: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACCION_PREGUNTAR_CULMINACION"
            putExtra("TAREA_ID", tId)
            putExtra("USUARIO_ID", uId)
            putExtra("NOMBRE_TAREA", nombre)
            putExtra("HORA_PROGRAMADA", tiempoPregunta - 3600000) // Guardamos la hora de inicio original
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            (tId + "preg").hashCode(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoPregunta, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, tiempoPregunta, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoPregunta, pendingIntent)
        }
    }
}
