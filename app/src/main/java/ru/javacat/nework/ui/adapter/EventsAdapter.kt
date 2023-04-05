package ru.javacat.nework.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardEventBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadCircleCrop

interface OnEventsListener {
    fun onLike(event: EventModel) {}
    fun onEdit(event: EventModel) {}
    fun onRemove(event: EventModel) {}
    fun onShare(event: EventModel) {}
    fun onPlayAudio(event: EventModel)
}
class EventsAdapter(
    private val onEventsListener: OnEventsListener
):ListAdapter<EventModel, EventViewHolder>(EventsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EventViewHolder(binding, onEventsListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }

}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventsListener: OnEventsListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(event: EventModel){
        if (event.attachment != null){
            binding.attachLayout.root.visibility = View.VISIBLE
            when (event.attachment.type) {
                AttachmentType.IMAGE -> {
                    binding.attachLayout.attachImage.load(event.attachment.url)
                }
                AttachmentType.VIDEO -> {

                }
                AttachmentType.AUDIO -> {

                }
            }
        } else {binding.attachLayout.root.visibility = View.GONE}

        binding.apply {
            avatar.loadCircleCrop(event.authorAvatar.toString())
            name.text = event.author
            published.text = event.published.asString()
            content.text = event.content
            dateOfEvent.text = event.datetime.asString()
            locationOfEvent.text = event.coords.toString()
            typeOfEvent.text = event.type.toString()
            //likes:
            likeBtn.isChecked = event.likedByMe
            likeBtn.text = "${event.likeOwnerIds?.size?: ""}"

            //speakers:
            if (event.speakerIds.isNotEmpty()) {
                speakers.text = event.speakerIds.map {
                    event.users[it]?.name }.joinToString(", ")

            }

            //participants:
            if (event.participantsIds.isNotEmpty()) {
                participants.text = event.participantsIds.map {
                    event.users[it]?.name
                }.joinToString ( ", " )
            }

            //image
            attachLayout.attachImage.isVisible = event.attachment?.type == AttachmentType.IMAGE

            // audio:
            attachLayout.attachAudio.isVisible = event.attachment?.type == AttachmentType.AUDIO
            attachLayout.attachAudio.setOnClickListener {
                onEventsListener.onPlayAudio(event)
            }

            //video
            attachLayout.videoGroup.isVisible = event.attachment?.type == AttachmentType.VIDEO
            attachLayout.videoPlayBtn.setOnClickListener {
                attachLayout.videoPlayBtn.isVisible = false

                attachLayout.attachVideo.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse(event.attachment?.url)
                    )
                    setOnPreparedListener {
                        start()
                    }
                    setOnCompletionListener {
                        stopPlayback()
                        attachLayout.videoPlayBtn.isVisible = true
                    }
                }
            }

            menu.isVisible = event.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onEventsListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onEventsListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            likeBtn.setOnClickListener {
                onEventsListener.onLike(event)
            }

            shareBtn.setOnClickListener {
                onEventsListener.onShare(event)
            }
        }
    }

}

class EventsDiffCallback: DiffUtil.ItemCallback<EventModel>() {
    override fun areItemsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
        return oldItem == newItem
    }

}



