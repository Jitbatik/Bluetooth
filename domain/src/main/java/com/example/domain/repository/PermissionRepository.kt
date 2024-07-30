package com.example.domain.repository

interface PermissionRepository {
    fun requestPermissions(permissions: Array<String>, requestCode: Int)
}