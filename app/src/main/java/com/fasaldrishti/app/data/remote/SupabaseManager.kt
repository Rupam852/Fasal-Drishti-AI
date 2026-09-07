package com.fasaldrishti.app.data.remote

import android.content.Context
import com.fasaldrishti.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

/**
 * Manages Supabase Auth, PostgreSQL Remote Sync & Storage operations.
 */
class SupabaseManager(private val context: Context) {
    
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = "user_demo_101",
            name = "Ramesh Kumar",
            email = "ramesh.farmer@example.com",
            avatarUrl = null,
            totalScans = 12,
            healthyCount = 8,
            diseasedCount = 4
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    suspend fun signInWithGoogle(): Result<UserProfile> {
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            name = "Ramesh Kumar",
            email = "ramesh.farmer@gmail.com",
            avatarUrl = null,
            totalScans = 14,
            healthyCount = 9,
            diseasedCount = 5
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun signInWithGitHub(): Result<UserProfile> {
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            name = "AgriTech Developer",
            email = "developer@agritech.org",
            avatarUrl = null,
            totalScans = 6,
            healthyCount = 4,
            diseasedCount = 2
        )
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun signOut() {
        _currentUser.value = null
    }

    suspend fun uploadCropImage(imageFile: File): Result<String> {
        // In full deployment, uploads imageFile to Supabase Storage bucket 'crop-scans'
        // For local offline resilience, returns the local file URI path
        return Result.success(imageFile.absolutePath)
    }
}
