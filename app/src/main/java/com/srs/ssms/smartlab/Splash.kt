package com.srs.ssms.smartlab

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    startActivity(Intent(this@Splash, InputSampel::class.java))
                    finish()
                }
            }
        }

        thread.start()
        val logoSRS = findViewById<ImageView>(R.id.logoSRS)
        Glide.with(this).load(R.drawable.ssms_green).into(logoSRS)
        val imageView = findViewById<ImageView>(R.id.splash)
        Glide.with(this).load(R.drawable.ssms_green).into(imageView)

        /*YoYo.with(Techniques.FadeIn)
            .duration(1000)
            .repeat(0)
            .playOn(findViewById(R.id.anim))*/

        val appLinkIntent = intent
        val appLinkAction = appLinkIntent.action
        val appLinkData = appLinkIntent.data
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}