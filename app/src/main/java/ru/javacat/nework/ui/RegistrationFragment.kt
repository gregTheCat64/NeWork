package ru.javacat.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.databinding.FragmentRegistrationBinding
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.viewmodels.RegistrationViewModel

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

        binding.registerUserBtn.setOnClickListener {
            if (binding.loginEditText.text.isNotEmpty() &&
                binding.passwordEditText.text.isNotEmpty()&&
                binding.nameEditText.text.isNotEmpty()){
                val login = binding.loginEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                val name = binding.nameEditText.text.toString()
                AndroidUtils.hideKeyboard(requireView())
                viewModel.registerUser(login,password,name)
            } else Snackbar.make(binding.root, "Заполните все поля", Snackbar.LENGTH_LONG).show()
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