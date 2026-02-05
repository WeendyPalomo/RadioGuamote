package ec.edu.puce.lavozguamote.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ec.edu.puce.lavozguamote.data.local.DataManager
import ec.edu.puce.lavozguamote.data.local.LoginResult
import ec.edu.puce.lavozguamote.data.local.RegisterResult
import ec.edu.puce.lavozguamote.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    
    @Inject
    lateinit var dataManager: DataManager

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("mode") ?: "login"
        isLoginMode = mode == "login"
        updateUI()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tvGoToRegister.setOnClickListener {
            isLoginMode = false
            updateUI()
        }

        binding.tvGoToLogin.setOnClickListener {
            isLoginMode = true
            updateUI()
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnRegister.setOnClickListener {
            performRegister()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            binding.tvTitulo.text = "Iniciar Sesión"
            binding.layoutLogin.visibility = View.VISIBLE
            binding.layoutRegister.visibility = View.GONE
        } else {
            binding.tvTitulo.text = "Crear Cuenta"
            binding.layoutLogin.visibility = View.GONE
            binding.layoutRegister.visibility = View.VISIBLE
        }
        binding.tvError.visibility = View.GONE
    }

    private fun performLogin() {
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos")
            return
        }

        setLoading(true)
        
        lifecycleScope.launch {
            when (val result = dataManager.login(email, password)) {
                is LoginResult.Success -> {
                    Toast.makeText(
                        this@AuthActivity,
                        "¡Bienvenido, ${result.usuario.nombreCompleto}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is LoginResult.Error -> {
                    showError(result.message)
                    setLoading(false)
                }
            }
        }
    }

    private fun performRegister() {
        val nombre = binding.etRegisterNombre.text.toString().trim()
        val apellido = binding.etRegisterApellido.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val telefono = binding.etRegisterTelefono.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString()
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString()

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Por favor completa todos los campos obligatorios")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Por favor ingresa un email válido")
            return
        }

        if (password != confirmPassword) {
            showError("Las contraseñas no coinciden")
            return
        }

        if (password.length < 6) {
            showError("La contraseña debe tener al menos 6 caracteres")
            return
        }

        setLoading(true)
        
        lifecycleScope.launch {
            when (val result = dataManager.register(
                nombre = nombre,
                apellido = apellido,
                email = email,
                password = password,
                telefono = telefono.ifEmpty { null }
            )) {
                is RegisterResult.Success -> {
                    Toast.makeText(
                        this@AuthActivity,
                        "¡Registro exitoso! Bienvenido, ${result.usuario.nombreCompleto}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is RegisterResult.Error -> {
                    showError(result.message)
                    setLoading(false)
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
    }
}
