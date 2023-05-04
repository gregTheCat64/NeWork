package ru.javacat.nework.ui.screens

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentNewPostBinding
import ru.javacat.nework.domain.model.UsersType
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.util.StringArg
import ru.javacat.nework.ui.viewmodels.PostViewModel
import ru.javacat.nework.ui.viewmodels.UserViewModel
import ru.javacat.nework.util.load

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
                        postViewModel.changePhoto(uri)
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

        postViewModel.photo.observe(viewLifecycleOwner){
            if (it.uri == null){
                binding.photoContainer.visibility = View.GONE
                return@observe
            } else {
                binding.photoContainer.visibility = View.VISIBLE
                binding.photo.load(it.uri.toString())
            }
        }

        postViewModel.edited.observe(viewLifecycleOwner) {post->
            //println("PHOTO: ${post.attachment?.url?.toUri()}")
            binding.edit.setText(post.content.trim())
            binding.usersTextView.text = post.mentionIds.toString()
            if (post.attachment?.url?.toUri() != null) {
                binding.photoContainer.visibility = View.VISIBLE
                binding.photo.load(post.attachment!!.url.toUri().toString())
            } else binding.photoContainer.visibility = View.GONE

        }

        binding.clearPicBtn.setOnClickListener {
            postViewModel.deleteAttechment()
        }

        binding.saveBtn.setOnClickListener {
            postViewModel.changeContent(binding.edit.text.toString())
            postViewModel.save()
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
}