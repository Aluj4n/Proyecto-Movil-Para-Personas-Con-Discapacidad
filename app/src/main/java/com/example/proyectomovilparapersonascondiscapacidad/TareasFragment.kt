package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class TareasFragment : Fragment() {

    private lateinit var adapter: TareaAdapter
    private val listaTareas = mutableListOf<TareaDatos>()
    private val database = FirebaseDatabase.getInstance().getReference("Tareas")

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

        // 2. Configurar botón para abrir el diálogo (AgregarTarea)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarTarea)
        btnAgregar.setOnClickListener {
            val dialogo = AgregarTarea()
            dialogo.show(parentFragmentManager, "AgregarTareaTag")
        }

        // 3. Cargar tareas desde Firebase en tiempo real
        cargarTareasDesdeFirebase()
    }

    private fun cargarTareasDesdeFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTareas.clear()
                for (postSnapshot in snapshot.children) {
                    val tarea = postSnapshot.getValue(TareaDatos::class.java)
                    tarea?.let { listaTareas.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Opcional: manejar error
            }
        })
    }
}