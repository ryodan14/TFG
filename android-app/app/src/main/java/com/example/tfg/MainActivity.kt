package com.example.tfg

import com.example.tfg.ui.AniadirEvento
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usamos la toolbar de la app_bar_main
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // --- BLOQUE: Datos del Header ---
        val nombreUsuario = intent.getStringExtra("EXTRA_NOMBRE")
        val emailUsuario = intent.getStringExtra("EXTRA_EMAIL")
        val headerView = navView.getHeaderView(0)
        val tvNombre = headerView.findViewById<TextView>(R.id.tvNombreHeader)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvEmailHeader)

        tvNombre.text = nombreUsuario ?: "Usuario"
        tvEmail.text = emailUsuario ?: "correo@asociacion.com"

        // --- BLOQUE: Navegación Segura ---
        // Buscamos el navHostFragment de forma manual para evitar errores de binding desactualizado
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment

        // Si no es nulo, configuramos el controlador
        navHostFragment?.let {
            val navController = it.navController

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home, R.id.nav_participantes, R.id.nav_hinchables,
                    R.id.nav_facturas, R.id.nav_contabilidad, R.id.nav_usuarios
                ), drawerLayout
            )

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_anadir -> {
                val intent = Intent(this, AniadirEvento::class.java)
                startActivity(intent)
                return true
            }
            // ESTO ES LO NUEVO: Revisa que el ID coincida con el de tu menu/main.xml
            R.id.action_ver_calendario -> {
                val intent = Intent(this, CalendarioActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        val navController = navHostFragment?.navController
        return navController?.let {
            it.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } ?: super.onSupportNavigateUp()
    }
}