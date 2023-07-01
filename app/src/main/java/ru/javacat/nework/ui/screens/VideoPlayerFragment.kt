package ru.javacat.nework.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentVideoPlayerBinding
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.ui.viewmodels.PlayerViewModel

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var player: ExoPlayer
    private val viewModel: PlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoPlayerBinding.inflate(layoutInflater)

        player = ExoPlayer.Builder(requireContext()).build()
        binding.attachVideo.player = player
        val args = arguments
        val url = args?.getString("URL") ?: ""

        viewModel.play(player, url)


        viewModel.state.observe(viewLifecycleOwner){
            binding.progressBar.isVisible = it.loading
            if (it == FeedModelState(error = true)){
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.play(player, url)
                    }
                    .show()
            }
        }


        binding.closeBtn.setOnClickListener {
            player.release()
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

//        dialog!!.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
    }

    override fun onStop() {
        super.onStop()
        player.pause()

    }

    override fun onResume() {
        super.onResume()

    }



    //    override fun onCreateDialog(
//        savedInstanceState: Bundle?
//    ): Dialog {
//
//        val listener = DialogInterface.OnClickListener{ _, which ->
//            when (which){
//                DialogInterface.BUTTON_NEGATIVE -> this.dismiss()
//                DialogInterface.BUTTON_NEUTRAL ->
//                    this.dismiss()
//            }
//        }
//
//        val builder = AlertDialog.Builder(context)
//            .setView(R.id.videoPlayerFragment)
//            .setNeutralButton("Закрыть", listener)
//            .create()
//        return builder
//    }
}