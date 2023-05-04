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
import ru.javacat.nework.databinding.CardPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar
import ru.javacat.nework.util.loadCircleCrop

class UserPostsAdapter(
    private val onInteractionListener: OnInteractionListener
): ListAdapter<PostModel, UserPostsViewHolder>(UserPostsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostsViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserPostsViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: UserPostsViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

}


class UserPostsViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: PostModel) {
        if (post.attachment != null) {
            binding.attachLayout.root.visibility = View.VISIBLE
            when (post.attachment!!.type) {
                AttachmentType.IMAGE -> {
                    binding.attachLayout.attachImage.load(post.attachment!!.url)
                }
                AttachmentType.VIDEO -> {
                }
                AttachmentType.AUDIO -> {
                }
            }
        } else binding.attachLayout.root.visibility = View.GONE

        binding.apply {
            avatar.loadAvatar(post.authorAvatar.toString())
            name.text = post.author
            published.text = post.published?.asString()
            content.text = post.content
            linkText.text = post.link

            //likes:
            interactionPosts.likeBtn.isChecked = post.likedByMe //???
            interactionPosts.likeBtn.text = "${post.likeOwnerIds?.size ?: ""}"

            //coords:
            if (post.coords != null) {
                locationBtn.visibility = View.VISIBLE
                //text = "${post.coords?.toString()}"
            } else {
                locationBtn.visibility = View.GONE
            }

            infoLayout.isVisible = post.link?.isNotEmpty() == true || post.coords != null

            //mentions:
//            if (post.mentionIds.isNotEmpty()) {
//                mentionIds.text = post.mentionIds.map {
//                    post.users[it]?.name
//                }.joinToString (", ")
//            } else mentionIds.text = ""

            //image
            attachLayout.attachImage.isVisible = post.attachment?.type == AttachmentType.IMAGE
            attachLayout.attachImage.setOnClickListener {
                onInteractionListener.onImage(post.attachment?.url.toString())
            }

            // audio:
            attachLayout.attachAudio.isVisible = post.attachment?.type == AttachmentType.AUDIO
            attachLayout.attachAudio.isChecked = post.playBtnPressed
            attachLayout.attachAudio.setOnClickListener {
                onInteractionListener.onPlayAudio(post)
            }

            //video
            attachLayout.videoGroup.isVisible = post.attachment?.type == AttachmentType.VIDEO
            attachLayout.videoPlayBtn.setOnClickListener {
                attachLayout.videoPlayBtn.isVisible = false

                attachLayout.attachVideo.apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse(post.attachment?.url)
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

            //onUserTouch
            avatar.setOnClickListener {
                onInteractionListener.onUser(post)
            }

//                onServer.setOnClickListener {
//                    if (!post.savedOnServer){
//                        onInteractionListener.onResave(post)
//                    }
//                }

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            interactionPosts.likeBtn.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            interactionPosts.shareBtn.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class UserPostsDiffCallback:DiffUtil.ItemCallback<PostModel>() {
    override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }
}