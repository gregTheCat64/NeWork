package ru.javacat.nework.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.javacat.nework.databinding.CardEventBinding
import ru.javacat.nework.domain.model.EventModel

class UserEventsAdapter (
    private val onEventsListener: OnEventsListener
): androidx.recyclerview.widget.ListAdapter<EventModel, EventViewHolder>(EventsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EventViewHolder(binding, onEventsListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }

}

