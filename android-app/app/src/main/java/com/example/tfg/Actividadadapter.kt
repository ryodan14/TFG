package com.example.tfg

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.ui.AniadirEvento
import com.example.tfg.ui.DetalleActividad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ActividadAdapter(
    private var listaActividades: MutableList<Actividad>
) : RecyclerView.Adapter<ActividadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCard)
        val tvHora: TextView = view.findViewById(R.id.tvHoraCard)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCard)
        val tvTipo: TextView = view.findViewById(R.id.tvTipoCard)
        val btnOpciones: ImageButton = view.findViewById(R.id.btnOpciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_actividad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val actividad = listaActividades[position]
        val context = holder.itemView.context

        holder.tvFecha.text = actividad.fecha
        holder.tvHora.text = actividad.horaInicio.substringBeforeLast(":")
        holder.tvNombre.text = actividad.nombre
        holder.tvTipo.text = actividad.tipo

        // === INICIO DE LO NUEVO: CLIC EN LA TARJETA PARA VER DETALLES ===
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleActividad::class.java)
            intent.putExtra("nombre", actividad.nombre)
            intent.putExtra("tipo", actividad.tipo)
            intent.putExtra("fecha", actividad.fecha)
            intent.putExtra("hora_inicio", actividad.horaInicio)
            intent.putExtra("hora_fin", actividad.horaFin)
            intent.putExtra("monitores", actividad.monitores)
            intent.putExtra("observaciones", actividad.observaciones)
            context.startActivity(intent)
        }
        // === FIN DE LO NUEVO ===

        holder.btnOpciones.setOnClickListener {
            val popup = PopupMenu(context, holder.btnOpciones)
            popup.menu.add("Editar")
            popup.menu.add("Eliminar")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Editar" -> {
                        val intent = Intent(context, AniadirEvento::class.java)
                        intent.putExtra("id_actividad", actividad.id)
                        intent.putExtra("nombre", actividad.nombre)
                        intent.putExtra("tipo", actividad.tipo)
                        intent.putExtra("fecha", actividad.fecha)
                        intent.putExtra("hora_inicio", actividad.horaInicio)
                        intent.putExtra("hora_fin", actividad.horaFin)
                        intent.putExtra("monitores", actividad.monitores)
                        intent.putExtra("observaciones", actividad.observaciones)
                        context.startActivity(intent)
                        true
                    }
                    "Eliminar" -> {
                        AlertDialog.Builder(context)
                            .setTitle("¿Borrar?")
                            .setPositiveButton("Sí") { _, _ -> borrarActividad(actividad.id, position, context) }
                            .setNegativeButton("No", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = listaActividades.size

    private fun borrarActividad(id: String, position: Int, context: Context) {
        (context as? AppCompatActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2/TFG/borrar_actividad.php")
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.doOutput = true
                val writer = OutputStreamWriter(con.outputStream)
                writer.write("id_actividad=" + URLEncoder.encode(id, "UTF-8"))
                writer.flush()
                writer.close()

                if (con.responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        listaActividades.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
