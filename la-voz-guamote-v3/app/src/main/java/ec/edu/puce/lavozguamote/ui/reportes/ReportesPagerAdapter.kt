package ec.edu.puce.lavozguamote.ui.reportes

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReportesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NuevoReporteFragment()
            1 -> MisReportesFragment()
            else -> NuevoReporteFragment()
        }
    }
}
