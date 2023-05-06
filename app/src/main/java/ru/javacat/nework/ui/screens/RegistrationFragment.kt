package ru.javacat.nework.ui.screens

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.databinding.FragmentRegistrationBinding
import ru.javacat.nework.domain.model.AttachModel
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.ui.viewmodels.RegistrationViewModel

private var avatar = AttachModel()

class RegistrationFragment : Fragment() {

    companion object {
        fun newInstance() = RegistrationFragment()
    }

    private  val viewModel: RegistrationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRegistrationBinding.inflate(inflater, container,false)

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
                        viewModel.changePhoto(uri, uri?.toFile())
                        //avatar = PhotoModel(uri, uri?.toFile())
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .crop()
                .compress(200)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg"
                    )
                )
                .createIntent {
                    pickPhotoLauncher.launch(it)
                }
        }

        binding.registerUserBtn.setOnClickListener {
            if (!binding.loginEditText.text.isNullOrEmpty() &&
                !binding.passwordEditText.text.isNullOrEmpty()&&
                !binding.nameEditText.text.isNullOrEmpty()){
                val login = binding.loginEditText.text.toString().trim()
                println("$login")
                val password = binding.passwordEditText.text.toString().trim()
                println("$password")
                val name = binding.nameEditText.text.toString().trim()
                AndroidUtils.hideKeyboard(requireView())

                if (binding.passwordEditText.text.toString().trim() == binding.passwordCheckEditText.text.toString().trim()){
                    viewModel.registerUser(login,password,name)
                } else Snackbar.make(binding.root, "Пароли не совпадают", Snackbar.LENGTH_LONG).show()

                //viewModel.registerUser(login,password,name)
            } else Snackbar.make(binding.root, "Заполните все поля", Snackbar.LENGTH_LONG).show()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it?.uri != null){
                binding.avatarImage.isVisible = true
                binding.pickPhoto.isVisible = false
                binding.avatarImage.setImageURI(it.uri)
                //getAvatars(it.uri.toString(), binding)
            }
        }

        viewModel.tokenReceived.observe(viewLifecycleOwner) {
            if (it == 0){
                Snackbar.make(binding.root, "Добавлен пользователь", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, "Неверный пароль или логин", Snackbar.LENGTH_LONG).show()
            }
        }


        return binding.root
    }

}
