package levkaantonov.com.study.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import levkaantonov.com.study.training.databinding.FragmentBlankBinding


class BlankFragment : Fragment() {

    private val binding by viewBinding(FragmentBlankBinding::inflate)
    private val argOne by args<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tv.text = argOne
        binding.btn.setOnClickListener {
            Toast.makeText(requireContext(), argOne, Toast.LENGTH_SHORT).show()
        }
    }
}