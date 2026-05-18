package com.example.proyectomovilparapersonascondiscapacidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class TareaAdapter(private val listaTareas: List<TareaDatos>) :
    RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreTarea)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacionTarea)
        val cbTarea: CheckBox = view.findViewById(R.id.cbTarea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = listaTareas[position]
        holder.tvNombre.text = tarea.nombre
        holder.cbTarea.isChecked = tarea.completada

        // Mostrar el nombre del lugar si existe
        if (!tarea.nombreLugar.isNullOrEmpty()) {
            holder.tvUbicacion.visibility = View.VISIBLE
            holder.tvUbicacion.text = "📍 ${tarea.nombreLugar}"
        } else {
            holder.tvUbicacion.visibility = View.GONE
        }

        // Opcional: Actualizar estado en Firebase al marcar el checkbox
        holder.cbTarea.setOnCheckedChangeListener { _, isChecked ->
            FirebaseDatabase.getInstance().getReference("Tareas")
                .child(tarea.id)
                .child("completada")
                .setValue(isChecked)
        }
    }

    override fun getItemCount(): Int = listaTareas.size
}
