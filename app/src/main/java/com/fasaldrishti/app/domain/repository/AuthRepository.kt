package com.fasaldrishti.app.domain.repository

import android.net.Uri
import com.fasaldrishti.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    fun getOAuthUrl(provider: String): String
    suspend fun handleAuthCallback(uri: Uri): Result<UserProfile>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signInWithGitHub(): Result<UserProfile>
    suspend fun signOut()
}
