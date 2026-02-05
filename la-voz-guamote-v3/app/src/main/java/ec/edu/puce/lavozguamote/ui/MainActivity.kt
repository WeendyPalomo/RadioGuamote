package ec.edu.puce.lavozguamote.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.ActivityMainBinding
import ec.edu.puce.lavozguamote.services.RadioStreamingService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private var radioService: RadioStreamingService? = null
    private var isBound = false
    private var isPlaying = false

    companion object {
        // Logo de La Voz de Guamote
        private const val LOGO_URL = "https://jesuitas.ec/wp-content/uploads/2024/02/LVG-PRODUCCIONES-01.png"
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioStreamingService.RadioBinder
            radioService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            radioService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupDrawer()
        setupDrawerLogo()
        setupMiniPlayer()
        setupBackPressHandler()
        bindRadioService()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        
        binding.navDrawer.setupWithNavController(navController)
        
        binding.navDrawer.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    true
                }
                else -> {
                    try {
                        navController.navigate(menuItem.itemId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
            }
        }
    }

    private fun setupDrawer() {
        // Drawer configurado automáticamente
    }

    private fun setupDrawerLogo() {
        // Cargar el logo en el header del drawer desde URL
        try {
            val headerView = binding.navDrawer.getHeaderView(0)
            val logoImageView = headerView?.findViewById<ImageView>(R.id.iv_drawer_logo)
            
            logoImageView?.let { imageView ->
                Glide.with(this)
                    .load(LOGO_URL)
                    .placeholder(R.drawable.ic_logo_guamote)
                    .error(R.drawable.ic_logo_guamote)
                    .circleCrop()
                    .into(imageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupMiniPlayer() {
        // Botón Play/Pause del mini player
        binding.miniPlayer.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseRadio()
            } else {
                playRadio()
            }
        }

        // Botón Cerrar del mini player
        binding.miniPlayer.btnClose.setOnClickListener {
            stopRadio()
            hideMiniPlayer()
        }

        // Click en el mini player (expandir en el futuro)
        binding.miniPlayer.miniPlayerCard.setOnClickListener {
            // TODO: Expandir reproductor completo
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    // ==================== RADIO CONTROL ====================

    private fun playRadio() {
        radioService?.play()
        isPlaying = true
        updateMiniPlayerUI()
    }

    private fun pauseRadio() {
        radioService?.pause()
        isPlaying = false
        updateMiniPlayerUI()
    }

    private fun stopRadio() {
        radioService?.stop()
        isPlaying = false
        updateMiniPlayerUI()
    }

    private fun updateMiniPlayerUI() {
        // Cambiar icono play/pause
        binding.miniPlayer.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_circle
        )
        
        // Mostrar/ocultar barra de progreso
        binding.miniPlayer.progressStreaming.visibility = 
            if (isPlaying) View.VISIBLE else View.GONE
    }

    // Llamado desde HomeFragment cuando se presiona play
    fun startRadioFromHome() {
        radioService?.play()
        isPlaying = true
        showMiniPlayer()
        updateMiniPlayerUI()
        
        // Actualizar info del mini player
        binding.miniPlayer.tvProgramName.text = "La Voz de Guamote"
        binding.miniPlayer.tvHostName.text = "1520 AM"
    }

    // Llamado desde HomeFragment cuando se presiona pause
    fun pauseRadioFromHome() {
        pauseRadio()
    }

    fun showMiniPlayer() {
        binding.miniPlayerContainer.visibility = View.VISIBLE
    }

    fun hideMiniPlayer() {
        binding.miniPlayerContainer.visibility = View.GONE
    }

    fun isRadioPlaying(): Boolean = isPlaying

    fun getRadioService(): RadioStreamingService? = radioService

    // ==================== SERVICE BINDING ====================

    private fun bindRadioService() {
        try {
            Intent(this, RadioStreamingService::class.java).also { intent ->
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            try {
                unbindService(serviceConnection)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isBound = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
