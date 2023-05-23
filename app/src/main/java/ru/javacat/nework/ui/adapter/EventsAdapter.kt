package ru.javacat.nework.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardEventBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar
import ru.javacat.nework.util.loadCircleCrop

interface OnEventsListener {
    fun onLike(event: EventModel) {}
    fun onEdit(event: EventModel) {}
    fun onRemove(event: EventModel) {}
    fun onShare(event: EventModel) {}
    fun onPlayAudio(event: EventModel){}
    fun onParticipant(event: EventModel){}
    fun onUser(event: EventModel){}

    fun onLiked(event: EventModel){}
    fun onImage(url: String){}

    fun onTakePartBtn(event: EventModel){}

    fun onLocation(event: EventModel){}
}
class EventsAdapter(
    private val onEventsListener: OnEventsListener
):PagingDataAdapter<EventModel, EventViewHolder>(EventsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EventViewHolder(binding, onEventsListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
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
            interactionPosts.takePartBtn.isVisible = true
            avatar.loadAvatar(event.authorAvatar.toString())
            name.text = event.author
            published.text = event.published?.asString()
            content.text = event.content
            dateOfEvent.text = event.datetime?.asString()
            if(event.type == EventType.OFFLINE){
                locationOfEvent.text = event.coords.toString()
            } else{locationOfEvent.text = event.link.toString()}

            locationBtn.isVisible = event.coords != null

            typeOfEvent.text = event.type.toString()

            //onUser:
            avatar.setOnClickListener {
                onEventsListener.onImage(event.authorAvatar.toString())
            }
            eventHeader.setOnClickListener {
                onEventsListener.onUser(event)
            }


            //likes:
            interactionPosts.likeBtn.isChecked = event.likedByMe
            interactionPosts.likeBtn.text = "${event.likeOwnerIds?.size?: ""}"

            //likeOwners:
            if (event.likeOwnerIds?.size != 0){
                likedList.visibility = View.VISIBLE
                likedList.text = event.likeOwnerIds?.map {
                    event.users[it]?.name
                }?.joinToString(", ", "Оценили: ")
            } else likedList.visibility = View.GONE

            likedList.setOnClickListener {
                onEventsListener.onLiked(event)
            }

            //speakers:
            if (event.speakerIds.isNotEmpty()) {
                speakers.text = event.speakerIds.map {
                    event.users[it]?.name }.joinToString(", ")
            } else {
                speakers.text = ""
            }


            //participants:
            if (event.participantsIds.isNotEmpty()) {
                interactionPosts.mentioned.visibility = View.VISIBLE
                interactionPosts.mentioned.text = event.participantsIds.size.toString()
            } else  interactionPosts.mentioned.visibility = View.GONE

            interactionPosts.mentioned.setOnClickListener {
                onEventsListener.onParticipant(event)
            }

            binding.interactionPosts.takePartBtn.setOnClickListener {
                onEventsListener.onTakePartBtn(event)
            }
            binding.interactionPosts.takePartBtn.isChecked = event.participatedByMe

            if (event.participatedByMe){
                binding.interactionPosts.takePartBtn.setText("Out")
            } else {
                binding.interactionPosts.takePartBtn.setText("In")
            }

            //image
            attachLayout.attachImage.isVisible = event.attachment?.type == AttachmentType.IMAGE
            attachLayout.attachImage.setOnClickListener {
                onEventsListener.onImage(event.attachment?.url.toString())
            }
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

            //location
            binding.locationBtn.setOnClickListener {
                onEventsListener.onLocation(event)
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

            interactionPosts.likeBtn.setOnClickListener {
                onEventsListener.onLike(event)
            }

            interactionPosts.shareBtn.setOnClickListener {
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



