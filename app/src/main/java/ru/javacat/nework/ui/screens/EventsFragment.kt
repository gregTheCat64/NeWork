package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.javacat.nework.R
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.databinding.FragmentEventsBinding
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.ui.adapter.EventsAdapter
import ru.javacat.nework.ui.adapter.OnEventsListener
import ru.javacat.nework.ui.viewmodels.EventViewModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.snack
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {
    private val viewModel: EventViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth
    lateinit var binding: FragmentEventsBinding

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val fragmentBinding = FragmentEventsBinding.inflate(inflater)
//        binding = fragmentBinding
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEventsBinding.bind(view)

        //анимация
        val upBtnAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.up_btn)
        val newEventsAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.new_posts_btn)

        val mAnimator = binding.eventsList.itemAnimator as SimpleItemAnimator
        mAnimator.supportsChangeAnimations = false

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
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND

                    val author = event.author
                    val content = event.content
                    val attach = event.attachment?.url?:""
                    val date = event.datetime?.asString()
                    val link = event.link?:""
                    val format = event.type.name
                    val downloadLink = "https://disk.yandex.ru/d/CMfR6397IROBqw"
                    val msg =
                        "$author делится мероприятием:\n" +
                                "$content \n." +
                                "Начало $date \n"+
                                "Формат мероприятия: $format\n"+
                                "вложение: $attach \n"+
                                "сайт: $link\n"+
                                "отправлено из NeWork App.\n"+
                                "чтобы скачать приложение пройдите по ссылке: \n"+
                                downloadLink

                    putExtra(Intent.EXTRA_TEXT, msg)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onParticipant(ids: List<Long>) {
                showUserListDialog(ids, childFragmentManager)
            }

            override fun onPlayVideo(url: String) {
                val bundle = Bundle()
                bundle.putString("URL", url)
                findNavController().navigate(R.id.videoPlayerFragment, bundle)
            }

            override fun onUser(event: EventModel) {
                val bundle = Bundle()
                bundle.putLong("userID", event.authorId)
                findNavController().navigate(R.id.wallFragment, bundle)
            }

            override fun onLiked(event: EventModel) {
                event.likeOwnerIds?.let { showUserListDialog(it, childFragmentManager) }
            }

            override fun onImage(url: String) {
                showImageDialog(url, childFragmentManager)
            }

            override fun onPlayAudio(event: EventModel) {
                if (event.playBtnPressed){
                    (requireActivity() as AppActivity).stopAudio()
                } else {
                    (requireActivity() as AppActivity).playAudio(event.attachment?.url.toString())
                }
                //event.playBtnPressed = !event.playBtnPressed
            }

            override fun onLocation(event: EventModel) {
                val coords = event.coords
                val bundle = Bundle()
                if (coords != null) {
                    bundle.putDoubleArray("POINT", doubleArrayOf(coords.latitude,coords.longitude))
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
                binding.upBtn.apply {
                    isVisible = true
                    startAnimation(upBtnAnim)
                }
            }

            override fun clearUpBtn() {
                binding.upBtn.isVisible = false
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

        lifecycleScope.launch {
            viewModel.data
                .catch { e:Throwable->
                    e.printStackTrace()
                }
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    adapter.submitData(it)
                }
        }

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
                            viewModel.refresh()
                        }
                        .show()
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner){state->
            binding.progress.root.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.refresh()
                    }
                    .show()
            }
        }

        lifecycleScope.launch{
            viewModel.newerCount.collectLatest {
                if (it>0){
                    val string = "Новое мероприятие ($it)"
                    binding.newEventsBtn.apply {
                        text = string
                        isVisible = true
                        startAnimation(newEventsAnim)
                    }
                }
            }
        }

        binding.newEventsBtn.setOnClickListener {
            it.isVisible = false
            binding.upBtn.isVisible = false
            binding.eventsList.smoothScrollToPosition(0)
            adapter.refresh()
        }

        binding.upBtn.setOnClickListener {
            binding.eventsList.smoothScrollToPosition(0)
            adapter.refresh()

            it.visibility = View.GONE
        }
    }

}