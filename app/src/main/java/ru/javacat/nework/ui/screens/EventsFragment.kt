package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentEventsBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.EventsAdapter
import ru.javacat.nework.ui.adapter.OnEventsListener
import ru.javacat.nework.ui.viewmodels.EventViewModel
import javax.inject.Inject

class EventsFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()

    private val mediaObserver = MediaLifecycleObserver()
    @Inject
    lateinit var appAuth: AppAuth



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        lifecycle.addObserver(mediaObserver)

        val adapter = EventsAdapter(object : OnEventsListener{
            override fun onLike(event: EventModel) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    viewModel.likeById(event.id)
                } else Toast.makeText(context, "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            }

            override fun onEdit(event: EventModel) {
                super.onEdit(event)
            }

            override fun onRemove(event: EventModel) {
                super.onRemove(event)
            }

            override fun onShare(event: EventModel) {
                super.onShare(event)
            }

            override fun onPlayAudio(event: EventModel) {
                mediaObserver.apply {
                    mediaPlayer?.reset()
                    mediaPlayer?.setDataSource(

                        event.attachment?.url
                    )
                }.play()
            }
        }
        )

        binding.eventsList.adapter = adapter

        binding.swipeToRefresh.setOnRefreshListener {
            viewModel.loadEvents()
        }
        viewModel.data.observe(viewLifecycleOwner){
        adapter.submitList(it)
        }


        binding.postListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_navigation_posts)
        }

        binding.eventsListBtn.setOnClickListener{
            // TODO: обновить список 
        }

        binding.newEventBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_newEventFragment)
        }
       
       
        return binding.root



        
    }

  
}