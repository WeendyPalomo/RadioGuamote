package ec.edu.puce.lavozguamote.ui.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.data.models.Evento
import ec.edu.puce.lavozguamote.data.models.Libro
import ec.edu.puce.lavozguamote.data.models.Noticia
import ec.edu.puce.lavozguamote.databinding.ItemEventoBinding
import ec.edu.puce.lavozguamote.databinding.ItemLibroBinding
import ec.edu.puce.lavozguamote.databinding.ItemNoticiaBinding
import ec.edu.puce.lavozguamote.databinding.ItemNoticiaHomeBinding

// EVENTOS ADAPTER
class EventosAdapter(
    private val onItemClick: (Evento) -> Unit,
    private val onLikeClick: (Evento) -> Unit,
    private val onShareClick: (Evento) -> Unit = {}
) : ListAdapter<Evento, EventosAdapter.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) = holder.bind(getItem(position))

    inner class EventoViewHolder(private val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(evento: Evento) {
            binding.apply {
                tvNombreEncargado.text = evento.nombreEncargado ?: "La Voz de Guamote"
                tvCategoria.text = evento.categoria?.nombre ?: "Evento"
                evento.categoria?.color?.let { try { tvCategoria.setBackgroundColor(Color.parseColor(it)) } catch (e: Exception) {} }
                if (!evento.imagenPrincipal.isNullOrEmpty()) {
                    Glide.with(ivEventoImagen).load(evento.imagenPrincipal).placeholder(R.drawable.placeholder_event).centerCrop().into(ivEventoImagen)
                }
                tvTitulo.text = evento.titulo
                tvFechaHora.text = evento.fechaEvento
                tvDireccion.text = evento.direccion ?: ""
                tvLikesCount.text = "${evento.likesCount} me gusta"
                btnLike.setImageResource(if (evento.userLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
                tvComentariosCount.visibility = View.GONE
                btnComment.visibility = View.GONE
                tvDescripcion.text = evento.descripcion?.take(150)?.plus("...") ?: ""
                tvPrecio.text = if (evento.precio > 0) "Entrada: $${String.format("%.2f", evento.precio)}" else "Entrada gratuita"
                root.setOnClickListener { onItemClick(evento) }
                btnLike.setOnClickListener { onLikeClick(evento) }
                btnShare.setOnClickListener { onShareClick(evento) }
            }
        }
    }
    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento) = oldItem.idEvento == newItem.idEvento
        override fun areContentsTheSame(oldItem: Evento, newItem: Evento) = oldItem == newItem
    }
}

// NOTICIAS ADAPTER
class NoticiasAdapter(private val onItemClick: (Noticia) -> Unit) : ListAdapter<Noticia, NoticiasAdapter.NoticiaViewHolder>(NoticiaDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val binding = ItemNoticiaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticiaViewHolder(binding)
    }
    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) = holder.bind(getItem(position))

    inner class NoticiaViewHolder(private val binding: ItemNoticiaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(noticia: Noticia) {
            binding.apply {
                tvTitulo.text = noticia.titulo
                tvSubtitulo.text = noticia.subtitulo ?: ""
                tvSubtitulo.visibility = if (noticia.subtitulo.isNullOrEmpty()) View.GONE else View.VISIBLE
                tvResumen.text = noticia.resumen ?: noticia.contenido.take(150) + "..."
                tvCategoria.text = noticia.categoria?.nombre ?: "General"
                tvAutorFecha.text = "${noticia.autor ?: "Redaccion"} - ${noticia.fechaPublicacion}"
                tvVistas.text = "${noticia.vistas} vistas"
                noticia.categoria?.color?.let { try { tvCategoria.setBackgroundColor(Color.parseColor(it)) } catch (e: Exception) {} }
                if (!noticia.imagenPrincipal.isNullOrEmpty()) {
                    Glide.with(ivNoticia).load(noticia.imagenPrincipal).placeholder(R.drawable.placeholder_news).centerCrop().into(ivNoticia)
                    ivNoticia.visibility = View.VISIBLE
                } else { ivNoticia.visibility = View.GONE }
                badgeUrgente.visibility = if (noticia.esNoticiaUrgente) View.VISIBLE else View.GONE
                badgeDestacada.visibility = if (noticia.esDestacada) View.VISIBLE else View.GONE
                root.setOnClickListener { onItemClick(noticia) }
            }
        }
    }
    class NoticiaDiffCallback : DiffUtil.ItemCallback<Noticia>() {
        override fun areItemsTheSame(oldItem: Noticia, newItem: Noticia) = oldItem.idNoticia == newItem.idNoticia
        override fun areContentsTheSame(oldItem: Noticia, newItem: Noticia) = oldItem == newItem
    }
}

