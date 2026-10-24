package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GestionUsuariosFragment : Fragment() {
    private lateinit var adapter: UsuariosAdapter
    private var usuarioSeleccionado: Usuario? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.rvUsuarios)
        rv.layoutManager = LinearLayoutManager(requireContext())

        adapter = UsuariosAdapter(emptyList()) { u ->
            usuarioSeleccionado = u
            Toast.makeText(context, "Seleccionado: ${u.username}", Toast.LENGTH_SHORT).show()
        }
        rv.adapter = adapter

        root.findViewById<Button>(R.id.btnNuevoUsuario).setOnClickListener {
            val intent = Intent(requireContext(), FormularioUsuarioActivity::class.java)
            intent.putExtra("MODO", "NUEVO")
            startActivity(intent)
        }

        root.findViewById<Button>(R.id.btnEditarUser).setOnClickListener {
            usuarioSeleccionado?.let { u ->
                val intent = Intent(requireContext(), FormularioUsuarioActivity::class.java)
                intent.putExtra("MODO", "EDITAR")
                intent.putExtra("ID", u.id)
                intent.putExtra("USER", u.username)
                intent.putExtra("NOMBRE", u.nombreCompleto)
                intent.putExtra("EMAIL", u.email)
                startActivity(intent)
            } ?: Toast.makeText(context, "Selecciona un usuario", Toast.LENGTH_SHORT).show()
        }

        cargarUsuarios()
        return root
    }

    private fun cargarUsuarios() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = URL("http://10.0.2.2/TFG/gestion_usuarios.php?accion=obtener").readText()
                val array = JSONObject(res).getJSONArray("usuarios")
                val listaLocal = mutableListOf<Usuario>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    listaLocal.add(Usuario(o.getString("id_usuario"), o.getString("username"), o.getString("nombre_completo"), o.getString("email"), o.getString("rol"), o.getString("activo")))
                }
                withContext(Dispatchers.Main) { adapter.updateData(listaLocal) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}