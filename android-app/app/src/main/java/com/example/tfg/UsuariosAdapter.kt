package com.example.tfg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(
    private var lista: List<Usuario>,
    private val onItemClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Asegúrate de que estos IDs existan en tu item_usuario.xml o cámbialos aquí
        val tvUser: TextView = view.findViewById(R.id.tvColUser)
        val tvNombre: TextView = view.findViewById(R.id.tvColNombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val u = lista[position]
        holder.tvUser.text = u.username
        holder.tvNombre.text = u.nombreCompleto
        holder.itemView.setOnClickListener { onItemClick(u) }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<Usuario>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}