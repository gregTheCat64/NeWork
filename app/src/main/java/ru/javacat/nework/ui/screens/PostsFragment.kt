package ru.javacat.nework.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentPostsBinding
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.ui.adapter.OnInteractionListener
import ru.javacat.nework.ui.adapter.PostsAdapter
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.snack
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {


    private val postViewModel: PostViewModel by activityViewModels()
    //private val eventsViewModel: EventViewModel by viewModels()




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


        //animation:
        val upBtnAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.up_btn)
        val newPostsAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.new_posts_btn)

        val mAnimator = binding.postsList.itemAnimator as SimpleItemAnimator
        mAnimator.supportsChangeAnimations = false




        //Доступ к локации:
        lifecycle.coroutineScope.launch {
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
                            postViewModel.setCoordinates(
                                latitude, longitude
                            )
                        }
                    }
                }
                // 2. Должны показать обоснование необходимости прав
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO: show rationale dialog
                    Toast.makeText(requireContext(), "Местоположение необходимо для добавления геометки", Toast.LENGTH_SHORT)
                        .show()
                    //requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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

            override fun makeUpBtn() {
                binding.upBtn.isVisible = true
                binding.upBtn.startAnimation(upBtnAnim)
            }

            override fun clearUpBtn() {
                binding.upBtn.isVisible = false
            }
        })

        binding.postsList.adapter = adapter



        lifecycleScope.launch {
            postViewModel.data
                .catch { e: Throwable ->
                    e.printStackTrace()
                }
                .collectLatest {
                    adapter.submitData(it)
                }
        }

        lifecycleScope.launch{
            postViewModel.newerCount.collectLatest {
                if (it>0){
                    val string = "Новая запись ($it)"
                    binding.newPostsBtn.apply {
                        text = string
                        isVisible = true
                        startAnimation(newPostsAnim)
                    }
                }
            }
        }

        binding.newPostsBtn.setOnClickListener {
            it.isVisible = false
            binding.upBtn.isVisible = false
            binding.postsList.smoothScrollToPosition(0)
            adapter.refresh()
        }

        //TODO: осуществить предварительную загрузку ивентов
//        lifecycleScope.launch {
//            eventsViewModel.data.collectLatest {  }
//        }


        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.progress.root.isVisible = it.refresh is LoadState.Loading
                        ||it.append is LoadState.Loading
                        ||it.prepend is LoadState.Loading

                binding.swipeToRefresh.isRefreshing = false

                if (it.refresh is LoadState.Error||
                            it.append is LoadState.Error ||
                            it.prepend is LoadState.Error
                ) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        adapter.refresh()
                    }
                    .show()
                }
            }
        }


//        postViewModel.state.observe(viewLifecycleOwner) { state ->
//            println("state: $state")
//
//            with(binding) {
//                progress.root.isVisible = state.loading
//                progress.root.isVisible = state.refreshing
//                //swipeToRefresh.isRefreshing = state.refreshing
//            }
//            if (state.error) {
//                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                    .setAction(R.string.retry_loading) {
//                        postViewModel.refresh()
//                    }
//                    .show()
//            }
//        }


        //TODO адаптер не успевает обновиться, и скролл происходит к предыдущему посту
        postViewModel.newPost.observe(viewLifecycleOwner){
            adapter.refresh()
            binding.postsList.smoothScrollToPosition(0)
            binding.upBtn.isVisible = false
            snack("Запись создана")
        }


        binding.upBtn.setOnClickListener {
            binding.postsList.smoothScrollToPosition(0)
            adapter.refresh()
            it.isVisible = false
        }

        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        return binding.root
    }


}



