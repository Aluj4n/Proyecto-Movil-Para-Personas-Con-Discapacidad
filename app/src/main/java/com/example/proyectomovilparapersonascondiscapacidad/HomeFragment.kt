package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homefragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Referenciamos el textview tvfrase de homefragment.xml
        val tvFrase = view.findViewById<TextView>(R.id.tvFrase)

        //Creamos una lista y agregamos frases
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

        //
        val fraseAleatoria = frases.random()
        tvFrase.text="\"$fraseAleatoria\""
    }



}