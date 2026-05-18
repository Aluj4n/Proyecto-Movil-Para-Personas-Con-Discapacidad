package com.example.proyectomovilparapersonascondiscapacidad

data class TareaDatos(
    val id: String = "",
    val nombre: String = "",
    val completada: Boolean = false,
    val hora: Long = 0,
    val latitud: Double = 0.0,
    val longitud: Double= 0.0,
    val nombreLugar: String = "",
    val usuarioid: String = ""

){

}
