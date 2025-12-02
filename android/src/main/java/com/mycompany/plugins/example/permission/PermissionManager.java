package com.mycompany.plugins.example.permission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

/**
 * 权限管理模块
 * 负责存储权限的请求和检查
 */
public class PermissionManager {
    private final Plugin plugin;

    public PermissionManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查是否有存储权限
     */
    public boolean hasStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE 权限
            return Environment.isExternalStorageManager();
        }

        return ContextCompat.checkSelfPermission(plugin.getContext(), 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(plugin.getContext(), 
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查权限
     */
    public void checkPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        boolean granted = hasStoragePermissions();
        ret.put("granted", granted);
        ret.put("message", granted ? "Storage permissions granted" : "Storage permissions not granted");
        call.resolve(ret);
    }

    /**
     * 请求权限（Android 11+）
     */
    public void requestManageExternalStoragePermission(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + plugin.getContext().getPackageName()));
            plugin.startActivityForResult(call, intent, "manageStoragePermissionResult");
        } catch (Exception e) {
            // 如果无法打开设置页面，尝试通用设置
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            plugin.startActivityForResult(call, intent, "manageStoragePermissionResult");
        }
    }

    /**
     * 创建权限结果对象
     */
    public JSObject createPermissionResult(boolean granted, String message) {
        JSObject ret = new JSObject();
        ret.put("granted", granted);
        ret.put("message", message);
        return ret;
    }
}
