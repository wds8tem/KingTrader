package com.wds.king.trader.ui.util

import android.content.Context
import android.content.pm.PackageManager

class PermissionChecks(private val context: Context) {

    fun lacksPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (lacksPermission(permission)) {
                return true
            }
        }
        return false
    }

    private fun lacksPermission(permission: String): Boolean {
        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED
    }
}
