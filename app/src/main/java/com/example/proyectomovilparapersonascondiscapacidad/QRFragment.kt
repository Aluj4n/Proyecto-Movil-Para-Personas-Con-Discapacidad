package com.example.proyectomovilparapersonascondiscapacidad

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.content.Intent
class QRFragment : Fragment() {

    private lateinit var cardResultado: MaterialCardView
    private lateinit var tvResultadoQR: TextView
    private lateinit var btnIniciarRutina: MaterialButton

    private val seleccionarImagenQR =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                leerQRDesdeImagen(uri)
            } else {
                Toast.makeText(requireContext(), "No seleccionaste ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

    private val qrLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                mostrarRutinaSegunQR(result.contents)
            } else {
                tvResultadoQR.text = "Escaneo cancelado"
                cardResultado.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.qr_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnEscanear = view.findViewById<MaterialButton>(R.id.btnEscanear)
        val btnLeerGaleria = view.findViewById<MaterialButton>(R.id.btnLeerGaleria)

        cardResultado = view.findViewById(R.id.cardResultado)
        tvResultadoQR = view.findViewById(R.id.tvResultadoQR)
        btnIniciarRutina = view.findViewById(R.id.btnIniciarRutina)

        btnEscanear.setOnClickListener {
            abrirCamaraQR()
        }

        btnLeerGaleria.setOnClickListener {
            seleccionarImagenQR.launch("image/*")
        }

        btnIniciarRutina.setOnClickListener {
            tvResultadoQR.text = "Rutina iniciada ✅"
        }
    }

    private fun abrirCamaraQR() {
        val options = ScanOptions()
        options.setPrompt("Escanea el QR de tu rutina")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setCaptureActivity(QRScannerActivity::class.java)

        qrLauncher.launch(options)
    }

    private fun leerQRDesdeImagen(imageUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            if (bitmap == null) {
                tvResultadoQR.text = "No se pudo abrir la imagen"
                cardResultado.visibility = View.VISIBLE
                return
            }

            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val result = MultiFormatReader().decode(binaryBitmap)
            mostrarRutinaSegunQR(result.text)

        } catch (e: Exception) {
            tvResultadoQR.text = "No se pudo leer el QR. Usa una captura clara, completa y sin recortar."
            cardResultado.visibility = View.VISIBLE
        }
    }

    private fun mostrarRutinaSegunQR(contenidoQR: String) {
        val intent = Intent(requireContext(), RutinaDetalleActivity::class.java)
        intent.putExtra("codigoRutina", contenidoQR)
        startActivity(intent)
    }

}


