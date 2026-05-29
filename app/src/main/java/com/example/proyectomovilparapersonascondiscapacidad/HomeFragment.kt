package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

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

        val fraseAleatoria = frases.random()
        tvFrase.text = "\"$fraseAleatoria\""

        escucharTareaActiva()
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

        // Añadir el label "AHORA MISMO"
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
