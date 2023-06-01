package ru.javacat.nework.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.SimpleItemAnimator
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
import ru.javacat.nework.util.asString
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

                    val msg =
                            "$author делится мероприятием:\n" +
                            "$content \n" +
                            "начало в $date \n"+
                            "формат мероприятия: $format"+
                            "$attach \n"+
                            "$link\n"+
                            "отправлено из NeWork App.\n"

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


            //binding.eventsList.smoothScrollToPosition(0)


        return binding.root

        
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            EventsFragment()
    }
}