package com.example.coroutine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutine.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    val fb: FirebaseFirestore

    val erroFlow = MutableSharedFlow<Throwable>()
    val messageFlow = MutableSharedFlow<String>()
    val activeUsersFlow = MutableSharedFlow<List<User>>()

    init {
        fb = FirebaseFirestore.getInstance()
    }

    suspend fun getUsers() {
        try {
            fb.collection("activeUsers").get().addOnSuccessListener {
                val list = mutableListOf<User>()
                viewModelScope.launch {
                    it.forEach {
                        val user = User(it.id, it.data["name"].toString())
                        list.add(user)
                    }
                    activeUsersFlow.emit(list)
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    messageFlow.emit(it.message ?: "")
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                erroFlow.emit(e)
            }
        }
    }
}