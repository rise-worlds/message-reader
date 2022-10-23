package com.example.messagereader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messagereader.databinding.FragmentSecondBinding
import org.greenrobot.eventbus.EventBus

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private var mSmsViewModel: SmsViewModel? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        EventBus.getDefault().postSticky(SmsReceiver.NewSmsEvent())

        val recyclerView: RecyclerView = binding.recyclerview
        val adapter = SmsListAdapter(SmsListAdapter.SmsDiff())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        mSmsViewModel = ViewModelProvider(this)[SmsViewModel::class.java]
        mSmsViewModel!!.allSmsItems.observe(viewLifecycleOwner) { items ->
            // Update the cached copy of the words in the adapter.
            adapter.submitList(items)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}