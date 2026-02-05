package ec.edu.puce.lavozguamote.ui.admin

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.annotations.SerializedName
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.databinding.FragmentAdminActivateBookBinding
import ec.edu.puce.lavozguamote.databinding.ItemUsuarioLibroBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

class AdminActivateBookFragment : Fragment() {

    private var _binding: FragmentAdminActivateBookBinding? = null
    private val binding get() = _binding

    private var bookId: Int = -1
    private var bookTitle: String = ""
    private var bookAuthor: String = ""
    private var bookImageUrl: String? = null

    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var activatedAdapter: UserActivatedAdapter
    private var searchJob: Job? = null
    
    // API Service local con URL correcta
    private val apiService: AdminApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Interceptor para agregar token de autenticación
        val authInterceptor = Interceptor { chain ->
            val token = getAuthToken()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
            
        Retrofit.Builder()
            .baseUrl("http://radioguamoteadmin.runasp.net/") // URL CORRECTA DEL SERVIDOR
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdminApiService::class.java)
    }
    
    private fun getAuthToken(): String? {
        return try {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.getString("auth_token", null)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminActivateBookBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener datos del libro
        bookId = arguments?.getInt("bookId") ?: -1
        bookTitle = arguments?.getString("bookTitle") ?: ""
        bookAuthor = arguments?.getString("bookAuthor") ?: ""
        bookImageUrl = arguments?.getString("bookImageUrl")

        if (bookId <= 0) {
            Toast.makeText(context, "Error: Libro no válido", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupBookInfo()
        setupRecyclerViews()
        setupSearch()
        loadActivatedUsers()
    }

    private fun setupToolbar() {
        binding?.toolbar?.title = "Activar: $bookTitle"
        binding?.toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupBookInfo() {
        binding?.tvLibroTitulo?.text = bookTitle
        binding?.tvLibroAutor?.text = bookAuthor

        if (!bookImageUrl.isNullOrEmpty()) {
            binding?.ivLibroPortada?.let {
                Glide.with(this)
                    .load(bookImageUrl)
                    .placeholder(R.drawable.placeholder_book)
                    .into(it)
            }
        }
    }

    private fun setupRecyclerViews() {
        // Adapter para búsqueda
        searchAdapter = UserSearchAdapter { user ->
            showActivateDialog(user)
        }
        binding?.rvUsuarios?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }

        // Adapter para usuarios activados
        activatedAdapter = UserActivatedAdapter { user ->
            showDeactivateDialog(user)
        }
        binding?.rvUsuariosActivados?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activatedAdapter
        }
    }

    private fun setupSearch() {
        binding?.etBuscarUsuario?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString().trim()
                
                if (query.length >= 2) {
                    searchJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(300) // Debounce
                        searchUsers(query)
                    }
                } else {
                    searchAdapter.submitList(emptyList())
                    binding?.tvResultadosLabel?.visibility = View.GONE
                    binding?.emptyState?.visibility = View.VISIBLE
                    binding?.tvEmptyMessage?.text = "Busca un usuario por nombre o email"
                }
            }
        })
    }

    private suspend fun searchUsers(query: String) {
        if (_binding == null) return
        
        binding?.progressBusqueda?.visibility = View.VISIBLE
        binding?.emptyState?.visibility = View.GONE

        try {
            val response = apiService.searchUsers(query)
            
            // Verificar que el fragmento sigue activo
            if (_binding == null) return
            
            if (response.isSuccessful) {
                val users = response.body()?.data ?: emptyList()
                searchAdapter.submitList(users)
                binding?.tvResultadosLabel?.visibility = if (users.isNotEmpty()) View.VISIBLE else View.GONE
                
                if (users.isEmpty()) {
                    binding?.emptyState?.visibility = View.VISIBLE
                    binding?.tvEmptyMessage?.text = "No se encontraron usuarios con \"$query\""
                }
            } else {
                Toast.makeText(context, "Error al buscar usuarios", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            if (_binding != null) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            binding?.progressBusqueda?.visibility = View.GONE
        }
    }

    private fun loadActivatedUsers() {
        if (_binding == null) return
        
        binding?.progressActivados?.visibility = View.VISIBLE
        binding?.tvNoActivados?.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getBookUsers(bookId)
                
                // Verificar que el fragmento sigue activo
                if (_binding == null) return@launch
                
                if (response.isSuccessful) {
                    val users = response.body()?.data ?: emptyList()
                    activatedAdapter.submitList(users)
                    
                    if (users.isEmpty()) {
                        binding?.tvNoActivados?.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, "Error al cargar usuarios activados", Toast.LENGTH_SHORT).show()
                }
            } finally {
                binding?.progressActivados?.visibility = View.GONE
            }
        }
    }

    private fun showActivateDialog(user: UserSearchResult) {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Activar libro")
                .setMessage("¿Deseas activar el libro \"$bookTitle\" para ${user.nombreCompleto}?")
                .setPositiveButton("Activar") { _, _ ->
                    activateBook(user)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showDeactivateDialog(user: UserWithActivation) {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(ctx)
                .setTitle("Desactivar libro")
                .setMessage("¿Deseas desactivar el libro \"$bookTitle\" para ${user.nombreCompleto}?")
                .setPositiveButton("Desactivar") { _, _ ->
                    deactivateBook(user)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun activateBook(user: UserSearchResult) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.activateBookForUser(user.idUsuario, bookId)
                
                if (_binding == null) return@launch
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Libro activado para ${user.nombreCompleto}", Toast.LENGTH_SHORT).show()
                    loadActivatedUsers()
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Error al activar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deactivateBook(user: UserWithActivation) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.deactivateBookForUser(user.idUsuario, bookId)
                
                if (_binding == null) return@launch
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Libro desactivado", Toast.LENGTH_SHORT).show()
                    loadActivatedUsers()
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Error al desactivar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}

// =============================================
// MODELOS DE DATOS LOCALES
// =============================================
data class UserSearchResult(
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String
)

data class UserWithActivation(
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("fechaActivacion") val fechaActivacion: String?
)

data class ApiResponseLocal<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// =============================================
// API SERVICE LOCAL
// =============================================
interface AdminApiService {
    @GET("api/admin/users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<ApiResponseLocal<List<UserSearchResult>>>

    @POST("api/admin/users/{userId}/books/{bookId}/activate")
    suspend fun activateBookForUser(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponseLocal<Unit>>

    @DELETE("api/admin/users/{userId}/books/{bookId}/activate")
    suspend fun deactivateBookForUser(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponseLocal<Unit>>

    @GET("api/admin/books/{bookId}/users")
    suspend fun getBookUsers(@Path("bookId") bookId: Int): Response<ApiResponseLocal<List<UserWithActivation>>>
}

// =============================================
// ADAPTERS
// =============================================

// Adapter para búsqueda
class UserSearchAdapter(
    private val onActivateClick: (UserSearchResult) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    private var users: List<UserSearchResult> = emptyList()

    fun submitList(newUsers: List<UserSearchResult>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemUsuarioLibroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserSearchResult) {
            binding.tvNombre.text = user.nombreCompleto
            binding.tvEmail.text = user.email
            binding.tvFechaActivacion.visibility = View.GONE
            binding.btnAccion.text = "Activar"
            binding.btnAccion.setBackgroundColor(binding.root.context.getColor(R.color.primary))
            binding.btnAccion.setOnClickListener { onActivateClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioLibroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size
}

// Adapter para usuarios activados
class UserActivatedAdapter(
    private val onDeactivateClick: (UserWithActivation) -> Unit
) : RecyclerView.Adapter<UserActivatedAdapter.ViewHolder>() {

    private var users: List<UserWithActivation> = emptyList()

    fun submitList(newUsers: List<UserWithActivation>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemUsuarioLibroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserWithActivation) {
            binding.tvNombre.text = user.nombreCompleto
            binding.tvEmail.text = user.email
            
            if (!user.fechaActivacion.isNullOrEmpty()) {
                binding.tvFechaActivacion.visibility = View.VISIBLE
                binding.tvFechaActivacion.text = "Activado: ${user.fechaActivacion}"
            } else {
                binding.tvFechaActivacion.visibility = View.GONE
            }
            
            binding.btnAccion.text = "Quitar"
            binding.btnAccion.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_red_light))
            binding.btnAccion.setOnClickListener { onDeactivateClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioLibroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size
}
