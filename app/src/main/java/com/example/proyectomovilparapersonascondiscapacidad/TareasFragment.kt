package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TareasFragment : Fragment() {

    private lateinit var adapter: TareaAdapter
    private val listaTareas = mutableListOf<TareaDatos>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tareasfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar RecyclerView
        val rvTareas = view.findViewById<RecyclerView>(R.id.rvTareas)
        adapter = TareaAdapter(listaTareas)
        rvTareas.adapter = adapter
        rvTareas.layoutManager = LinearLayoutManager(requireContext())

        // 2. Configurar botón para agregar
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarTarea)
        btnAgregar.setOnClickListener {
            val dialogo = AgregarTarea()
            dialogo.show(parentFragmentManager, "AgregarTareaTag")
        }

        // 3. Cargar datos
        cargarTareasDesdeFirebase()
    }

    private fun cargarTareasDesdeFirebase() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val emailSanitizado = user?.email?.replace(".", ",") ?: user?.uid ?: "anonimo"
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("Tareas")
            .child(emailSanitizado)

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTareas.clear()
                val tareasActivas = mutableListOf<TareaDatos>()

                for (postSnapshot in snapshot.children) {
                    val tarea = postSnapshot.getValue(TareaDatos::class.java)
                    tarea?.let {
                        if (it.estadoTarea == "ACTIVA") {
                            tareasActivas.add(it)
                        } else if (it.estadoTarea == "PENDIENTE") {
                            listaTareas.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                actualizarCuadroTareaActiva(tareasActivas)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarCuadroTareaActiva(tareasActivas: List<TareaDatos>) {
        val contenedor = view?.findViewById<LinearLayout>(R.id.cuadroTarea) ?: return
        contenedor.removeAllViews()

        // Añadir el label "TAREA ACTUAL"
        val label = TextView(context).apply {
            text = "TAREA ACTUAL"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            letterSpacing = 0.1f
            setTextColor(resources.getColor(R.color.focus_on_secondary_text, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8.dpToPx())
        }
        contenedor.addView(label)

        if (tareasActivas.isEmpty()) {
            val tvVacio = TextView(context).apply {
                text = "Sin tareas activas"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setTextColor(resources.getColor(R.color.focus_on_background, null))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            contenedor.addView(tvVacio)
        } else {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            for (tarea in tareasActivas) {
                val tvNombre = TextView(context).apply {
                    text = tarea.nombreTarea
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
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
