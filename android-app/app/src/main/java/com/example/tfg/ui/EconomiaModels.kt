package com.example.tfg.ui

class EconomiaModels {
    data class RespuestaEconomia(
        val resumen: ResumenMensual,
        val detalles: List<MovimientoDetalle>
    )

    data class ResumenMensual(
        val salidas: Int,
        val ingresos: Double,
        val gastos: Double,
        val beneficio: Double
    )

    data class MovimientoDetalle(
        val fecha: String,
        val descripcion: String,
        val monto: Double,
        val tipo: String
    )
}