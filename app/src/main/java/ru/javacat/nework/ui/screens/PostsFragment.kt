package ru.javacat.nework.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentPostsBinding
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.adapter.OnInteractionListener
import ru.javacat.nework.ui.adapter.PostsAdapter
import ru.javacat.nework.ui.screens.NewPostFragment.Companion.textArg
import ru.javacat.nework.ui.viewmodels.PostViewModel
import java.io.Serializable
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {


    private val postViewModel: PostViewModel by activityViewModels()

    private val mediaObserver = MediaLifecycleObserver()


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

        //postViewModel.refresh()

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

                    fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener {location->
                        println("МЕСТО: $location")
                    val latitude = location.latitude.toString().take(7).toDouble()
                    val longitude = location.longitude.toString().take(7).toDouble()
                        if (location != null) {
                            Log.i("MY_LOCATION", latitude.toString())
                            postViewModel.setCoordinates(
                                latitude, longitude
                            )
                        }

                    }
                }
                // 2. Должны показать обоснование необходимости прав
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO: show rationale dialog
                    //requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    Toast.makeText(requireContext(), "НАМ НУЖНЫ ЕБАНЫ ПРАВА", Toast.LENGTH_SHORT)
                        .show()
                }
                // 3. Запрашиваем права
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

        lifecycle.addObserver(mediaObserver)


        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    postViewModel.likeById(post.id)
                } else showSignInDialog(this@PostsFragment)
            }

            override fun onEdit(post: PostModel) {
                //val contentToEdit = post.content
                postViewModel.edit(post)
                findNavController().navigate(R.id.newPostFragment)

            }

            override fun onRemove(post: PostModel) {
                postViewModel.removeById(post.id)
            }

            override fun onShare(post: PostModel) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
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
                mediaObserver.apply {
                    if (!mediaPlayer!!.isPlaying) {
                        mediaPlayer?.reset()
                        mediaPlayer?.setDataSource(post.attachment?.url)
                        this.play()
                    } else {
                        mediaPlayer!!.pause()
                    }
                }


//                val audioPlayer = MediaPlayer.create(context, post.attachment?.url.toString().toUri())
//                audioPlayer.setOnPreparedListener{
//                    it.start()
//                }
//                audioPlayer.setOnCompletionListener {
//                    post.playBtnPressed = false
//                    it.stop()
//                }
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

            override fun onCoords(post: PostModel) {
                Toast.makeText(context, "${post.coords}", Toast.LENGTH_SHORT).show()
                val coords = post.coords
                val bundle = Bundle()
                if (coords != null) {
                    bundle.putDoubleArray("POINT", doubleArrayOf(coords.latitude,coords.longitude))
                }
                findNavController().navigate(R.id.mapsFragment, bundle)
            }
        })

        binding.postsList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            postViewModel.data
                .catch { e: Throwable ->
                    e.printStackTrace()
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) {
                            postViewModel.refresh()
                        }
                        .show()
                }
                .collectLatest {
                    adapter.submitData(it)
                    //binding.postsList.smoothScrollToPosition(0)

                }
        }


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeToRefresh.isRefreshing =
                    it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

//        lifecycleScope.launchWhenCreated {
//            postViewModel.data.collectLatest {
//                if (it.toString().isEmpty()) {
//                    binding.emptyText.isVisible
//                }
//            }
//        }


        postViewModel.state.observe(viewLifecycleOwner) { state ->
            println("state: $state")

            with(binding) {
                progress.isVisible = state.loading
                //swipeToRefresh.isRefreshing = state.refreshing
            }
            //if (!state.refreshing){binding.postsList.smoothScrollToPosition(0)}
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        postViewModel.refresh()
                    }
                    .show()
            }
        }




        postViewModel.postCreated.observe(viewLifecycleOwner){
            binding.postsList.smoothScrollToPosition(0)
        }

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

        binding.addPostBtn.setOnClickListener {
            if (appAuth.authStateFlow.value.token != null) {
                findNavController().navigate(R.id.newPostFragment)
            } else showSignInDialog(this)

        }

        binding.postListBtn.setOnClickListener {
            binding.postsList.smoothScrollToPosition(0)
        }

        binding.eventsListBtn.setOnClickListener {
            findNavController().navigate(R.id.events)
        }

        return binding.root
    }




}



