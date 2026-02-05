package ec.edu.puce.lavozguamote.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.ActivitySplashBinding
import ec.edu.puce.lavozguamote.ui.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    
    // URL del logo de La Voz de Guamote
    private val logoUrl = "https://pbs.twimg.com/profile_images/1490159003112034311/aXH5wV1s.jpg"

    companion object {
        private const val SPLASH_DELAY = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullScreen()
        loadLogo()
        startAnimations()
        navigateToMain()
    }

    private fun setupFullScreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
    
    private fun loadLogo() {
        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.ic_radio_logo)
            .circleCrop()
            .into(binding.ivLogo)
    }

    private fun startAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(fadeIn)
        binding.tvSlogan.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.layoutByPuce.visibility = View.VISIBLE
            binding.layoutByPuce.startAnimation(slideUp)
        }, 1000)
    }

    private fun navigateToMain() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, SPLASH_DELAY)
    }
}
