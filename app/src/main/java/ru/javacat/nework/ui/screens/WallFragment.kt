package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentWallBinding
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.ui.adapter.OnInteractionListener
import ru.javacat.nework.ui.adapter.PostsAdapter
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.WallViewModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.loadCircleCrop
import ru.javacat.nework.util.snack
import javax.inject.Inject

@AndroidEntryPoint
class WallFragment : Fragment() {
    //private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val wallViewModel: WallViewModel by viewModels()


    var currentUser: User = User(0L, "", "", "", false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate")

    }



    @Inject
    lateinit var appAuth: AppAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("onCreateView")
        val binding = FragmentWallBinding.inflate(inflater, container, false)

        val mAnimator = binding.postsList.itemAnimator as SimpleItemAnimator
        mAnimator.supportsChangeAnimations = false


        val args = arguments
        val authorId = args?.getLong("userID", 0L) ?: 0L
        val myId = appAuth.authStateFlow.value.id


        val favedIcon =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
        val unFavedIcon = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.ic_baseline_favorite_border_24
        )


        //init
        wallViewModel.getUserJob(authorId)
        wallViewModel.getUserById(authorId)
        wallViewModel.getPostsCount(authorId)


        val addJobBtn = binding.addJobBtn
        val toolbar = binding.mainToolbar
        val progress = binding.progress
        val favBtn = binding.toFavBtn


        favBtn.isVisible = myId != authorId

        favBtn.setOnClickListener {
            if (!favBtn.isChecked) {
                wallViewModel.deleteUserFromFav(myId, authorId)
                //val daores = wallViewModel.getFavList(myId)
                //snack("$daores")
                snack("Больше не в избранном")
            } else {
                wallViewModel.addUserToFav(myId, authorId)
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


        wallViewModel.userJob.observe(viewLifecycleOwner) {
            Log.i("GETTING_JOB", "observer started in Fragment")
            if (it.isNullOrEmpty()) {
                binding.userJob.visibility = View.GONE
            } else {
                binding.userJob.visibility = View.VISIBLE
                binding.userJob.text = it
            }
            addJobBtn.isVisible = myId == authorId && binding.userJob.text.isNullOrBlank()
//            it.let {
//                Log.i("GETTING_JOB", "binding in Fragment")
//                binding.userJob.isVisible != it.isNullOrEmpty()
//                binding.userJob.text = it
//            }
        }

        wallViewModel.dataState.observe(viewLifecycleOwner) { state ->
            progress.root.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        wallViewModel.refresh(authorId)
                    }
                    .show()
            }
        }

        wallViewModel.postsSize.observe(viewLifecycleOwner) { size ->
            binding.postsSize.text = getPostSizeText(size)
        }

        wallViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.avatar?.let { binding.avatar.loadCircleCrop(it) }
            user?.name?.let {
                binding.mainToolbar.title = it
            }
            currentUser = user
            favBtn.isChecked = currentUser.favoured == true
        }


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

            override fun clearUpBtn() {
                super.clearUpBtn()
            }
        })

        binding.postsList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            wallViewModel.getUserPosts(authorId).collectLatest {
                adapter.submitData(it)
            }
        }

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addPostMenuBtn -> {
                    findNavController().navigate(R.id.newPostFragment)
                    true
                }

                R.id.addJobMenuBtn -> {
                    findNavController().navigate(R.id.newJobFragment)
                    true
                }

                else -> false
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("onViewCreated")
        //(activity as AppCompatActivity).findViewById<AppBarLayout>(R.id.topAppBar)!!.visibility = View.GONE
    }
}



fun getPostSizeText(size: Int): String {
    val result = if (size in 5..20) {
        "$size записей"
    } else {
        when (size % 10) {
            1 -> "$size запись"
            in (2..4) -> "$size записи"
            in (5..9) -> "$size записей"
            else -> {
                "$size записей"
            }
        }
    }
    return result
}