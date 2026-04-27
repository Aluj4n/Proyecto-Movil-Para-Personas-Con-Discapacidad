package com.example.proyectomovilparapersonascondiscapacidad

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

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

        val toolbar = findViewById<Toolbar>(R.id.menuOpciones)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FocusFlow"

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Carga HomeFragment por defecto
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor, HomeFragment())
            .commit()

        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_perfil -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_tareas -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor, TareasFragment())
                        .commit()
                    true
                }
                R.id.nav_enlace -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor, QRFragment())
                        .commit()
                    true
                }
            R.id.nav_chatbot -> {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor, ChatBotFragment())
                .commit()
            true
        }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.cerrarGoogle -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}