package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
import ru.javacat.nework.util.snack
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility =
            View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).findViewById<View>(R.id.topAppBar)!!.visibility = View.GONE
    }


    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWallBinding.inflate(inflater)


        val mAnimator = binding.postsList.itemAnimator as SimpleItemAnimator
        mAnimator.supportsChangeAnimations = false

        val args = arguments
        val authorId = args?.getLong("userID", 0L) ?: 0L
        val myId = appAuth.authStateFlow.value.id


        //init
        wallViewModel.getUserJob(authorId)
        userViewModel.getUserById(authorId)
        Log.i("GETTING_JOB","init in Fragment")
        //jobsViewModel.getJobsByUserId(authorId)
        val favBtn = binding.toFavBtn
        val addJobBtn = binding.addJobBtn
        val toolbar = binding.mainToolbar

        toolbar.menu.setGroupVisible(R.id.wallMenu, myId == authorId)
        toolbar.menu.setGroupVisible(R.id.favMenu, myId != authorId)
        val fav = toolbar.menu.findItem(R.id.favMenu)
        //fav.setChecked(true)

        addJobBtn.isVisible = myId == authorId && binding.userJob.text.isEmpty()
        favBtn.isVisible = myId != authorId

        favBtn.setOnClickListener {
            if (!favBtn.isChecked) {
                snack("Больше не в избранном")
            } else {
                snack("Добавлен в избранное")
            }
        }

        addJobBtn.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }

        binding.userJob.setOnClickListener {
            val bundle = Bundle()
            bundle.putLong("userID", authorId)
            findNavController().navigate(R.id.jobsFragment, bundle)
        }


        wallViewModel.userJob.observe(viewLifecycleOwner){
            Log.i("GETTING_JOB","observer started in Fragment")
            it.let {
                Log.i("GETTING_JOB","binding in Fragment")
                binding.userJob.isVisible != it.isNullOrEmpty()
                binding.userJob.text = it
            }
        }

        wallViewModel.dataState.observe(viewLifecycleOwner){
            snack("Ошибка загрузки")
        }

//        lifecycleScope.launch {
//            val job = wallViewModel.getUserJob(authorId)
//            if (job.isNullOrEmpty()) {
//                binding.userJob.visibility = View.GONE
//            } else {
//                binding.userJob.text = job
//            }
//
//        }

        lifecycleScope.launch {
            val postsSize = wallViewModel.getPostsCount(authorId)
            val postsSizeText = "$postsSize записей "
            binding.postsSize.text = postsSizeText
        }

        userViewModel.user.observe(viewLifecycleOwner){user->
            user?.avatar?.let { binding.avatar.loadCircleCrop(it) }
            user?.name?.let {
                binding.mainToolbar.title = it
            }
        }
        //val user = userViewModel.getUserById(authorId)


//        lifecycleScope.launch {
//            val user = userViewModel.getUser(authorId)
//            user?.avatar?.let { binding.avatar.loadCircleCrop(it) }
//            user?.name?.let {
//                binding.mainToolbar.title = user.name
//            }
//        }


        //refresh

//        postViewModel.state.observe(viewLifecycleOwner) { state ->
//            //binding.refreshBtn.isVisible = !state.loading
//            //binding.progress.isVisible = state.loading
//        }



        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                val myId = appAuth.authStateFlow.value.id
                if (myId != 0L) {
                    postViewModel.likeById(post.id)
                } else showSignInDialog(this@WallFragment)
            }

            override fun onEdit(post: PostModel) {
                //удаляем картинку из лайвдаты если она там есть
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
                postViewModel.edit(post)
                //postViewModel.save()
            }

            override fun onPlayAudio(post: PostModel) {
                post.playBtnPressed = !post.playBtnPressed
                (requireActivity() as AppActivity).playAudio(post.attachment?.url.toString())
            }

            override fun onPlayVideo(url: String) {
                val bundle = Bundle()
                bundle.putString("URL", url)
                findNavController().navigate(R.id.videoPlayerFragment, bundle)

            }

            override fun onUser(post: PostModel) {
                val bundle = Bundle()
                bundle.putLong("userID", post.authorId)
                findNavController().navigate(R.id.wallFragment, bundle)
            }

            override fun onImage(url: String) {
                showImageDialog(url, childFragmentManager)
            }

            override fun onMention(post: PostModel) {
                showUserListDialog(post.mentionIds, childFragmentManager)
            }

            override fun onLiked(post: PostModel) {
                post.likeOwnerIds?.let { showUserListDialog(it, childFragmentManager) }
            }

            override fun onCoords(post: PostModel) {
                val coords = post.coords
                val bundle = Bundle()
                if (coords != null) {
                    bundle.putDoubleArray("POINT", doubleArrayOf(coords.latitude, coords.longitude))
                }
                findNavController().navigate(R.id.mapsFragment, bundle)
            }

            override fun onLink(url: String) {
                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    this.data = url.toUri()
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_link))
                startActivity(shareIntent)
            }
        })

        binding.postsList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            wallViewModel.getUserPosts(authorId).collectLatest {
                adapter.submitData(it)
            }
        }

        binding.mainToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.mainToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addPostMenuBtn -> {
                    findNavController().navigate(R.id.newPostFragment)
                    true
                }

                R.id.addJobMenuBtn -> {
                    findNavController().navigate(R.id.newJobFragment)
                    true
                }

                R.id.toFav -> {
                    snack("В избранное")
                    true
                }

                else -> false
            }
        }


        return binding.root
    }


}