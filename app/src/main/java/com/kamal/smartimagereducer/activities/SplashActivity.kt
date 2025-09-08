package com.kamal.smartimagereducer.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kamal.smartimagereducer.R
import com.kamal.smartimagereducer.databinding.ActivitySplashBinding

class SplashActivity  : AppCompatActivity() {
 private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show Android 12+ Splash
        installSplashScreen()
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#36D080")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding =ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logo = findViewById<ImageView>(R.id.logoImage)
        val animation = AnimationUtils.loadAnimation(this, R.anim.scale_anim)
        logo.startAnimation(animation)

        // Navigate to MainActivity after delay
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // 2 seconds delay
    }
}