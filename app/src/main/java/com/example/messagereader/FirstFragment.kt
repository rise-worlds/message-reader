package com.example.messagereader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.messagereader.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sp = this.context?.getSharedPreferences("config",
            AppCompatActivity.MODE_PRIVATE
        )

        if (sp != null) {
            val deviceSerial = sp.getString("DeviceSerial", "")
            binding.editDeviceId.setText(deviceSerial)
            val phoneNumber = sp.getString("DevicePhoneNumber", "")
            binding.textviewPhone.text = phoneNumber
        }

        binding.buttonFirst.setOnClickListener {
            val deviceSerial = binding.editDeviceId.text.toString()
            if (deviceSerial.isEmpty()) {
                return@setOnClickListener
            }
            if (sp != null) {
                val editor = sp.edit()
                editor.putString("DeviceSerial", deviceSerial)
                editor.apply()
            }
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}