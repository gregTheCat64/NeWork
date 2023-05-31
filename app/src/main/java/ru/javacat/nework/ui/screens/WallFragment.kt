package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentWallBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.*
import ru.javacat.nework.ui.viewmodels.*
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.loadCircleCrop
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val jobsViewModel: JobsViewModel by viewModels()

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

        //init
        userViewModel.getUserById(authorId)
        jobsViewModel.getJobsByUserId(authorId)
        postViewModel.loadPostsByAuthorId(authorId)
        eventViewModel.getByAuthorId(authorId)

        //refresh
        binding.refreshBtn.setOnClickListener {
            postViewModel.updatePostsByAuthorId(authorId)
            eventViewModel.updateEventsByAuthorId(authorId)
            jobsViewModel.updateJobsByUserId(authorId)
        }

        postViewModel.state.observe(viewLifecycleOwner){state->
            binding.refreshBtn.isVisible = !state.loading
            binding.progress.isVisible = state.loading
        }

        //user:
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.avatar?.let { binding.avatar.loadCircleCrop(it) }
            user?.name?.let { binding.name.text = it }
        }

        //jobs:
        val jobsAdapter = JobsAdapter()

        binding.jobsList.adapter = jobsAdapter

        jobsViewModel.userJobs.observe(viewLifecycleOwner) {
            jobsAdapter.submitList(it)
        }
        binding.jobsListBtn.setOnClickListener {
            jobsViewModel.getJobsByUserId(authorId)
            expandedJobs = !expandedJobs
            binding.jobsList.isVisible = expandedJobs
        }


        //posts:
        val postAdapter = UserPostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    postViewModel.likeById(post.id)
                } else showSignInDialog(this@WallFragment)
            }

            override fun onEdit(post: PostModel) {
                postViewModel.deleteAttachment()
                postViewModel.edit(post)
                findNavController().navigate(R.id.newPostFragment)
            }

            override fun onRemove(post: PostModel) {
                postViewModel.removeById(post.id)
            }

            override fun onShare(post: PostModel) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND

                    val author = post.author
                    val content = post.content
                    val attach = post.attachment?.url ?: ""
                    val link = post.link ?: ""
                    val published = post.published?.asString()

                    val msg = "$author пишет:\n" +
                            "$content \n" +
                            "$attach \n" +
                            "$link\n" +
                            "$published" +
                            "отправлено из NeWork App.\n"

                    putExtra(Intent.EXTRA_TEXT, msg)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onResave(post: PostModel) {
                super.onResave(post)
            }

            override fun onPlayAudio(post: PostModel) {
                post.playBtnPressed = !post.playBtnPressed
                (requireActivity() as AppActivity).playAudio(post.attachment?.url.toString())
            }

            override fun onPlayVideo(url: String) {
                showVideoDialog(url, childFragmentManager)
            }

            override fun onImage(url: String) {
                showImageDialog(url, parentFragmentManager)
            }

            override fun onUser(post: PostModel) {
                val bundle = Bundle()
                bundle.putLong("userID", post.authorId)
                findNavController().navigate(R.id.wallFragment, bundle)
            }

            override fun onMention(post: PostModel) {
                showUserListDialog(post.mentionIds, parentFragmentManager)
            }

            override fun onCoords(post: PostModel) {
                val coords = post.coords
                val bundle = Bundle()
                if (coords != null) {
                    bundle.putDoubleArray("POINT", doubleArrayOf(coords.latitude, coords.longitude))
                }
                findNavController().navigate(R.id.mapsFragment, bundle)
            }
        })

        binding.postsList.adapter = postAdapter

        postViewModel.userPosts.observe(viewLifecycleOwner) {
            postAdapter.submitList(it)
        }

        binding.postListBtn.setOnClickListener {
            //postViewModel.loadPostsByAuthorId(authorId)
            expandedPosts = !expandedPosts
            binding.postsList.isVisible = expandedPosts
            binding.eventsList.isVisible = false
        }

        //events:
        val eventsAdapter = UserEventsAdapter(object : OnEventsListener {
        })

        binding.eventsList.adapter = eventsAdapter

        eventViewModel.userEvents.observe(viewLifecycleOwner){
            eventsAdapter.submitList(it)
        }

        binding.eventsListBtn.setOnClickListener {
            //eventViewModel.getByAuthorId(authorId)
            expandedEvents = !expandedEvents
            binding.eventsList.isVisible = expandedEvents
        }

        //navigation:
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

    companion object {
        @JvmStatic
        fun newInstance() =
            WallFragment()
    }

}