package co.candyhouse.app.base

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.back_sub.*

open class BaseNFG(layout: Int) : Fragment(layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_zone?.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}