package com.fasaldrishti.app.data.repository

import android.net.Uri
import com.fasaldrishti.app.data.remote.SupabaseManager
import com.fasaldrishti.app.domain.model.UserProfile
import com.fasaldrishti.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val supabaseManager: SupabaseManager
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = supabaseManager.currentUser

    override fun getOAuthUrl(provider: String): String {
        return supabaseManager.getOAuthUrl(provider)
    }

    override suspend fun handleAuthCallback(uri: Uri): Result<UserProfile> {
        return supabaseManager.handleAuthCallback(uri)
    }

    override suspend fun signInWithGoogle(): Result<UserProfile> {
        return supabaseManager.signInWithGoogle()
    }

    override fun setAuthenticatedUser(user: UserProfile) {
        supabaseManager.setAuthenticatedUser(user)
    }

    override suspend fun signOut() {
        supabaseManager.signOut()
    }
}
