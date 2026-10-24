package com.example.tfg.ui.slideshow

data class MovimientoEconomico(
    val id_movimiento: String, // <--- Fíjate que se llame exactamente así
    val fecha: String,
    val tipo: String,
    val concepto: String,
    val cantidad: String,
    val observaciones: String
)