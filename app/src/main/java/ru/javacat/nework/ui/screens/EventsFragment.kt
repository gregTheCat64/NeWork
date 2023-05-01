package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentEventsBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.EventsAdapter
import ru.javacat.nework.ui.adapter.OnEventsListener
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.util.showSignInDialog
import javax.inject.Inject

@AndroidEntryPoint
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
                if (appAuth.authStateFlow.value.id!=0L){
                    viewModel.likeById(event.id)
                }else showSignInDialog(this@EventsFragment)
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

            override fun onParticipant(event: EventModel) {
                showUserListDialog(event.participantsIds, childFragmentManager)
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

        viewModel.state.observe(viewLifecycleOwner) {
            binding.swipeToRefresh.isRefreshing = it.refreshing
                    || it.loading
        }


        binding.postListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_navigation_posts)
        }

        binding.eventsListBtn.setOnClickListener{
            viewModel.loadEvents()
        }

        binding.newEventBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_newEventFragment)
        }

        return binding.root

        
    }
}