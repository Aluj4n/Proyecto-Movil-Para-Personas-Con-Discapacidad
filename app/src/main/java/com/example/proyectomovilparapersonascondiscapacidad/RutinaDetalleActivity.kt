package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class RutinaDetalleActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private var textoRutina = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutina_detalle)

        // Views principales
        val tvTitulo = findViewById<TextView>(R.id.tvTituloRutina)
        val btnComenzar = findViewById<Button>(R.id.btnComenzarRutina)
        val tvInstruccion = findViewById<TextView>(R.id.tvInstruccion)
        
        // Contenedores
        val layoutMoods = findViewById<LinearLayout>(R.id.layoutMoods)
        val layoutOptions = findViewById<LinearLayout>(R.id.layoutOptions)
        val cardRutina = findViewById<MaterialCardView>(R.id.cardRutina)

        // Botones de Mood
        val btnAlegre = findViewById<LinearLayout>(R.id.btnAlegre)
        val btnTriste = findViewById<LinearLayout>(R.id.btnTriste)
        val btnMolesto = findViewById<LinearLayout>(R.id.btnMolesto)
        val btnRegresar = findViewById<Button>(R.id.btnRegresar)
        val btnAsignarTarea = findViewById<Button>(R.id.btnAsignarTarea)

        // Vistas de Opciones
        val tvMensajeMood = findViewById<TextView>(R.id.tvMensajeMood)
        val btnOpcion1 = findViewById<Button>(R.id.btnOpcion1)
        val btnOpcion2 = findViewById<Button>(R.id.btnOpcion2)
        val btnOpcion3 = findViewById<Button>(R.id.btnOpcion3)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "MX")
                tts.setSpeechRate(0.85f)
            }
        }

        // Lógica al elegir Emoji
        btnAlegre.setOnClickListener {
            mostrarPasosSeleccion("alegre", tvMensajeMood, btnOpcion1, btnOpcion2, btnOpcion3, layoutMoods, layoutOptions, tvInstruccion)
        }
        btnTriste.setOnClickListener {
            mostrarPasosSeleccion("triste", tvMensajeMood, btnOpcion1, btnOpcion2, btnOpcion3, layoutMoods, layoutOptions, tvInstruccion)
        }
        btnMolesto.setOnClickListener {
            mostrarPasosSeleccion("molesto", tvMensajeMood, btnOpcion1, btnOpcion2, btnOpcion3, layoutMoods, layoutOptions, tvInstruccion)
        }

        btnRegresar.setOnClickListener {
            if (::tts.isInitialized) tts.stop()

            if (cardRutina.visibility == View.VISIBLE) {
                // De rutina a opciones
                cardRutina.visibility = View.GONE
                findViewById<View>(R.id.btnComenzarRutina).visibility = View.GONE
                layoutOptions.visibility = View.VISIBLE
                tvInstruccion.text = getString(R.string.instruccion_meta)
            } else if (layoutOptions.visibility == View.VISIBLE) {
                // De opciones a mood
                layoutOptions.visibility = View.GONE
                btnRegresar.visibility = View.GONE
                layoutMoods.visibility = View.VISIBLE
                tvInstruccion.text = getString(R.string.instruccion_mood)
            }
        }

        btnAsignarTarea.setOnClickListener {
            val titulo = tvTitulo.text.toString()
            if (titulo.isNotEmpty()) {
                asignarRutinaComoTarea(titulo)
            }
        }

        btnComenzar.setOnClickListener {
            if (textoRutina.isNotEmpty()) {
                val regexEmojis = Regex("[\\p{So}\\p{Cn}]")
                val textoParaVoz = textoRutina.replace(regexEmojis, "").replace("\n", ". ").trim()
                tts.speak(textoParaVoz, TextToSpeech.QUEUE_FLUSH, null, "rutina_voz")
            }
        }
    }

    private fun mostrarPasosSeleccion(
        mood: String,
        tvMsg: TextView,
        b1: Button, b2: Button, b3: Button,
        lMoods: View, lOpts: View, tvInst: TextView
    ) {
        lMoods.visibility = View.GONE
        lOpts.visibility = View.VISIBLE
        findViewById<Button>(R.id.btnRegresar).visibility = View.VISIBLE
        tvInst.text = getString(R.string.instruccion_meta)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloRutina)
        val tvPasos = findViewById<TextView>(R.id.tvPasosRutina)
        val card = findViewById<MaterialCardView>(R.id.cardRutina)
        val btnEscuchar = findViewById<Button>(R.id.btnComenzarRutina)

        when (mood) {
            "alegre" -> {
                tvMsg.text = getString(R.string.msg_alegre)
                configurarBotonTarea(b1, "Estudio Flash", "Abre tu libro o apunte en la página que te toca leer.\n\n Lee solamente un párrafo o resuelve un ejercicio.\n\n Celébralo con un baile!", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b2, "Avance de Tarea", "Escribe el título de tu tarea y tu nombre en una hoja nueva.\n\n Contesta solo la primera pregunta sin pensar demasiado..\n\n Marca esa pequeña victoria y decide si quieres seguir.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b3, "Partida de Celebración", "Abre tu juego favorito.\n\n Juega una sola partida rápida para aprovechar tu buena energía.\n\n Cierra el juego al terminar y estírate un poco.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)

            }
            "triste" -> {
                tvMsg.text = getString(R.string.msg_triste)
                configurarBotonTarea(b1, "Mira Una Pelicula", "Ponte tu ropa más cómoda o tu pijama favorito.\n\n Pon una película que disfrutes mucho.\n\n Descansa en tu cama sin sentir nada de culpa.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b2, "🧸 Micro-Orden", "🧹 1. Recoge solo 3 objetos.\n\n🧹 2. Limpia un pedacito de tu mesa.\n\n🧹 3. ¡Excelente avance!", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b3, "Escucha tu musica favorita", " Lávate la cara.\n\n Ponte ropa cómoda.\n\n Escucha tu canciónciones favoritas.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
            }
            "molesto" -> {
                tvMsg.text = getString(R.string.msg_molesto)
                configurarBotonTarea(b1, "💪 Descarga Segura", "🥊 1. Aprieta tus manos fuerte 5 segundos.\n\n🥊 2. Suelta el aire con ruido.\n\n🥊 3. Repite 3 veces.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b2, "🧊 Enfoque Sensorial", "❄️ 1. Busca algo muy frío.\n\n❄️ 2. Tócalo por 10 segundos.\n\n❄️ 3. Nota cómo baja el calor de tu cuerpo.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
                configurarBotonTarea(b3, "🔎 Juego de Rastreo", "👁️ 1. Busca 5 cosas verdes.\n\n👁️ 2. Busca 4 cosas cuadradas.\n\n👁️ 3. Respira y nota el cambio.", tvTitulo, tvPasos, card, lOpts, btnEscuchar)
            }
        }
    }

    private fun configurarBotonTarea(btn: Button, titulo: String, pasos: String, tvT: TextView, tvP: TextView, card: View, lOpts: View, btnE: View) {
        btn.text = titulo
        btn.setOnClickListener {
            tvT.text = titulo
            tvP.text = pasos
            textoRutina = pasos
            lOpts.visibility = View.GONE
            card.visibility = View.VISIBLE
            btnE.visibility = View.VISIBLE
        }
    }

    private fun asignarRutinaComoTarea(nombre: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val emailSanitizado = user?.email?.replace(".", ",") ?: user?.uid ?: "anonimo"
        val database = FirebaseDatabase.getInstance().getReference("Tareas").child(emailSanitizado)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var maxId = 0
                for (child in snapshot.children) {
                    val key = child.key
                    val numericId = key?.toIntOrNull() ?: 0
                    if (numericId > maxId) {
                        maxId = numericId
                    }
                }

                val nuevoIdInt = maxId + 1
                val idFormateado = String.format(Locale.getDefault(), "%05d", nuevoIdInt)

                val nuevaTarea = TareaDatos(
                    idTarea = idFormateado,
                    nombreTarea = nombre,
                    estadoTarea = "ACTIVA", // Se asigna como ACTIVA directamente
                    horaTarea = System.currentTimeMillis(),
                    latitud = 0.0,
                    longitud = 0.0,
                    nombreLugar = "Rutina Personalizada",
                    usuarioid = emailSanitizado
                )

                database.child(idFormateado).setValue(nuevaTarea)
                    .addOnSuccessListener {
                        Toast.makeText(this@RutinaDetalleActivity, getString(R.string.toast_rutina_activada), Toast.LENGTH_LONG).show()
                        finish() // Opcional: Cerrar la actividad al asignar
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@RutinaDetalleActivity, getString(R.string.toast_error_activar), Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RutinaDetalleActivity, getString(R.string.toast_error_conexion), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}