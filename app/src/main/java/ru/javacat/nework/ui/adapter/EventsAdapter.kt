package ru.javacat.nework.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardEventBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar
import ru.javacat.nework.util.setDateToPost

interface OnEventsListener {
    fun onLike(event: EventModel) {}
    fun onEdit(event: EventModel) {}
    fun onRemove(event: EventModel) {}
    fun onShare(event: EventModel) {}
    fun onPlayAudio(event: EventModel){}

    fun onPlayVideo(url: String){}
    fun onParticipant(ids: List<Long>){}
    fun onUser(event: EventModel){}

    fun onLiked(event: EventModel){}
    fun onImage(url: String){}

    fun onTakePartBtn(event: EventModel){}

    fun onLocation(event: EventModel){}

    fun onLink(url: String){}

    fun makeUpBtn() {}

    fun clearUpBtn() {}
}
class EventsAdapter(
    private val onEventsListener: OnEventsListener
):PagingDataAdapter<EventModel, EventViewHolder>(EventsDiffCallback()) {
    var isScrolledOver = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EventViewHolder(binding, onEventsListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        if (position == 10 && !isScrolledOver) {
            isScrolledOver = true
            onEventsListener.makeUpBtn()
        }

        if (position<4) {
            onEventsListener.clearUpBtn()
            isScrolledOver = false
        }
        holder.bind(event)
    }

}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventsListener: OnEventsListener
): RecyclerView.ViewHolder(binding.root) {
    private val defaultUserAvatar = AppCompatResources.getDrawable(binding.root.context,
        R.drawable.baseline_account_circle_36
    )
    fun bind(event: EventModel){
        if (event.attachment != null && event.attachment.url.startsWith("http", false)){
            binding.attachLayout.root.visibility = View.VISIBLE
            when (event.attachment.type) {
                AttachmentType.IMAGE -> {
                    binding.attachLayout.attachImage.load(event.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    binding.attachLayout.attachVideo.load(event.attachment.url)
                }
                AttachmentType.AUDIO -> {

                }
            }
        } else {binding.attachLayout.root.visibility = View.GONE}

        binding.apply {
            interactionPosts.takePartBtn.isVisible = true
            event.authorAvatar?.let {
                if (it.startsWith("http", false))
                    avatar.loadAvatar(it)
            }?:avatar.setImageDrawable(defaultUserAvatar)
            name.text = event.author
            published.text = setDateToPost(event.published)
            content.text = event.content
            dateOfEvent.text = event.datetime?.asString()
            interactionPosts.mentioned.setIconResource(R.drawable.ic_baseline_people_24)

            if(event.link != null){
                webLayout.isVisible = true
                webText.text = event.link.toString()
            } else{ webLayout.isVisible = false}


            locationBtn.isVisible = event.coords!=null
            webBtn.isVisible = event.link !=null



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
                    if (event.users[it]?.name != null) {event.users[it]?.name} else
                        binding.root.resources.getString(R.string.Me)
                }?.joinToString(", ", binding.root.resources.getString(R.string.Liked)+" ")
            } else likedList.visibility = View.GONE

            likedList.setOnClickListener {
                onEventsListener.onLiked(event)
            }

            //speakers:
            if (event.speakerIds.isNotEmpty()) {
                binding.speakersLayout.isVisible = true
                speakers.text = event.speakerIds.map {
                    event.users[it]?.name }.joinToString(", ")
            } else {
                binding.speakersLayout.isVisible = false
            }
            binding.speakers.setOnClickListener {
                onEventsListener.onParticipant(event.speakerIds)
            }


            //participants:
            if (event.participantsIds.isNotEmpty()) {
                interactionPosts.mentioned.visibility = View.VISIBLE
                interactionPosts.mentioned.text = event.participantsIds.size.toString()
            } else  interactionPosts.mentioned.visibility = View.GONE

            interactionPosts.mentioned.setOnClickListener {
                onEventsListener.onParticipant(event.participantsIds)
            }

            binding.interactionPosts.takePartBtn.setOnClickListener {
                onEventsListener.onTakePartBtn(event)
            }
            binding.interactionPosts.takePartBtn.isChecked = event.participatedByMe

            if (event.participatedByMe){
                binding.interactionPosts.takePartBtn.text = "Out"
            } else {
                binding.interactionPosts.takePartBtn.text = "In"
            }

            //image
            attachLayout.attachImage.isVisible = event.attachment?.type == AttachmentType.IMAGE
            attachLayout.attachImage.setOnClickListener {
                onEventsListener.onImage(event.attachment?.url.toString())
            }
            // audio:
            attachLayout.attachAudio.isVisible = event.attachment?.type == AttachmentType.AUDIO
            attachLayout.attachAudio.isChecked = event.playBtnPressed
            attachLayout.attachAudio.setOnClickListener {
                onEventsListener.onPlayAudio(event)
            }

            //video
            attachLayout.videoGroup.isVisible = event.attachment?.type == AttachmentType.VIDEO
            attachLayout.videoPlayBtn.setOnClickListener {
               onEventsListener.onPlayVideo(event.attachment?.url.toString())

                attachLayout.attachVideo.apply {

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

            webText.setOnClickListener {
                onEventsListener.onLink(event.link.toString())
            }

            webBtn.setOnClickListener {
                onEventsListener.onLink(event.link.toString())
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



