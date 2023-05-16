package ru.javacat.nework.ui.screens

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewPostBinding
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.FeedModelState
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.ui.adapter.OnUserListener
import ru.javacat.nework.ui.adapter.UsersAdapter
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.StringArg
import ru.javacat.nework.util.load
import ru.javacat.nework.util.toFile

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val postViewModel: PostViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

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
        //postViewModel.edited.value?.let { initBindings(it,binding) }


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
//                        postViewModel.save(choosenType)
//                        AndroidUtils.hideKeyboard(requireView())
//                        findNavController().navigateUp()
//                        true
//                    }
//                        else -> false
//                }
//        }, viewLifecycleOwner)

            //pickers:
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
                        println("file: ${uri?.toString()}")
                        println("UPLOADING")
                        postViewModel.changeAttach(uri, choosenType)
                    }
                }
            }

        //media:
        val pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    RESULT_OK -> {
                        val file = it.data?.data?.toFile(requireContext())
                        postViewModel.changeAttach(file?.toUri(), choosenType)
                    }
                }
            }


        //listeners:
        binding.buttonPanel.takePhoto.setOnClickListener {
            //TODO вынести функцию
            postViewModel.changeContent(binding.edit.text?.trim().toString())
            choosenType = AttachmentType.IMAGE
            ImagePicker.Builder(this)
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.buttonPanel.pickPhoto.setOnClickListener {
            postViewModel.setState(FeedModelState(loading = true))
            postViewModel.changeContent(binding.edit.text?.trim().toString())
            choosenType = AttachmentType.IMAGE
            ImagePicker.Builder(this)
                .galleryOnly()
                .crop()
                .compress(2000)
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.buttonPanel.audio.setOnClickListener {
            postViewModel.changeContent(binding.edit.text?.trim().toString())
            val intent = Intent()
                .setType("audio/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.AUDIO
            pickFileLauncher.launch(intent)
        }

        binding.buttonPanel.videoBtn.setOnClickListener {
            postViewModel.changeContent(binding.edit.text?.trim().toString())
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            choosenType = AttachmentType.VIDEO
            pickFileLauncher.launch(intent)
        }

        binding.mentionUsersBtn.setOnClickListener {
            postViewModel.changeContent(binding.edit.text?.trim().toString())
            setFragmentResultListener("IDS") { _, bundle ->
                val result = bundle.getLongArray("IDS")
                if (result != null) {
//                    userViewModel.getUsersById(result.toList())
//                    val added = userViewModel.addedUsers.value
//                    println("$added")
                    //postViewModel.edited.value?.mentionIds = result.toList()
                    postViewModel.setMentions(result.toList())
                }
            }
            findNavController().navigate(R.id.usersAddingFragment)
        }



        binding.clearPicBtn.setOnClickListener {
            choosenType = null
            postViewModel.deleteAttachment()
        }

        //appBAR:
        binding.topAppBar.setNavigationOnClickListener {
            postViewModel.clearEdit()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        binding.topAppBar.setOnMenuItemClickListener {menuItem->
            when (menuItem.itemId) {
                R.id.create -> {
                    postViewModel.changeContent(binding.edit.text.toString())
                    postViewModel.save(choosenType)
                    AndroidUtils.hideKeyboard(requireView())
                    true
                }

                else -> {false}
            }

        }


        //usersList:
        val list = binding.addedUsersList

        val adapter = UsersAdapter(object : OnUserListener {
            override fun onTouch(user: User) {
                val bundle = Bundle()
                bundle.putLong("userID", user.id)
                findNavController().navigate(R.id.wallFragment, bundle)
            }
        })

        list.adapter = adapter

        userViewModel.addedUsers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        //observers:
        postViewModel.postCreated.observe(viewLifecycleOwner) {
            postViewModel.refresh()
            findNavController().navigateUp()

        }

        postViewModel.edited.observe(viewLifecycleOwner) { post ->
            println("POST_IDS: ${post.mentionIds}")
            println("EDITED: ${postViewModel.edited.value}")
            userViewModel.getUsersById(post.mentionIds)
            //postViewModel.setAddedUsersIds(post.mentionIds)
            initBindings(post, binding)
        }

        postViewModel.state.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it.loading
        }



        return binding.root
    }

//    private fun getRealPathFromUri(contentUri: Uri?): String?{
//        val proj = arrayOf( MediaStore.Audio.Media.DATA)
//        val loader = CursorLoader(requireContext(), contentUri!!, proj ,null,null,null)
//        val cursor = loader.loadInBackground()
//        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//        cursor?.moveToFirst()
//        val result = column_index?.let { cursor.getString(it) }
//        cursor?.close()
//        return result
//    }

//    private fun getFileFromUri(contentResolver: ContentResolver, uri: Uri?, directory: File): File {
//        val file =
//            File.createTempFile("tmp", "@gree", directory)
//        file.outputStream().use {
//            if (uri != null) {
//                val input = contentResolver.openInputStream(uri)
//                input?.copyTo(it)
//                input?.close()
//            }
//        }
//        return file
//    }

    private fun initBindings(post: PostModel, binding: FragmentNewPostBinding) {
        binding.edit.setText(post.content.trim())
        //binding.usersTextView.text = "Отмечены:"
        if (post.attachment == null) {
            binding.attachmentContainer.visibility = View.GONE
            return
        } else {
            binding.attachmentContainer.visibility = View.VISIBLE
            when (post.attachment!!.type) {
                AttachmentType.IMAGE -> {
                    binding.photo.visibility = View.VISIBLE
                    binding.audioContainer.root.visibility = View.GONE
                    binding.videoContainer.root.visibility = View.GONE
                    binding.photo.load(post.attachment?.url.toString())
                }

                AttachmentType.AUDIO -> {
                    binding.audioContainer.root.visibility = View.VISIBLE
                    binding.photo.visibility = View.GONE
                    binding.videoContainer.root.visibility = View.GONE
                    binding.audioContainer.audioName.text = post.attachment?.url.toString()
                }

                AttachmentType.VIDEO -> {
                    binding.videoContainer.root.visibility = View.VISIBLE
                    binding.photo.visibility = View.GONE
                    binding.audioContainer.root.visibility = View.GONE
                    binding.videoContainer.videoName.text = post.attachment?.url.toString()
                }

                else -> {
                    binding.attachmentContainer.visibility = View.GONE
                }
            }

        }
    }

}