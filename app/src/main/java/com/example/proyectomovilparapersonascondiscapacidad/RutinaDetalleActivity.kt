package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class RutinaDetalleActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private var textoRutina = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutina_detalle)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloRutina)
        val tvEspecialista = findViewById<TextView>(R.id.tvEspecialistaRutina)
        val tvPasos = findViewById<TextView>(R.id.tvPasosRutina)
        val btnComenzar = findViewById<Button>(R.id.btnComenzarRutina)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "MX")

                // BAJAR LA VELOCIDAD: 0.7f o 0.8f es ideal para que sea entendible
                tts.setSpeechRate(0.8f)

                // También puedes bajar el tono (pitch) para que la voz sea más grave y tranquila
                tts.setPitch(1.0f)
            }
        }

        val codigo = intent.getStringExtra("codigoRutina")?.trim()

        textoRutina = when (codigo) {
            "FOCUS_01" -> {
                tvTitulo.text = "🎁 Rutina de Inicio Fácil"
                tvEspecialista.text = "Especialista virtual: Dra. Luna"

                "🌬️ 1. Respira profundo 5 veces para calmarte.\n\n" +
                        "🧹 2. Quita de tu mesa lo que no vayas a usar.\n\n" +
                        "💧 3. Toma un poco de agua para despertar.\n\n" +
                        "🏃 4. Sacude tus manos y hombros 5 segundos.\n\n" +
                        "🎯 5. Elige la tarea más pequeña que tengas.\n\n" +
                        "✍️ 6. Escribe en un papel solo el primer paso.\n\n" +
                        "⏱️ 7. Trabaja en eso por solo 10 minutos.\n\n" +
                        "✅ 8. ¡Genial! Ya rompiste la inercia."
            }

            "FOCUS_02" -> {
                tvTitulo.text = "🎯 Rutina Anti-Distracción"
                tvEspecialista.text = "Especialista virtual: Dr. Leo"

                "📵 1. Pon el celular boca abajo o lejos de ti.\n\n" +
                        "🎧 2. Ponte audífonos para avisar que estás ocupado.\n\n" +
                        "🧠 3. Di en voz alta: 'Ahora voy a trabajar en esto'.\n\n" +
                        "✂️ 4. Divide tu tarea en 3 partes muy cortas.\n\n" +
                        "⏱️ 5. Pon un cronómetro de 15 minutos.\n\n" +
                        "📝 6. Si algo te distrae, anótalo y sigue.\n\n" +
                        "🌿 7. Si te pierdes, respira y vuelve a empezar.\n\n" +
                        "🔁 8. Sigue un poco más, lo estás haciendo bien."
            }

            "FOCUS_03" -> {
                tvTitulo.text = "🌞 Rutina de Calma y Orden"
                tvEspecialista.text = "Especialista virtual: Dra. Sol"

                "💧 1. Toma un sorbo de agua despacio.\n\n" +
                        "🌬️ 2. Haz 3 respiraciones lentas por la nariz.\n\n" +
                        "📋 3. Escribe tus 3 pendientes más importantes.\n\n" +
                        "⭐ 4. Elige el que te parezca más divertido o fácil.\n\n" +
                        "🪑 5. Ajusta tu silla para estar muy cómodo.\n\n" +
                        "🚀 6. Empieza la tarea sin pensar en el final.\n\n" +
                        "⏱️ 7. Mantente enfocado solo 10 minutos.\n\n" +
                        "👏 8. Reconoce tu esfuerzo, cada minuto cuenta."
            }

            "FOCUS_04" -> {
                tvTitulo.text = "⏱️ Pomodoro TDAH Dinámico"
                tvEspecialista.text = "Especialista virtual: Dr. Mateo"

                "📚 1. Ten a la mano solo lo necesario para trabajar.\n\n" +
                        "🎯 2. Decide exactamente qué vas a terminar hoy.\n\n" +
                        "⏱️ 3. Trabaja concentrado por 12 minutos.\n\n" +
                        "☕ 4. Cuando suene la alarma, detente de inmediato.\n\n" +
                        "🚶 5. Camina un poco o estírate por 3 minutos.\n\n" +
                        "🔁 6. Regresa y haz otro bloque de 12 minutos.\n\n" +
                        "🧠 7. Nota qué tanto avanzaste en este tiempo.\n\n" +
                        "✅ 8. ¡Misión cumplida! Tómate un descanso real."
            }

            else -> {
                tvTitulo.text = "Rutina no encontrada"
                tvEspecialista.text = "QR no reconocido"
                "❌ Intenta escanear el código nuevamente."
            }
        }

        tvPasos.text = textoRutina

        btnComenzar.setOnClickListener {
            val regexEmojis = Regex("[\\p{So}\\p{Cn}]")

            // Limpiamos el texto para que el TTS sea breve
            val textoParaVoz = textoRutina
                .replace(regexEmojis, "")
                .replace("\n", ". ")
                .replace("minutos", "min") // El TTS suele leer "min" más rápido
                .trim()

            tts.speak(textoParaVoz, TextToSpeech.QUEUE_FLUSH, null, "rutina_tda")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}