package com.srs.ssms.smartlab

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.text.InputType
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.Toast
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_input_sampel.*
import kotlinx.android.synthetic.main.text_edit.view.*
import java.io.File
import java.io.FileOutputStream
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.form.*
import kotlinx.android.synthetic.main.tv_isian.view.*
import java.text.SimpleDateFormat
import java.util.*

class InputSampel : AppCompatActivity() {

    private var nomor= ""
    private var surat= ""
    private var sampel= ""
    private var pendamping= ""
    private var pdfName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_sampel)

        setText(
            layoutNomor,
            "N10",
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME.or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
        )
        setText(
            layoutSurat,
            "N10",
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME.or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
        )
        setText(
            layoutSampel,
            "N10",
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME.or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
        )
        setText(
            pendamping1,
            "N10",
            InputType.TYPE_TEXT_VARIATION_PERSON_NAME.or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
        )

        layoutNomor.textContent.text = "Nomor"
        layoutSurat.textContent.text = "Surat"
        layoutSampel.textContent.text = "Sampel"
        pendamping1.textContent.text = "Pendamping"

        btSimpan.setOnClickListener {
            tvEstate.tvDataHasil.text = layoutNomor.etContent.text
            tvAfd.tvDataHasil.text = layoutSurat.etContent.text
            tvBlok.tvDataHasil.text = layoutSampel.etContent.text
            tvTanggal.tvDataHasil.text = pendamping1.etContent.text
            savePDF("Pdf") {
                Toasty.info(this, "Tersimpan"). show()

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun savePDF(str: String, function: () -> Unit) {
        //tvNomorSurat.tvDataDes.text = "Nomor Surat"
        //tvNomorSurat.tvDataHasil.text = noSurat.toString()
        pdfName = "${pdfName.replace(".PDF", "")}$str.PDF"
        try {
            formHasil.visibility = View.VISIBLE
        } finally {
            val iv = findViewById<HorizontalScrollView>(R.id.svHorizontal)
            val bm = Bitmap.createBitmap(
                iv.getChildAt(0).width,
                iv.getChildAt(0).height,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(bm)
            iv.getChildAt(0).draw(c)
            val dirPath = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
            val dir = File(dirPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dirPath, "share.JPEG")
            try {
                val fos = FileOutputStream(file)
                bm.compress(Bitmap.CompressFormat.JPEG, 30, fos)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Terjadi kesalahan Error: $e", Toast.LENGTH_SHORT).show()
            }
            try {
                val pagesize = Rectangle(440f, 900f)
                val document = Document(pagesize)
                PdfWriter.getInstance(
                    document,
                    FileOutputStream("$dirPath${File.separator}$pdfName")
                ) //  Change pdf's name.
                document.open()
                val img = Image.getInstance("$dirPath/share.JPEG")
                val scaler = (document.pageSize
                    .height - document.topMargin() - document.bottomMargin()) / img.height * 100
                img.scalePercent(scaler)
                img.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
                document.add(img)
                document.close()
                function()
            } catch (e: Exception) {
                Toasty.error(
                    this,
                    "Terjadi Kesalahan error: $dirPath${File.separator}$pdfName",
                    Toast.LENGTH_LONG
                ).show()
            }
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    private fun setText(view: View, judul: String, inputType: Int) {
        view.textContent.text = judul
        view.etContent.hint = judul
        view.etContent.inputType = inputType
    }
}