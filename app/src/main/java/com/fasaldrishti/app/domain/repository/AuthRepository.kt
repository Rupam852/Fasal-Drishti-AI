package com.fasaldrishti.app.domain.repository

import com.fasaldrishti.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signInWithGitHub(): Result<UserProfile>
    suspend fun signOut()
}
