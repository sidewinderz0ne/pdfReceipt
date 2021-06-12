package com.srs.ssms.smartlab

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_main.view.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tampilanMainMenu()

        inputSampel.setOnClickListener {
            val intent = Intent(this@MainActivity, InputSampel::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun tampilanMainMenu(){
        tvUser.text = "${PrefManager(this).name}" //setting nama dari shared preferences

        //ganti icon dan warna text dan button
        inputSampel.ivMenu.setImageDrawable(resources.getDrawable(R.drawable.ssms_green))
        //inputSampel.ivMenu.imageTintList = ColorStateList.valueOf(getColor(R.color.blueDefault))
        inputSampel.cardMenu.setStrokeColor(ColorStateList.valueOf(getColor(R.color.blueDefault)))
        inputSampel.tvMenu.setTextColor(ColorStateList.valueOf(getColor(R.color.blueDefault)))

        preparasi.ivMenu.setImageDrawable(resources.getDrawable(R.drawable.ssms_green))
        preparasi.ivMenu.imageTintList = ColorStateList.valueOf(getColor(R.color.blueDefault))
        preparasi.cardMenu.setStrokeColor(ColorStateList.valueOf(getColor(R.color.blueDefault)))
        preparasi.tvMenu.setTextColor(ColorStateList.valueOf(getColor(R.color.blueDefault)))

        oven.tvMenu.text = "Oven"
        preparasi.tvMenu.text = "Preparasi"
        pengukuran.tvMenu.text = "Pengukuran"
        inputSampel.tvMenu.text = "Input Sampel"

    }

    override fun onBackPressed() {
        AlertDialogUtility.withTwoActions(
            this,
            "Batal",
            "Keluar",
            "Apakah anda yakin untuk keluar dari apikasi SmartLAB?",
            "warning.json"
        ) {
            finishAffinity()
        }
    }

}