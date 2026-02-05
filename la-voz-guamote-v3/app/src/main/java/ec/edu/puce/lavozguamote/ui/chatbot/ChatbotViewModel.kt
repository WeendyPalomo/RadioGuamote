package ec.edu.puce.lavozguamote.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatbotViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastBotResponse = MutableStateFlow<String?>(null)
    val lastBotResponse: StateFlow<String?> = _lastBotResponse.asStateFlow()

    var shouldSpeak = false
        private set

    private val sessionId = UUID.randomUUID().toString()

    // Base de conocimiento completa de La Voz de Guamote
    private val knowledgeBase = mapOf(
        // ===== UBICACIÓN =====
        listOf("ubicación", "ubicacion", "donde", "dirección", "direccion", "llegar", "encuentran", "están", "estan", "queda") to 
            "📍 Estamos ubicados en la Comunidad Santa Cruz de Guamote. ¡Te esperamos!",
        
        // ===== PUBLICIDAD =====
        listOf("publicidad", "anuncio", "anunciar", "promocionar", "cuña", "cuna", "spot", "precio publicidad", "costo publicidad") to 
            "📢 ¡Nuestro medio oferta precios muy económicos! Tenemos combos desde \$15 dólares en radio y redes. Comunícate a estos números para cotizar:\n📞 0989432206\n📞 0991280714",
        
        // ===== TRANSMISIONES/COBERTURAS =====
        listOf("transmisión", "transmision", "cobertura", "cubrir", "evento", "precio transmision", "costo transmision", "grabar") to 
            "🎙️ Cubrimos eventos a nivel local. El costo es módico, comienza con \$50 y depende de la hora y el lugar. Para más información comunícate:\n📞 0989432206\n📞 0991280714",
        
        // ===== PROGRAMAS 2026 =====
        listOf("programa", "programas", "programación", "programacion", "2026", "horario", "transmiten") to 
            "📻 Nuestros programas del 2026 incluyen:\n\n🎤 Testimonios de mi Pueblo\n🎭 Nuestra Identidad\n⛪ Voces que Evangelizan\n⚽ La Voz del Deporte\n🎪 Tu Voz en La Feria\n🏥 Programas de Salud\n👥 Voces del Pueblo\n🎉 Coberturas de eventos culturales\n\n¡Síguenos en nuestras redes para no perderte ninguno!",
        
        // ===== FUNDACIÓN E HISTORIA =====
        listOf("fundó", "fundo", "fundador", "fundación", "fundacion", "historia", "creó", "creo", "inicio", "comenzó", "comenzo", "origen", "quién creó", "quien creo") to 
            "📜 La Voz de Guamote es un medio comunitario fundado en el 2002 por el P. Julio Gortaire, S.J. y la Hermana Laura Melena, quienes anhelaban acompañar a la comunidad y que las voces de la gente indígena se escuchen.\n\nDesde ese año nació La Voz de Guamote 1520 AM, Ayllukunapak Shimi (La Voz de las Familias).",
        
        // ===== PLATAFORMAS DIGITALES =====
        listOf("digital", "internet", "online", "facebook", "instagram", "tiktok", "youtube", "web", "página", "pagina", "redes", "plataforma") to 
            "🌐 La Voz de Guamote fortaleció su presencia en plataformas digitales en el 2022, gracias al apoyo del P. Edwin Moscoso, S.J. y muchos otros sacerdotes.\n\n📱 Encuéntranos en:\n• Facebook: La Voz de Guamote\n• Instagram: @lavozdeguamote_\n• TikTok: @lavozdeguamote\n• YouTube: La Voz de Guamote\n• Web: www.lavozdeguamote.org",
        
        // ===== HORARIOS DE ATENCIÓN =====
        listOf("horario", "atención", "atencion", "hora", "abierto", "trabajan", "oficina", "atienden") to 
            "🕐 Horarios de atención:\n\n📅 Mañanas: 8:00 AM - 12:00 PM\n📅 Tardes: 2:00 PM - 6:00 PM\n\n¡Te esperamos!",
        
        // ===== ENTREVISTAS =====
        listOf("entrevista", "entrevistar", "participar", "invitado", "invitar") to 
            "🎙️ ¡Con todo gusto te atendemos para una entrevista! Comunícate a:\n📞 0989432206\n📞 0991280714",
        
        // ===== CONTACTO GENERAL =====
        listOf("teléfono", "telefono", "llamar", "contacto", "número", "numero", "whatsapp", "celular", "comunicar") to 
            "📞 Contáctanos:\n\n📱 0989432206\n📱 0991280714\n\n📍 Ubicación: Comunidad Santa Cruz de Guamote\n🕐 Horario: 8:00-12:00 y 14:00-18:00",
        
        // ===== FRECUENCIA =====
        listOf("frecuencia", "sintonizar", "am", "dial", "escuchar", "1520") to 
            "📻 Sintonízanos en 1520 AM - La Voz de Guamote, Ayllukunapak Shimi.\n\n🌐 También puedes escucharnos en línea en www.lavozdeguamote.org y todas nuestras redes sociales.",
        
        // ===== LUGARES TURÍSTICOS =====
        listOf("turismo", "turístico", "turistico", "visitar", "conocer", "lugar", "atractivo", "paseo", "tour") to 
            "🗺️ Lugares turísticos de Guamote:\n\n🛒 Feria de Guamote (jueves, una de las más grandes del país)\n⛪ Iglesia Matriz San Pedro de Guamote\n🏔️ El Mirador de Utsubuj\n🏜️ Las Dunas de Arena\n🏞️ Lagunas de Atillo\n🌉 El Puente Negro\n🕳️ La Cueva de Luterano\n\n¡Te invitamos a conocer nuestra hermosa tierra!",
        
        // ===== FERIA DE GUAMOTE =====
        listOf("feria", "mercado", "jueves", "compras") to 
            "🛒 La famosa Feria de Guamote se realiza todos los JUEVES. Es una de las ferias indígenas más grandes del Ecuador.\n\n📍 Se encuentra en el centro de Guamote\n🕕 Comienza desde las 6:00 AM\n\n¡Ven a disfrutar de productos frescos, artesanías y la cultura de nuestro pueblo!",
        
        // ===== LAGUNAS DE ATILLO =====
        listOf("laguna", "atillo", "naturaleza", "paisaje") to 
            "🏞️ Las Lagunas de Atillo son un conjunto de hermosas lagunas de origen glaciar ubicadas en el Parque Nacional Sangay.\n\n📍 A aproximadamente 1 hora de Guamote\n🎣 Ideales para pesca deportiva y caminatas\n🦅 Gran diversidad de aves y flora andina\n\n¡Un paraíso natural que debes conocer!",
        
        // ===== COMIDA TÍPICA =====
        listOf("comida", "gastronomía", "gastronomia", "plato", "típico", "tipico", "comer", "restaurante", "cuy", "hornado") to 
            "🍽️ Comida típica de Guamote:\n\n🐷 Hornado\n🐹 Cuy asado\n🥔 Papas con cuy o fritada\n🍲 Caldo de gallina criolla\n🌽 Mote con chicharrón\n🫓 Tortillas de papa\n\n¡Delicias que debes probar cuando nos visites!",
        
        // ===== FIESTAS =====
        listOf("fiesta", "celebración", "celebracion", "festividad", "carnaval", "tradición", "tradicion", "costumbre") to 
            "🎉 Fiestas más importantes de Guamote:\n\n🎭 Carnaval de Guamote (febrero/marzo) - Famoso por sus comparsas y juegos tradicionales\n✝️ Semana Santa - Procesiones y tradiciones religiosas\n🏛️ Fiestas de Cantonización (1 de agosto)\n🕯️ Día de los Difuntos (2 de noviembre) - Con la tradicional colada morada y guaguas de pan\n👶 Pases del Niño (diciembre) - Celebraciones navideñas comunitarias\n\n¡Te invitamos a vivir nuestras tradiciones!",
        
        // ===== IGLESIA =====
        listOf("iglesia", "matriz", "san pedro", "templo", "misa") to 
            "⛪ La Iglesia Matriz San Pedro de Guamote es un hermoso templo ubicado en el centro del cantón.\n\n📍 Ubicación: Parque Central de Guamote\n🕐 Misas: Consultar horarios locales\n\nEs un símbolo de la fe y la historia de nuestro pueblo.",
        
        // ===== MIRADOR =====
        listOf("mirador", "utsubuj", "vista", "panorámica", "panoramica") to 
            "🏔️ El Mirador de Utsubuj ofrece una vista panorámica espectacular del cantón Guamote y sus alrededores.\n\n📍 Ideal para fotografía y contemplación\n🌄 Hermosos atardeceres\n\n¡Un lugar imperdible para los visitantes!",
        
        // ===== DUNAS =====
        listOf("dunas", "arena", "desierto") to 
            "🏜️ Las Dunas de Arena de Guamote son un paisaje único en los Andes ecuatorianos.\n\n📍 Formaciones de arena natural\n📸 Perfecto para fotografía\n\n¡Un lugar sorprendente que contrasta con el paisaje andino!",
        
        // ===== PUENTE NEGRO =====
        listOf("puente", "negro", "ferrocarril", "tren") to 
            "🌉 El Puente Negro es una estructura histórica del antiguo ferrocarril ecuatoriano.\n\n🚂 Parte de la ruta del tren más difícil del mundo\n📜 Patrimonio histórico\n\n¡Un testimonio de la ingeniería y la historia del Ecuador!",
        
        // ===== CUEVA =====
        listOf("cueva", "luterano", "caverna") to 
            "🕳️ La Cueva de Luterano es una formación natural con historia y leyendas locales.\n\n📍 Un sitio de aventura y misterio\n🔦 Se recomienda ir con guía local\n\n¡Descubre los secretos de esta cueva!",
        
        // ===== KICHWA =====
        listOf("kichwa", "quichua", "idioma", "lengua", "indígena", "indigena", "ayllukunapak") to 
            "🗣️ 'Ayllukunapak Shimi' significa 'La Voz de las Familias' en Kichwa.\n\nLa Voz de Guamote transmite en español y kichwa para servir a toda nuestra comunidad. El kichwa es nuestra lengua ancestral y la preservamos con orgullo.\n\n¡Ñukanchik shimiwan parlanchik! (¡Hablamos en nuestra lengua!)",
        
        // ===== SALUDOS =====
        listOf("hola", "buenos", "buenas", "saludos", "qué tal", "que tal") to 
            "¡Imanalla! (¡Hola en Kichwa!) 👋\n\nSoy el asistente virtual de La Voz de Guamote 1520 AM, Ayllukunapak Shimi.\n\n¿En qué puedo ayudarte? Puedes preguntarme sobre:\n• 📻 Programas y horarios\n• 📞 Contacto y ubicación\n• 💰 Publicidad y transmisiones\n• 🗺️ Turismo en Guamote\n• 🍽️ Comida típica\n• 🎉 Fiestas y tradiciones",
        
        listOf("gracias", "thanks", "agradezco") to 
            "¡Yupaychani! (¡Gracias en Kichwa!) 🙏\n\nFue un gusto ayudarte. Si tienes más preguntas, aquí estaré.\n\n📻 La Voz de Guamote 1520 AM - Ayllukunapak Shimi",
        
        listOf("adios", "chao", "bye", "hasta luego", "nos vemos") to 
            "¡Kayakaman! (¡Hasta luego en Kichwa!) 👋\n\nGracias por comunicarte con La Voz de Guamote.\n\n📻 Sintonízanos en 1520 AM\n🌐 www.lavozdeguamote.org\n\n¡Que te vaya bien!",
        
        // ===== AYUDA =====
        listOf("ayuda", "help", "opciones", "qué puedo", "que puedo", "información", "informacion") to 
            "🤖 ¡Hola! Soy tu asistente de La Voz de Guamote. Puedo ayudarte con:\n\n📻 Programación de la radio\n📍 Ubicación y contacto\n💰 Precios de publicidad\n🎙️ Transmisiones y coberturas\n📱 Redes sociales\n🕐 Horarios de atención\n🗺️ Lugares turísticos de Guamote\n🍽️ Comida típica\n🎉 Fiestas y tradiciones\n📜 Historia de la radio\n\n¡Pregúntame lo que necesites!"
    )

    fun addWelcomeMessage(welcomeText: String) {
        val welcome = ChatMessage(
            id = 0,
            text = "¡Imanalla! 👋 Soy el asistente virtual de La Voz de Guamote 1520 AM, Ayllukunapak Shimi.\n\n¿En qué puedo ayudarte hoy? Puedes preguntarme sobre programación, contacto, publicidad, turismo, comida típica, fiestas y más.",
            isBot = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = listOf(welcome)
    }

    fun sendMessage(text: String, language: String, isVoice: Boolean = false) {
        shouldSpeak = isVoice

        // Agregar mensaje del usuario
        val userMessage = ChatMessage(
            id = _messages.value.size + 1,
            text = text,
            isBot = false,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage

        _isLoading.value = true

        viewModelScope.launch {
            // Simular delay de procesamiento
            delay(600 + (Math.random() * 500).toLong())

            val response = generateResponse(text.lowercase(), language)
            
            val botMessage = ChatMessage(
                id = _messages.value.size + 1,
                text = response,
                isBot = true,
                timestamp = System.currentTimeMillis()
            )
            
            _messages.value = _messages.value + botMessage
            _lastBotResponse.value = response
            _isLoading.value = false
        }
    }

    private fun generateResponse(query: String, language: String): String {
        // Normalizar la consulta (quitar tildes para mejor matching)
        val normalizedQuery = normalizeText(query)
        
        // Buscar en la base de conocimiento local
        for ((keywords, response) in knowledgeBase) {
            val normalizedKeywords = keywords.map { normalizeText(it) }
            if (normalizedKeywords.any { normalizedQuery.contains(it) }) {
                return if (language == "qu") {
                    translateToKichwa(response)
                } else {
                    response
                }
            }
        }

        // Respuesta por defecto
        return when (language) {
            "qu" -> "Mana chay tapuyta yachanichu. 🤔\n\nKaykunamanta tapuway:\n• Programación\n• Contacto\n• Publicidad\n• Turismo\n• Mikuna (comida)\n• Raymi (fiestas)"
            "en" -> "I'm not sure about that. 🤔\n\nTry asking about:\n• Programming\n• Contact\n• Advertising\n• Tourism\n• Typical food\n• Festivals"
            else -> "No tengo información específica sobre eso. 🤔\n\nPuedes preguntarme sobre:\n• 📻 Programación de la radio\n• 📞 Contacto y ubicación\n• 💰 Publicidad y transmisiones\n• 🗺️ Lugares turísticos\n• 🍽️ Comida típica\n• 🎉 Fiestas de Guamote\n\nO llámanos al 0989432206 / 0991280714"
        }
    }

    private fun normalizeText(text: String): String {
        return text
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace("ü", "u")
    }

    private fun translateToKichwa(spanish: String): String {
        // Traducciones básicas al Kichwa
        return spanish
            .replace("Buenos días", "Alli puncha")
            .replace("Gracias", "Yupaychani")
            .replace("Hola", "Imanalla")
            .replace("Hasta luego", "Kayakaman")
            .replace("¿En qué puedo ayudarte?", "¿Imatatak yanapasha?")
    }
}

data class ChatMessage(
    val id: Int = 0,
    val text: String,
    val isBot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
