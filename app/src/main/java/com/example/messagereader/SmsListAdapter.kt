package com.example.messagereader

import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class SmsListAdapter(@NonNull diffCallback: DiffUtil.ItemCallback<SmsItem>) :
    ListAdapter<SmsItem, SmsViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        return SmsViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val current: SmsItem = getItem(position)
        holder.bind(current)
    }

    internal class WordDiff : DiffUtil.ItemCallback<SmsItem>() {
        override fun areItemsTheSame(@NonNull oldItem: SmsItem, @NonNull newItem: SmsItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(@NonNull oldItem: SmsItem, @NonNull newItem: SmsItem): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
