package com.example.tfg

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class FormularioUsuarioActivity : AppCompatActivity() {
    private var idUsuario: String? = null
    private var modo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_usuario)

        val etUser = findViewById<EditText>(R.id.etUsernam)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEmail = findViewById<EditText>(R.id.etEmai)
        val spRol = findViewById<Spinner>(R.id.spRo)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCambio)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        val roles = arrayOf("ADMINISTRADOR", "MONITOR")
        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        modo = intent.getStringExtra("MODO")
        if (modo == "EDITAR") {
            idUsuario = intent.getStringExtra("ID")
            etUser.setText(intent.getStringExtra("USER") ?: "")
            etNombre.setText(intent.getStringExtra("NOMBRE") ?: "")
            etEmail.setText(intent.getStringExtra("EMAIL") ?: "")
        }

        btnGuardar.setOnClickListener {
            guardarServidor(etUser.text.toString(), etNombre.text.toString(), etEmail.text.toString(), spRol.selectedItem.toString())
        }
        btnVolver.setOnClickListener { finish() }
    }

    private fun guardarServidor(u: String, n: String, e: String, r: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val acc = if (modo == "NUEVO") "insertar" else "actualizar"
                val p = "accion=$acc&id_usuario=$idUsuario&username=$u&nombre_completo=$n&email=$e&rol=$r"
                val conn = URL("http://10.0.2.2/TFG/gestion_usuarios.php").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.write(p.toByteArray())
                val res = conn.inputStream.bufferedReader().readText()
                if (res.contains("Exito")) withContext(Dispatchers.Main) { finish() }
            } catch (ex: Exception) { ex.printStackTrace() }
        }
    }
}