package ru.javacat.nework.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.javacat.nework.BuildConfig
import ru.javacat.nework.BuildConfig.BASE_URL
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardPostBinding
import ru.javacat.nework.dto.Post


interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}

const val BASE_URL = "http://10.0.2.2:9999"

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
): ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {


        fun bind(post: Post) {
            getAvatars(post,binding)
            if (post.attachment != null){
                binding.attachImage.visibility = View.VISIBLE
                getAttachment(post, binding)
            } else binding.attachImage.visibility = View.GONE

            binding.apply {
                //avatar.loadCircleCrop("${ru.javacat.nework.adapter.BASE_URL}/avatars/${post.authorAvatar}")
                name.text = post.author
                published.text = post.published
                content.text = post.content
                // в адаптере
                likeBtn.isChecked = post.likedByMe
                likeBtn.text = "${post.likes}"


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

fun getAvatars(post: Post, binding: CardPostBinding){
    Glide.with(binding.avatar)
        .load("${ru.javacat.nework.adapter.BASE_URL}/avatars/${post.authorAvatar}")
        .placeholder(R.drawable.ic_baseline_account_circle_24)
        .error(R.drawable.ic_baseline_account_circle_24)
        .circleCrop()
        .timeout(10_000)
        .into(binding.avatar)
}

fun getAttachment(post: Post, binding: CardPostBinding){
    Glide.with(binding.attachImage)
        .load("${ru.javacat.nework.adapter.BASE_URL}/images/${post.attachment?.url}")
        .error(R.drawable.ic_baseline_close_24)
        .timeout(10_000)
        .into(binding.attachImage)
}



class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}