package ru.javacat.nework.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.auth.Token
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {
    val data: LiveData<Token> = appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authorized: Boolean
        get() = appAuth.authStateFlow.value.id != 0L
}