package ru.javacat.nework.ui.adapter


import android.util.Log
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
import ru.javacat.nework.databinding.CardPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.load
import ru.javacat.nework.util.loadAvatar


interface OnInteractionListener {
    fun onLike(post: PostModel) {}
    fun onEdit(post: PostModel) {}
    fun onRemove(post: PostModel) {}
    fun onShare(post: PostModel) {}
    fun onResave(post: PostModel) {}
    fun onPlayAudio(post: PostModel) {}
    fun onPlayVideo(url: String) {}
    fun onImage(url: String) {}
    fun onUser(post: PostModel) {}
    fun onMention(post: PostModel){}

    fun onLiked(post: PostModel){}
    fun onCoords(post: PostModel){}

    fun onLink(url: String) {}

    fun onUpBtn() {

    }

}



class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<PostModel, PostViewHolder>(PostDiffCallback()) {
    var isScrolledOver = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position) ?: return

        Log.i("POS", position.toString())
        if (position == 0) {isScrolledOver = false}
        if (position == 10 && !isScrolledOver) {
            isScrolledOver = true
            onInteractionListener.onUpBtn()
        }


        holder.bind(post)
    }
}


class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    private val defaultUserAvatar = AppCompatResources.getDrawable(binding.root.context,
        R.drawable.baseline_account_circle_36
    )


    fun bind(post: PostModel) {
        if (post.attachment!= null && post.attachment.url.startsWith("http", false)){
            binding.attachLayout.root.visibility = View.VISIBLE
            when (post.attachment.type) {
                AttachmentType.IMAGE -> {
                    binding.attachLayout.attachImage.load(post.attachment.url)
                }
                AttachmentType.VIDEO -> {
                    binding.attachLayout.attachVideo.load(post.attachment.url)
                }
                AttachmentType.AUDIO -> {
                }
            }
        }else binding.attachLayout.root.visibility = View.GONE

        binding.apply {
            post.authorAvatar?.let {
                if (it.startsWith("http",false))
                avatar.loadAvatar(it)
            }?:avatar.setImageDrawable(defaultUserAvatar)

            name.text = post.author
            published.text = post.published?.asString()
            content.text = post.content
            linkText.text = post.link
            interactionPosts.takePartBtn.isVisible = false
            interactionPosts.mentioned.setIconResource(R.drawable.baseline_alternate_email_24)

            //likes:
            interactionPosts.likeBtn.isChecked = post.likedByMe //???
            interactionPosts.likeBtn.text = "${post.likeOwnerIds?.size ?: ""}"
            if (post.likeOwnerIds?.size != 0){
                likedList.visibility = View.VISIBLE

                likedList.text = post.likeOwnerIds?.map{
                    if (post.users[it]?.name != null) {post.users[it]?.name} else
                        binding.root.resources.getString(R.string.Me)
                }?.joinToString (", ", binding.root.resources.getString(R.string.Liked)+" ")
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

            likedList.setOnClickListener {
                onInteractionListener.onLiked(post)
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
            //attachLayout.attachAudio.isChecked = post.playBtnPressed
            attachLayout.attachAudio.setOnClickListener {
                onInteractionListener.onPlayAudio(post)
            }

            //video
            attachLayout.videoGroup.isVisible = post.attachment?.type == AttachmentType.VIDEO
            attachLayout.videoPlayBtn.setOnClickListener {
                onInteractionListener.onPlayVideo(post.attachment?.url.toString())
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

            //map
            locationBtn.setOnClickListener {
                onInteractionListener.onCoords(post)
            }

            //link
            linkText.setOnClickListener {
                onInteractionListener.onLink(post.link.toString())
            }
        }
    }
}




class PostDiffCallback : DiffUtil.ItemCallback<PostModel>() {
    override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
        return oldItem == newItem
    }
}