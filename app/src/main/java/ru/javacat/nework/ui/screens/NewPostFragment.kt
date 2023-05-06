package ru.javacat.nework.ui.screens

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.databinding.FragmentNewPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.StringArg
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.util.load
import java.io.File

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val postViewModel: PostViewModel by activityViewModels()
    //private val userViewModel: UserViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        var choosenType: AttachmentType? = null
        var usersAddedText = ""

        //*** Так можно добавить меню на appBar
//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.create_post_menu, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
//                when (menuItem.itemId){
//                    R.id.saveBtn ->{
//                        postViewModel.changeContent(binding.edit.text.toString())
//                        postViewModel.save()
//                        AndroidUtils.hideKeyboard(requireView())
//                        findNavController().navigateUp()
//                        true
//                    }
//                        else -> false
//                }
//        }, viewLifecycleOwner)


        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        choosenType = AttachmentType.IMAGE
                        postViewModel.changeAttach(uri, choosenType)
                    }
                }
            }

        val pickAudioFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                when(it.resultCode) {
                    RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        //Log.i("URI", uri.toString())
                        choosenType = AttachmentType.AUDIO
                        postViewModel.changeAttach(uri,choosenType)

                    }
                }
            }

        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048,2048)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .galleryOnly()
                .crop()
                .compress(2000)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.audio.setOnClickListener {
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            pickAudioFileLauncher.launch(intent)
            //startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }

        binding.videoBtn.setOnClickListener {
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            pickAudioFileLauncher.launch(intent)

        }

        postViewModel.photo.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), "$choosenType and ${it.uri} ", Toast.LENGTH_SHORT).show()
            if (it.uri == null){
                binding.attachmentContainer.visibility = View.GONE
                return@observe
            } else { binding.attachmentContainer.visibility = View.VISIBLE
                when (choosenType){
                    AttachmentType.IMAGE ->{binding.photo.visibility = View.VISIBLE
                        binding.audioContainer.root.visibility = View.GONE
                        binding.videoContainer.root.visibility = View.GONE
                        binding.photo.load(it.uri.toString())}
                    AttachmentType.AUDIO->{
                        binding.audioContainer.root.visibility = View.VISIBLE
                        binding.photo.visibility = View.GONE
                        binding.videoContainer.root.visibility = View.GONE
                    binding.audioContainer.audioName.text = it.uri.toString()
                    }
                    AttachmentType.VIDEO->{
                        binding.videoContainer.root.visibility = View.VISIBLE
                        binding.photo.visibility = View.GONE
                        binding.audioContainer.root.visibility = View.GONE
                        binding.videoContainer.videoName.text = it.uri.toString()
                    }

                    else -> {binding.attachmentContainer.visibility = View.GONE}
                }

            }
        }

        postViewModel.edited.observe(viewLifecycleOwner) {post->
            //println("PHOTO: ${post.attachment?.url?.toUri()}")
            binding.edit.setText(post.content.trim())
            binding.usersTextView.text = post.mentionIds.toString()
            if (post.attachment?.url == null){
                binding.attachmentContainer.visibility = View.GONE
                return@observe
            } else { binding.attachmentContainer.visibility = View.VISIBLE
                when (choosenType){
                    AttachmentType.IMAGE ->{binding.photo.visibility = View.VISIBLE
                        binding.audioContainer.root.visibility = View.GONE
                        binding.videoContainer.root.visibility = View.GONE
                        binding.photo.load(post.attachment?.url.toString())}
                    AttachmentType.AUDIO->{
                        binding.audioContainer.root.visibility = View.VISIBLE
                        binding.photo.visibility = View.GONE
                        binding.videoContainer.root.visibility = View.GONE
                        binding.audioContainer.audioName.text = post.attachment?.url.toString()
                    }
                    AttachmentType.VIDEO->{
                        binding.videoContainer.root.visibility = View.VISIBLE
                        binding.photo.visibility = View.GONE
                        binding.audioContainer.root.visibility = View.GONE
                        binding.videoContainer.videoName.text = post.attachment?.url.toString()
                    }

                    else -> {binding.attachmentContainer.visibility = View.GONE}
                }

            }
        }

        binding.clearPicBtn.setOnClickListener {
            choosenType = null
            postViewModel.deleteAttechment()
        }

        binding.saveBtn.setOnClickListener {
            postViewModel.changeContent(binding.edit.text.toString())
            postViewModel.save(choosenType)
            postViewModel.setUsersAdded("")
            binding.usersTextView.text=""
            AndroidUtils.hideKeyboard(requireView())
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            postViewModel.loadPosts()
            findNavController().navigateUp()
        }


        postViewModel.usersAdded.observe(viewLifecycleOwner){
            usersAddedText = "Отмечены: "
            usersAddedText += it
            binding.usersTextView.text = usersAddedText
        }

        binding.cancelButton.setOnClickListener {
            postViewModel.setUsersAdded("")
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }


        binding.addUsersBtn.setOnClickListener {
            val action = NewPostFragmentDirections.actionNewPostFragmentToUsersAddingFragment(UsersType.MENTION)
            findNavController().navigate(action)
        }

        return binding.root
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 111 && resultCode == RESULT_OK) {
//            val uri = data?.data // The URI with the location of the file
//            postViewModel.changeAttach(uri)
//
//        }
//    }
}