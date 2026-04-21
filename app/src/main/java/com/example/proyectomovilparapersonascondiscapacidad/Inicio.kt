package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inicio)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottom=findViewById<BottomNavigationView>(R.id.bottomNavigation)
        supportFragmentManager.beginTransaction().replace(R.id.contenedor, TareasFragment()).commit()

        bottom.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_perfil -> {
                    supportFragmentManager.beginTransaction().replace(R.id.contenedor, PerfilFragment()).commit()
                    true
                }
                R.id.nav_tareas -> {
                    supportFragmentManager.beginTransaction().replace(R.id.contenedor, TareasFragment()).commit()
                    true
                }
                R.id.nav_enlace -> {
                    supportFragmentManager.beginTransaction().replace(R.id.contenedor, EnlaceFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}