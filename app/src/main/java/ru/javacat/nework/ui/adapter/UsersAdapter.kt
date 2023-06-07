package ru.javacat.nework.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardUserBinding
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.util.loadCircleCrop


class UsersAdapter(
    private val onUserListener: OnUserListener
): ListAdapter<User, UserViewHolder>(UsersDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding,onUserListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserListener: OnUserListener
    ): RecyclerView.ViewHolder(binding.root) {
        private val cardView = binding.root

    fun bind(user: User){

        binding.apply {
            userName.text = user.name
            userAvatar.loadCircleCrop(user.avatar.toString())
            favIcon.isVisible = user.favoured

        }

        binding.userName.setOnClickListener {
            cardView.isChecked = !cardView.isChecked
            onUserListener.onTouch(user)
        }
    }
}

class UsersDiffCallback: DiffUtil.ItemCallback<User>(){
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}

interface OnUserListener {
    fun onTouch(user: User) {}
}
