package ru.javacat.nework.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.javacat.nework.R
import ru.javacat.nework.ui.adapter.OnInteractionListener
import ru.javacat.nework.ui.adapter.PostsAdapter
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentFeedBinding
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.mediaplayer.MediaLifecycleObserver
import ru.javacat.nework.ui.screens.NewPostFragment.Companion.textArg
import ru.javacat.nework.ui.viewmodels.PostViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

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
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        
        //Доступ к локации:
        when {
            // 1. Проверяем есть ли уже права
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(requireContext(), "Йес!", Toast.LENGTH_SHORT).show()
                val fusedLocationProviderClient = LocationServices
                    .getFusedLocationProviderClient(requireActivity())

                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    println(it)
                    val df = DecimalFormat("#,######")
                    df.roundingMode = RoundingMode.CEILING
//                    val latitude = String.format("%.7g%n", it.latitude).toDouble()
//                    val longitude = String.format("%.7g%n", it.longitude).toDouble()

                    viewModel.setCoordinates(df.format(it.latitude).toDouble(), df.format(it.longitude).toDouble())
                }
            }
            // 2. Должны показать обоснование необходимости прав
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // TODO: show rationale dialog
            }
            // 3. Запрашиваем права
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        lifecycle.addObserver(mediaObserver)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: PostModel) {
                if (appAuth.authStateFlow.value.id != 0L) {
                    viewModel.likeById(post.id)
                } else showSignInDialog()
            }

            override fun onEdit(post: PostModel) {
                val contentToEdit = post.content
                findNavController().navigate(R.id.action_navigation_posts_to_newPostFragment,
                    Bundle().apply { textArg = contentToEdit })
                viewModel.edit(post)
            }

            override fun onRemove(post: PostModel) {
                viewModel.removeById(post.id)
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
                viewModel.edit(post)
                viewModel.save()
            }

            override fun onPlayAudio(post: PostModel) {
                mediaObserver.apply {
                    mediaPlayer?.reset()
                    mediaPlayer?.setDataSource(
                        post.attachment?.url
                    )
                }.play()
            }

        })

        binding.postsList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeToRefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }
//        viewModel.data.observe(viewLifecycleOwner) { feedModel ->
//            adapter.submitList(feedModel.posts)
//            with(binding){
//                emptyText.isVisible = feedModel.empty
//            }
//        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            with(binding) {
                progress.isVisible = state.loading
                swipeToRefresh.isRefreshing = state.refreshing
            }
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.refresh()
                    }
                    .show()
            }
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
                findNavController().navigate(R.id.action_navigation_posts_to_newPostFragment)
            } else showSignInDialog()

        }

        binding.eventsListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_posts_to_navigation_events)
        }

        return binding.root
    }

    private fun showSignInDialog() {
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> findNavController().navigate(R.id.action_navigation_posts_to_signInFragment)
                DialogInterface.BUTTON_NEGATIVE -> Toast.makeText(
                    context,
                    "Не забудьте авторизоваться",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val dialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("Вы не авторизованы!")
            .setMessage("Пожалуйста, авторизуйтесь")
            .setPositiveButton("Хорошо", listener)
            .setNegativeButton("Позже", listener)
            .create()

        dialog.show()
    }
}