package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena los campos", Toast.LENGTH_SHORT).show()
            } else {
                // PARA EMULADOR: 10.0.2.2 equivale al "localhost" de tu PC
                // Asegúrate de que tu archivo está en: C:\xampp\htdocs\tfg\login.php
                val url = "http://10.0.2.2/TFG/login.php"

                validarLogin(url, usuario, password)
            }
        }
    }

    private fun validarLogin(url: String, usuario: String, pass: String) {
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.optString("message", "Sin mensaje")

                    if (success) {
                        // 1. Recoger los datos que vienen del PHP
                        // Asegúrate que en tu PHP las claves sean "nombre" y "email"
                        val nombreRecibido = jsonResponse.optString("nombre")
                        val emailRecibido = jsonResponse.optString("email")

                        Toast.makeText(this, "Bienvenido $nombreRecibido", Toast.LENGTH_SHORT).show()

                        // 2. Meterlos en la mochila para el viaje
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("EXTRA_NOMBRE", nombreRecibido)
                        intent.putExtra("EXTRA_EMAIL", emailRecibido)

                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error procesando respuesta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                // Este mensaje saldrá si XAMPP está apagado o la ruta está mal
                Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                // CAMBIO AQUÍ: Antes ponía "usuario", ahora pon "username"
                params["username"] = usuario

                params["password"] = pass
                return params
            }
        }

        queue.add(stringRequest)
    }
}