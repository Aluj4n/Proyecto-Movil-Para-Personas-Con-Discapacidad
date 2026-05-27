package com.example.proyectomovilparapersonascondiscapacidad

data class TareaDatos(
    //id de tarea
    val idTarea: String = "",
    val nombreTarea: String = "",
    // Estados: "PENDIENTE", "ACTIVA", "COMPLETADA"
    val estadoTarea: String = "PENDIENTE",
    val horaTarea: Long = 0,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val nombreLugar: String = "",
    val usuarioid: String = ""
){

}
