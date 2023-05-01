package ru.javacat.nework.ui.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar
import ru.javacat.nework.util.loadCircleCrop


interface OnInteractionListener {
    fun onLike(post: PostModel) {}
    fun onEdit(post: PostModel) {}
    fun onRemove(post: PostModel) {}
    fun onShare(post: PostModel) {}
    fun onResave(post: PostModel) {}
    fun onPlayAudio(post: PostModel) {}
    fun onPlayVideo(post: PostModel) {}
    fun onImage(url: String) {}
    fun onUser(post: PostModel) {}
    fun onMention(post: PostModel){}
    fun onCoords(post: PostModel){}
}


class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<PostModel, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: PostModel) {
        if (post.attachment != null) {
            binding.attachLayout.root.visibility = View.VISIBLE
            when (post.attachment.type) {
                AttachmentType.IMAGE -> {
                    binding.attachLayout.attachImage.load(post.attachment.url)
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
            if (post.likeOwnerIds?.size != 0){
                likedList.visibility = View.VISIBLE
                likedList.text = post.likeOwnerIds?.map{
                    post.users[it]?.name
                }?.joinToString (", ", "Оценили: ")
            } else likedList.visibility = View.GONE

            //coords:
            if (post.coords != null) {
                locationBtn.visibility = View.VISIBLE
                //text = "${post.coords?.toString()}"
            } else {
                locationBtn.visibility = View.GONE
            }
            locationBtn.setOnClickListener {
                onInteractionListener.onCoords(post)
            }
            infoLayout.isVisible = post.link?.isNotEmpty() == true || post.coords != null


            //mentions:
            if (post.mentionIds.isNotEmpty()) {
                interactionPosts.mentioned.visibility = View.VISIBLE
                interactionPosts.mentioned.text = post.mentionIds.size.toString()
            } else  interactionPosts.mentioned.visibility = View.GONE

            interactionPosts.mentioned.setOnClickListener {
                onInteractionListener.onMention(post)
            }

            //image
            avatar.setOnClickListener {
                onInteractionListener.onImage(post.authorAvatar.toString())
            }
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
            postInfoHeader.setOnClickListener {
                onInteractionListener.onUser(post)
            }

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


fun playVideo(post: PostModel, url: String) {

}


class PostDiffCallback : DiffUtil.ItemCallback<PostModel>() {
    override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }
}