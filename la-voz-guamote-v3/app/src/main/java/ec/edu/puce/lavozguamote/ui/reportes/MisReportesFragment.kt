package ec.edu.puce.lavozguamote.ui.reportes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ec.edu.puce.lavozguamote.R

class MisReportesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textView = TextView(context).apply {
            text = "Próximamente: Lista de tus reportes enviados"
            textSize = 16f
            setPadding(32, 32, 32, 32)
            setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        return textView
    }
}
