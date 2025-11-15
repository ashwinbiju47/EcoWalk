package com.example.ecowalk.ui.auth

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowalk.data.local.User
import com.example.ecowalk.data.local.UserDatabase
import com.example.ecowalk.data.repository.UserRepository
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val userDao = UserDatabase.getDatabase(application).userDao()
    private val repository = UserRepository(userDao)

    var authMessage = mutableStateOf<String?>(null)

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    viewModelScope.launch {
                        repository.saveFirebaseUser(uid, email)
                        authMessage.value = "Account created successfully!"
                    }
                } else {
                    authMessage.value = task.exception?.message ?: "Registration failed."
                }
            }
    }

    fun login(email: String, password: String, onSuccess: (User) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser!!
                    viewModelScope.launch {
                        val local = repository.getLocalUser(firebaseUser.uid)
                        if (local != null) {
                            onSuccess(local)
                        } else {
                            val newLocal = User(firebaseUid = firebaseUser.uid, email = firebaseUser.email ?: email)
                            repository.saveFirebaseUser(firebaseUser.uid, newLocal.email)
                            onSuccess(newLocal)
                        }
                    }
                } else {
                    authMessage.value = "Invalid email or password."
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
    suspend fun getLocalUser(uid: String): User? {
        return repository.getLocalUser(uid)
    }

}

