package ru.javacat.nework.ui.adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel

import java.text.SimpleDateFormat
import java.util.Date


interface OnInteractionListener {
    fun onLike(post: PostModel) {}
    fun onEdit(post: PostModel) {}
    fun onRemove(post: PostModel) {}
    fun onShare(post: PostModel) {}
    fun onResave(post: PostModel) {}
    fun onPlayAudio(post: PostModel) {}
    fun onPlayVideo(post: PostModel) {}
}


class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
): PagingDataAdapter<PostModel, PostViewHolder>(PostDiffCallback()) {
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
            getAvatars(post,binding)
                if (post.attachment != null) {
                        when (post.attachment!!.type) {
                            AttachmentType.IMAGE -> {
                                binding.attachImage.visibility = View.VISIBLE
                                getAttachment(post, binding)
                            }
                            AttachmentType.VIDEO -> {

                            }
                            AttachmentType.AUDIO -> {

                            }
                        }
                    } else binding.attachImage.visibility = View.GONE



            binding.apply {
                name.text = post.author
                published.text = post.published.toString()
                content.text = post.content
                // в адаптере
                likeBtn.isChecked = post.likedByMe
                likeBtn.text = "${post.likeOwnerIds?.size}"
                if (post.coords != null) {
                    locationTextView.text = "${post.coords?.toString()}"
                }

                attachAudio.isVisible = post.attachment?.type == AttachmentType.AUDIO
                videoGroup.isVisible = post.attachment?.type == AttachmentType.VIDEO

                attachAudio.setOnClickListener {
                    onInteractionListener.onPlayAudio(post)
                }
                videoPlayBtn.setOnClickListener {
                    videoPlayBtn.isVisible = false

                    attachVideo.apply {
                        setMediaController(MediaController(context))
                        setVideoURI(
                            Uri.parse(post.attachment?.url)
                        )
                        setOnPreparedListener {
                            start()
                        }
                        setOnCompletionListener {
                            stopPlayback()
                            videoPlayBtn.isVisible = true
                        }
                    }
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

                likeBtn.setOnClickListener {
                    onInteractionListener.onLike(post)
                }

                shareBtn.setOnClickListener {
                    onInteractionListener.onShare(post)
                }
            }
        }
    }

fun getAvatars(post: PostModel, binding: CardPostBinding){
    Glide.with(binding.avatar)
        .load("${post.authorAvatar}")
        .placeholder(R.drawable.ic_baseline_account_circle_24)
        .error(R.drawable.ic_baseline_account_circle_24)
        .circleCrop()
        .timeout(10_000)
        .into(binding.avatar)
}

fun getAttachment(post: PostModel, binding: CardPostBinding){
    Glide.with(binding.attachImage)
        .load("${post.attachment?.url}")
        .error(R.drawable.ic_baseline_close_24)
        .timeout(10_000)
        .into(binding.attachImage)
}

fun playVideo(post: PostModel, url: String){

}

//fun getDate(date: String): Date? {
//    val format = SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss'Z'")
//    return format.parse(date)
//}


class PostDiffCallback : DiffUtil.ItemCallback<PostModel>() {
    override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }
}