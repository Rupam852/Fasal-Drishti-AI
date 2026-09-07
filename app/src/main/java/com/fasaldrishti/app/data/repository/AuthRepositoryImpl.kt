package com.fasaldrishti.app.data.repository

import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.domain.model.UserProfile
import com.fasaldrishti.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val supabaseManager: SupabaseManager
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = supabaseManager.currentUser

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        return supabaseManager.signInWithGoogle()
    }

    override suspend fun signInWithGitHub(): Result<UserProfile> {
        return supabaseManager.signInWithGitHub()
    }

    override suspend fun signOut() {
        supabaseManager.signOut()
    }
}
