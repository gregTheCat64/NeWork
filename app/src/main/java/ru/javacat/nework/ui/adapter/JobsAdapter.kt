package ru.javacat.nework.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.javacat.nework.R
import ru.javacat.nework.databinding.CardJobBinding
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.util.asString

class JobsAdapter: ListAdapter<JobModel, JobViewHolder>(JobsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}


class JobViewHolder(
    private val binding: CardJobBinding
) : RecyclerView.ViewHolder(binding.root){
    fun bind(job: JobModel){
        binding.apply {
            jobName.text = job.name
            jobPosition.text = job.position
            jobStart.text = job.start.asString()
            jobEnd.text = job.finish?.asString()
            linkOfJob.text = job.link

            menuBtn.isVisible = job.ownedByMe == true
            menuBtn.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item->
                        when (item.itemId) {
                            R.id.remove -> {

                                true
                            }
                            R.id.edit-> {
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }

    }
}

class JobsDiffCallback(): DiffUtil.ItemCallback<JobModel>() {
    override fun areItemsTheSame(oldItem: JobModel, newItem: JobModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: JobModel, newItem: JobModel): Boolean {
        return oldItem == newItem
    }
}