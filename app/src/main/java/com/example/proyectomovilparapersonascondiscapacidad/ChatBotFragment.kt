    package com.example.proyectomovilparapersonascondiscapacidad

    import android.graphics.Bitmap
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.fragment.app.Fragment
    import com.google.android.material.button.MaterialButton
    import com.google.zxing.BarcodeFormat
    import com.google.zxing.qrcode.QRCodeWriter

    class ChatBotFragment : Fragment() {

        private data class Rutina(
            val codigo: String,
            val especialista: String,
            val titulo: String,
            val descripcion: String
        )

        private val rutinas = listOf(
            Rutina(
                "FOCUS_01",
                "Dra. Luna",
                "Rutina de enfoque suave",
                "1. Respira profundo durante 2 minutos.\n" +
                        "2. Ordena tu escritorio por 3 minutos.\n" +
                        "3. Estudia una sola tarea durante 15 minutos.\n" +
                        "4. Descansa 5 minutos sin celular.\n" +
                        "5. Marca tu avance en la app."
            ),
            Rutina(
                "FOCUS_02",
                "Dr. Leo",
                "Rutina anti-distracción",
                "1. Elige una tarea pequeña.\n" +
                        "2. Guarda objetos que te distraigan.\n" +
                        "3. Activa un temporizador de 20 minutos.\n" +
                        "4. Trabaja solo en esa tarea.\n" +
                        "5. Al terminar, date una pausa corta."
            ),
            Rutina(
                "FOCUS_03",
                "Dra. Sol",
                "Rutina de calma y organización",
                "1. Toma agua.\n" +
                        "2. Respira lentamente 5 veces.\n" +
                        "3. Escribe 3 cosas que debes hacer.\n" +
                        "4. Empieza por la tarea más fácil.\n" +
                        "5. Felicítate al terminar."
            ),
            Rutina(
                "FOCUS_04",
                "Dr. Mateo",
                "Rutina Pomodoro para TDAH",
                "1. Estudia 10 minutos.\n" +
                        "2. Descansa 3 minutos.\n" +
                        "3. Repite el ciclo 2 veces.\n" +
                        "4. Evita cambiar de tarea.\n" +
                        "5. Registra cómo te sentiste."
            )
        )

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.chatbot_fragment, container, false)

            val tvMensajeBot = view.findViewById<TextView>(R.id.tvMensajeBot)
            val btnGenerarRutina = view.findViewById<MaterialButton>(R.id.btnGenerarRutina)
            val imgQR = view.findViewById<ImageView>(R.id.imgQR)
            val tvDetalleRutina = view.findViewById<TextView>(R.id.tvDetalleRutina)

            btnGenerarRutina.setOnClickListener {
                val rutina = rutinas.random()

                tvMensajeBot.text =
                    "Gracias por contarme cómo te sientes 😊\n\n" +
                            "He preparado una rutina sorpresa con ayuda de un especialista virtual.\n\n" +
                            "No te mostraré la rutina todavía. Primero desbloquéala escaneando el QR."

                tvDetalleRutina.text =
                    "🎁 Rutina sorpresa generada\n\n" +
                            "Especialista virtual: ${rutina.especialista}\n\n" +
                            "📷 Toma captura del QR.\n" +
                            "Luego entra a Escanear QR y selecciona la imagen desde tu galería."

                tvDetalleRutina.visibility = View.VISIBLE
                imgQR.visibility = View.VISIBLE
                imgQR.setImageBitmap(generarQR(rutina.codigo))
            }

            return view
        }

        private fun generarQR(texto: String): Bitmap {
            val size = 1000
            val bits = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }

            return bitmap
        }
    }