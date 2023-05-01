package ru.javacat.nework.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class WallViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
): ViewModel() {

    init {
        Log.i("WALLFRAG", "initialized")
    }

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _userPosts = MutableLiveData<List<PostModel>>()
    val userPosts: LiveData<List<PostModel>>
        get() = _userPosts


    fun getUserById(id: Long) {
        viewModelScope.launch {
            try {
                _user.postValue(userRepository.getById(id))
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun loadPostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _userPosts.postValue(postRepository.getPostsByAuthorId(authorId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePostsByAuthorId(authorId: Long) = viewModelScope.launch {
        try {
            _userPosts.postValue(postRepository.updatePostsByAuthorId(authorId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}