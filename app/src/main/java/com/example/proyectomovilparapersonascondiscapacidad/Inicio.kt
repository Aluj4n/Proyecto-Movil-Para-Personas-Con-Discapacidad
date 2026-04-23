package com.example.proyectomovilparapersonascondiscapacidad

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


//Import's agregados -v
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

        // Menu opciones
        val toolbar = findViewById<Toolbar>(R.id.menuOpciones)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "FocusEco"


        //Navigation view
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
    
    //Logica para cierre de sesion Google
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
            // 3. Regresar al Login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}