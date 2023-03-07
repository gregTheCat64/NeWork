package ru.javacat.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.javacat.nework.auth.AppAuth
import ru.javacat.nework.databinding.FragmentSignInBinding
import ru.javacat.nework.util.AndroidUtils
import ru.javacat.nework.viewmodels.SignInViewModel

class SignInFragment: Fragment() {
    companion object {
        fun newInstance() = SignInFragment()
    }

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.loginBtn.setOnClickListener {
            if (binding.loginEditText.text.toString().isNotEmpty()
                && binding.passwordEditText.text.toString().isNotEmpty() ){
                val login = binding.loginEditText.text.toString()
                val pass = binding.passwordEditText.text.toString()
                AndroidUtils.hideKeyboard(requireView())
                viewModel.updateUser(login,pass)
            } else Snackbar.make(binding.root, "Заполните все поля", Snackbar.LENGTH_LONG).show()
        }
        viewModel.tokenReceived.observe(viewLifecycleOwner) {
            if (it == 0){
                Snackbar.make(binding.root, "успешный вход!", Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, "Неверный пароль или логин", Snackbar.LENGTH_LONG).show()
            }
        }
        return binding.root
    }
}