// NOTICIAS HOME ADAPTER
class NoticiasHomeAdapter(private val onItemClick: (Noticia) -> Unit) : ListAdapter<Noticia, NoticiasHomeAdapter.NoticiaViewHolder>(NoticiaDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticiaViewHolder {
        val binding = ItemNoticiaHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticiaViewHolder(binding)
    }
    override fun onBindViewHolder(holder: NoticiaViewHolder, position: Int) = holder.bind(getItem(position))

    inner class NoticiaViewHolder(private val binding: ItemNoticiaHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(noticia: Noticia) {
            binding.apply {
                tvTitulo.text = noticia.titulo
                tvResumen.text = noticia.resumen ?: noticia.contenido.take(100) + "..."
                tvFecha.text = noticia.fechaPublicacion
                if (!noticia.imagenPrincipal.isNullOrEmpty()) {
                    Glide.with(ivNoticia).load(noticia.imagenPrincipal).placeholder(R.drawable.placeholder_news).centerCrop().into(ivNoticia)
                }
                root.setOnClickListener { onItemClick(noticia) }
            }
        }
    }
    class NoticiaDiffCallback : DiffUtil.ItemCallback<Noticia>() {
        override fun areItemsTheSame(oldItem: Noticia, newItem: Noticia) = oldItem.idNoticia == newItem.idNoticia
        override fun areContentsTheSame(oldItem: Noticia, newItem: Noticia) = oldItem == newItem
    }
}

// LIBROS ADAPTER - Verificacion estricta: SOLO esGratis o comprado dan acceso
class LibrosAdapter(
    private val onItemClick: (Libro) -> Unit, 
    private val onComprarClick: (Libro) -> Unit
) : ListAdapter<Libro, LibrosAdapter.LibroViewHolder>(LibroDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibroViewHolder {
        val binding = ItemLibroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LibroViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LibroViewHolder, position: Int) = holder.bind(getItem(position))

    inner class LibroViewHolder(private val binding: ItemLibroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(libro: Libro) {
            binding.apply {
                tvTitulo.text = libro.titulo
                tvAutor.text = libro.autor ?: "Autor desconocido"
                tvCategoria.text = libro.categoria?.nombre ?: ""
                
                if (!libro.imagenPortada.isNullOrEmpty()) {
                    Glide.with(ivPortada).load(libro.imagenPortada).placeholder(R.drawable.placeholder_book).into(ivPortada)
                }
                
                // Ocultar precios siempre
                tvPrecio.visibility = View.GONE
                tvPrecioOriginal.visibility = View.GONE
                
                // Ocultar estrellas/valoracion
                ratingBar.visibility = View.GONE
                tvValoracion.visibility = View.GONE
                
                // VERIFICACION ESTRICTA: tiene acceso SOLO si esGratis=true O comprado=true
                // NO se usa precio == 0.0 como condicion de acceso
                val tieneAcceso = libro.esGratis || libro.comprado
                
                // Mostrar badge GRATIS solo si esGratis es true (del servidor)
                if (libro.esGratis) {
                    badgeOferta.text = "GRATIS"
                    badgeOferta.setBackgroundColor(0xFF4CAF50.toInt())
                    badgeOferta.visibility = View.VISIBLE
                } else {
                    badgeOferta.visibility = View.GONE
                }
                
                // Estado del libro segun acceso estricto
                if (tieneAcceso) {
                    // TIENE ACCESO - mostrar boton Leer
                    ivLock.visibility = View.GONE
                    btnComprar.visibility = View.GONE
                    btnLeer.visibility = View.VISIBLE
                    overlayBloqueado.visibility = View.GONE
                } else {
                    // NO TIENE ACCESO - mostrar boton Solicitar y candado
                    ivLock.visibility = View.VISIBLE
                    btnComprar.visibility = View.VISIBLE
                    btnComprar.text = "Solicitar"
                    btnLeer.visibility = View.GONE
                    overlayBloqueado.visibility = View.VISIBLE
                }
                
                // Click en el card completo - el Fragment decidira que hacer
                root.setOnClickListener { onItemClick(libro) }
                
                // Click en boton Solicitar - siempre va a WhatsApp
                btnComprar.setOnClickListener { onComprarClick(libro) }
                
                // Click en boton Leer - solo visible si tiene acceso
                btnLeer.setOnClickListener { onItemClick(libro) }
            }
        }
    }
    
    class LibroDiffCallback : DiffUtil.ItemCallback<Libro>() {
        override fun areItemsTheSame(oldItem: Libro, newItem: Libro) = oldItem.idLibro == newItem.idLibro
        override fun areContentsTheSame(oldItem: Libro, newItem: Libro) = oldItem == newItem
    }
}
