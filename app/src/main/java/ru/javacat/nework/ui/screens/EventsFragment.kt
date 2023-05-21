package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentEventsBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.EventsAdapter
import ru.javacat.nework.ui.adapter.OnEventsListener
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.util.snack
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
                viewModel.edit(event)
                findNavController().navigate(R.id.newEventFragment)
            }

            override fun onRemove(event: EventModel) {
               viewModel.removeById(event.id)
            }

            override fun onTakePartBtn(event: EventModel) {
                if (appAuth.authStateFlow.value.id!=0L){
                    viewModel.takePart(event)
                    if (event.participatedByMe){
                        snack("Вы вышли")
                    } else{
                        snack("Вы участвуете!")
                    }
                }else showSignInDialog(this@EventsFragment)

            }

            override fun onShare(event: EventModel) {
                super.onShare(event)
            }

            override fun onParticipant(event: EventModel) {
                showUserListDialog(event.participantsIds, childFragmentManager)
            }

            override fun onUser(event: EventModel) {
                val bundle = Bundle()
                bundle.putLong("userID", event.authorId)
                //val action = PostsFragmentDirections.actionNavigationPostsToWallFragment(post.authorId)
                findNavController().navigate(R.id.wallFragment, bundle)
            }

            override fun onImage(url: String) {
                showImageDialog(url, childFragmentManager)
            }

            override fun onPlayAudio(event: EventModel) {
                mediaObserver.apply {
                    mediaPlayer?.reset()
                    mediaPlayer?.setDataSource(

                        event.attachment?.url
                    )
                }.play()
            }

            override fun onLocation(event: EventModel) {
                val coords = event.coords
                val bundle = Bundle()
                if (coords != null) {
                    bundle.putDoubleArray("POINT", doubleArrayOf(coords.latitude,coords.longitude))
                }
                findNavController().navigate(R.id.mapsFragment, bundle)
            }
        }
        )

        binding.eventsList.adapter = adapter

        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
//        viewModel.data.observe(viewLifecycleOwner){
//        adapter.submitList(it)
//        }

        lifecycleScope.launchWhenCreated {
            viewModel.data
                .catch { e:Throwable->
                    e.printStackTrace()
                }
                .collectLatest {
                    adapter.submitData(it)
                }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeToRefresh.isRefreshing = it.refresh is LoadState.Loading
                        ||it.append is LoadState.Loading
                        ||it.prepend is LoadState.Loading
            }
        }

//        viewModel.state.observe(viewLifecycleOwner) {
//            binding.swipeToRefresh.isRefreshing = it.refreshing
//                    || it.loading
//        }


        binding.postListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_navigation_posts)
        }

        binding.eventsListBtn.setOnClickListener{
            binding.eventsList.smoothScrollToPosition(0)
        }

        binding.newEventBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_events_to_newEventFragment)
        }

        return binding.root

        
    }
}