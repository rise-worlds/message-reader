package com.example.messagereader

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messagereader.databinding.FragmentSecondBinding
import okhttp3.internal.notify
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private lateinit var adapter: SmsListAdapter
    private lateinit var mSmsViewModel: SmsViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        val recyclerView: RecyclerView = binding.recyclerview
        adapter = SmsListAdapter(SmsListAdapter.SmsDiff())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        mSmsViewModel = ViewModelProvider(this)[SmsViewModel::class.java]
        mSmsViewModel.allSmsItems.observe(viewLifecycleOwner) { items ->
            // Update the cached copy of the words in the adapter.
            adapter.submitList(items)
        }

        EventBus.getDefault().postSticky(SmsReceiver.NewSmsEvent())
        EventBus.getDefault().register(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true)
    fun handleEvent(event: SmsReceiver.UpdateSmsListEvent) {
        mSmsViewModel.update()
        adapter.update()

        // prevent event from re-delivering, like when leaving and coming back to app
        EventBus.getDefault().removeStickyEvent(event)
    }
}