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
        val tareaId = intent.getStringExtra("TAREA_ID") ?: return
        val usuarioId = intent.getStringExtra("USUARIO_ID") ?: return
        val nombreTarea = intent.getStringExtra("NOMBRE_TAREA") ?: "Tarea"
        val horaProgramada = intent.getLongExtra("HORA_PROGRAMADA", System.currentTimeMillis())
        val accion = intent.action

        val database = FirebaseDatabase.getInstance().getReference("Tareas").child(usuarioId).child(tareaId)

        when (accion) {
            "ACCION_INICIAR_TAREA" -> {
                // 1. Cambiar estado a ACTIVA
                database.child("estadoTarea").setValue("ACTIVA")

                // 2. Notificar inicio
                mostrarNotificacionSimple(context, "Tarea iniciada", "$nombreTarea")

                // 3. Programar la pregunta exactamente 1 hora después de la hora de inicio (horaProgramada + 3600000)
                val tiempoPregunta = horaProgramada + 3600000 // 1 hora después de la hora programada
                programarPreguntaParaDespues(context, tareaId, usuarioId, nombreTarea, tiempoPregunta)
            }

            "ACCION_PREGUNTAR_CULMINACION" -> {
                mostrarNotificacionConBotones(context, tareaId, usuarioId, nombreTarea)
            }

            "ACCION_MARCAR_COMPLETADA" -> {
                database.child("estadoTarea").setValue("COMPLETADA")
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(tareaId.hashCode())
            }
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

    private fun mostrarNotificacionConBotones(context: Context, tId: String, uId: String, nombre: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tareas_preguntas"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Consultas de Tareas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¿Terminaste tu tarea?")
            .setContentText("¿Has culminado la actividad: $nombre?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_launcher_foreground, "SÍ, COMPLETADA", pendingSi)
            .addAction(R.drawable.ic_launcher_foreground, "NO COMPLETADA", null)
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
