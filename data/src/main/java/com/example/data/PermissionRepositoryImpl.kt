package com.example.data

import android.app.Activity
import androidx.core.app.ActivityCompat
import com.example.domain.repository.PermissionRepository
import javax.inject.Inject

class PermissionRepositoryImpl @Inject constructor(
    private val activity: Activity
): PermissionRepository {
    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }
}