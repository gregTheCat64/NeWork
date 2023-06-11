package ru.javacat.nework.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentPostsBinding
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.OnInteractionListener
import ru.javacat.nework.ui.adapter.PostsAdapter
import ru.javacat.nework.ui.screens.NewPostFragment.Companion.textArg
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.ui.viewmodels.PlayerViewModel
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.ui.viewmodels.WallViewModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.snack
import java.io.Serializable
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {


    private val postViewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val eventsViewModel: EventViewModel by viewModels()
    private val wallViewModel: WallViewModel by viewModels()



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Права получены", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(requireContext(), "Права не получены", Toast.LENGTH_SHORT)
            }
        }

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        //init:
        //userViewModel.loadUsers()



        //Доступ к локации:
        lifecycle.coroutineScope.launchWhenCreated {
            when {
                // 1. Проверяем есть ли уже права
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //Toast.makeText(requireContext(), "Йес!", Toast.LENGTH_SHORT).show()
                    val fusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(requireActivity())

                    fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->
                        println("МЕСТО: $location")
                        val latitude = location.latitude.toString().take(7).toDouble()
                        val longitude = location.longitude.toString().take(7).toDouble()
                        if (location != null) {
                            Log.i("MY_LOCATION", latitude.toString())
                            postViewModel.getCoordinates(
                                latitude, longitude
                            )
                        }

                    }
                }
                // 2. Должны показать обоснование необходимости прав
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO: show rationale dialog
                    //requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    Toast.makeText(requireContext(), "Просто дай нам права!", Toast.LENGTH_SHORT)
                        .show()
                }
                // 3. Запрашиваем права
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }



        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                val myId = appAuth.authStateFlow.value.id
                if (myId != 0L) {
                    postViewModel.likeById(post.id)
                } else showSignInDialog(this@PostsFragment)
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
                try {
                    post.playBtnPressed = !post.playBtnPressed
                    (requireActivity() as AppActivity).playAudio(post.attachment?.url.toString())
                } catch (e: NetworkError) {
                    snack("Ошибка сети")
                }

            }

            override fun onPlayVideo(url: String) {
                //showVideoDialog(url, childFragmentManager)
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
                //Toast.makeText(context, "${post.coords}", Toast.LENGTH_SHORT).show()
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

        val mAnimator = binding.postsList.itemAnimator as SimpleItemAnimator
        mAnimator.supportsChangeAnimations = false

        lifecycleScope.launch {
            postViewModel.data
                .catch { e: Throwable ->
                    e.printStackTrace()
                }
                .collectLatest {
                    adapter.submitData(it)
                }
        }

        lifecycleScope.launch {
            eventsViewModel.data.collectLatest {  }
        }


        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.progress.isVisible = it.refresh is LoadState.Loading
                binding.swipeToRefresh.isRefreshing = false

                if (it.refresh is LoadState.Error||
                            it.append is LoadState.Error ||
                            it.prepend is LoadState.Error
                ) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        postViewModel.refresh()
                    }
                    .show()
                }
            }
        }


        postViewModel.state.observe(viewLifecycleOwner) { state ->
            println("state: $state")

            with(binding) {
                progress.isVisible = state.loading
                //swipeToRefresh.isRefreshing = state.refreshing
            }
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        postViewModel.refresh()
                    }
                    .show()
            }
        }

//        postViewModel.postCreated.observe(viewLifecycleOwner) {
//            binding.postsList.smoothScrollToPosition(0)
//        }

//        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
//            println("НОВЫХ ПОСТОВ: $state штук!!!")
//            val btnText = "Новые записи"
//            if(state>0){
//                binding.newPostsBtn.isVisible = true
//                binding.newPostsBtn.text = btnText
//            }
//        }

//        binding.newPostsBtn.setOnClickListener {
//            viewModel.refresh()
//            binding.newPostsBtn.isVisible = false
//            binding.postsList.smoothScrollToPosition(0)
//        }

        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        return binding.root
    }


}



