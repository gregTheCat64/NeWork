package ru.javacat.nework.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentWallBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.*
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.JobsViewModel
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.WallViewModel
import ru.javacat.nework.util.loadCircleCrop
import ru.javacat.nework.util.showSignInDialog
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {

    private val jobsViewModel: JobsViewModel by viewModels()
    private val wallViewModel: WallViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()

    private val mediaObserver = MediaLifecycleObserver()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWallBinding.inflate(inflater)

        val args = arguments
        val authorId = args?.getLong("userID", 0L) ?: 0L


        binding.addJobBtn.isVisible = authorId == appAuth.getId()

        var expandedPosts = false
        var expandedEvents = false
        var expandedJobs = false

        val jobsAdapter = JobsAdapter()

        //jobsAdapter:
        binding.jobsList.adapter = jobsAdapter

        jobsViewModel.userJobs.observe(viewLifecycleOwner) {
            jobsAdapter.submitList(it)
        }
        binding.jobsListBtn.setOnClickListener {
            jobsViewModel.getJobsByUserId(authorId)

            expandedJobs = !expandedJobs
            binding.jobsList.isVisible = expandedJobs
//            binding.postsList.isVisible = false
//            binding.eventsList.isVisible = false
        }


        //postAdapter:
        val postAdapter = UserPostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    postViewModel.likeById(post.id)
                } else showSignInDialog(this@WallFragment)
            }

            override fun onEdit(post: PostModel) {
                super.onEdit(post)
            }

            override fun onRemove(post: PostModel) {
                postViewModel.removeById(post.id)
            }

            override fun onShare(post: PostModel) {
                super.onShare(post)
            }

            override fun onResave(post: PostModel) {
                super.onResave(post)
            }

            override fun onPlayAudio(post: PostModel) {
                post.playBtnPressed = !post.playBtnPressed
                mediaObserver.apply {
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer?.reset()
                        mediaPlayer?.setDataSource(post.attachment?.url)
                        this.play()
                    } else {
                        mediaPlayer!!.pause()
                    }
                }
            }

            override fun onPlayVideo(post: PostModel) {
                super.onPlayVideo(post)
            }

            override fun onImage(url: String) {
                showImageDialog(url, parentFragmentManager)
            }

            override fun onUser(post: PostModel) {
                super.onUser(post)
            }

            override fun onMention(post: PostModel) {
                showUserListDialog(post.mentionIds, parentFragmentManager)
            }

            override fun onCoords(post: PostModel) {
                super.onCoords(post)
            }
        })


        binding.postsList.adapter = postAdapter

        wallViewModel.userPosts.observe(viewLifecycleOwner) {
            postAdapter.submitList(it)
        }

        binding.postListBtn.setOnClickListener {
            wallViewModel.loadPostsByAuthorId(authorId)
            expandedPosts = !expandedPosts
//            binding.jobsList.isVisible = false
//            binding.eventsList.isVisible = false
            binding.postsList.isVisible = expandedPosts
        }

        wallViewModel.getUserById(authorId)

        wallViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.avatar?.let { binding.avatar.loadCircleCrop(it) }
            user?.name?.let { binding.name.text = it }
        }

        val eventsAdapter = EventsAdapter(object : OnEventsListener {
        })

        binding.eventsList.adapter = eventsAdapter

        eventViewModel.dataByAuthor.observe(viewLifecycleOwner) {
            eventsAdapter.submitList(it)
        }

        binding.eventsListBtn.setOnClickListener {
            eventViewModel.getByAuthorId(authorId)
            expandedEvents = !expandedEvents
//            binding.jobsList.isVisible = false
//            binding.postsList.isVisible = false
            binding.eventsList.isVisible = expandedEvents
        }

        binding.addJobBtn.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }


        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

//        binding.swipeToRefresh.setOnRefreshListener {
//            //binding.postsList.smoothScrollToPosition(0)
//            wallViewModel.updatePostsByAuthorId(authorId)
//        }

//        binding.jobsListBtn.setOnClickListener {
//            val action = WallFragmentDirections.actionWallFragmentToJobsFragment(authorId)
//            findNavController().navigate(action)
//        }

//        binding.swipeToRefresh.setOnRefreshListener {
//            jobsViewModel.getJobsByUserId(authorId)
//        }
//
//        jobsViewModel.data.observe(viewLifecycleOwner){
//           // adapter.(it)
//        }


        return binding.root
    }

}