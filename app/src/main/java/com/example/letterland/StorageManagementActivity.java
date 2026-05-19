package com.example.letterland;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

public class StorageManagementActivity extends AppCompatActivity {

    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_management);

        MaterialButton btnStorageBack = findViewById(R.id.btnStorageBack);
        MaterialButton btnClearCache = findViewById(R.id.btnClearCache);
        MaterialButton btnDeleteLogs = findViewById(R.id.btnDeleteLogs);
        MaterialButton btnResetData = findViewById(R.id.btnResetData);

        btnStorageBack.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            finish();
        });

        btnClearCache.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            clearAppTemporaryCache();
        });

        btnDeleteLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            showPurgeLogsDialog();
        });

        // 🌟 TRIGGER: The Factory Reset Sequence
        btnResetData.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            showFactoryResetWarning();
        });
    }

    private boolean isSpamClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 500) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }

    // --- CACHE CLEARING LOGIC ---
    private void clearAppTemporaryCache() {
        Toast.makeText(this, "Clearing cache, please wait...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                Glide.get(StorageManagementActivity.this).clearDiskCache();
                deleteDirectoryTree(getCacheDir());
                runOnUiThread(() -> {
                    Glide.get(StorageManagementActivity.this).clearMemory();
                    Toast.makeText(StorageManagementActivity.this, "App cache cleared successfully!", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(StorageManagementActivity.this, "Failed to completely clear cache.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private boolean deleteDirectoryTree(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDirectoryTree(new File(dir, child));
                    if (!success) { return false; }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    // --- PURGE LOGS MENU ---
    private void showPurgeLogsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_purge_logs, null);
        AlertDialog purgeDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (purgeDialog.getWindow() != null) {
            purgeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnPurgeHistoryLogs = dialogView.findViewById(R.id.btnPurgeHistoryLogs);
        MaterialButton btnPurgeUserLogs = dialogView.findViewById(R.id.btnPurgeUserLogs);
        MaterialButton btnPurgeQuizRecords = dialogView.findViewById(R.id.btnPurgeQuizRecords);
        MaterialButton btnPurgeDeletedItems = dialogView.findViewById(R.id.btnPurgeDeletedItems);
        MaterialButton btnCancelPurge = dialogView.findViewById(R.id.btnCancelPurge);

        btnCancelPurge.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            purgeDialog.dismiss();
        });

        btnPurgeHistoryLogs.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            confirmFinalDeletion(1, "History Logs", purgeDialog);
        });

        btnPurgeUserLogs.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            confirmFinalDeletion(2, "User Logs", purgeDialog);
        });

        btnPurgeQuizRecords.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            confirmFinalDeletion(3, "Quiz Records", purgeDialog);
        });

        btnPurgeDeletedItems.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            confirmFinalDeletion(4, "Deleted Items History", purgeDialog);
        });

        purgeDialog.show();
    }

    private void confirmFinalDeletion(int logTypeId, String title, AlertDialog parentDialog) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Purge")
                .setMessage("Are you sure you want to permanently delete all " + title + "? This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    SoundManager.getInstance(this).playClick();
                    parentDialog.dismiss();
                    executePurge(logTypeId, title);
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    SoundManager.getInstance(this).playClick();
                    dialog.dismiss();
                })
                .show();
    }

    private void executePurge(int logTypeId, String title) {
        Toast.makeText(this, "Purging " + title + "...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(StorageManagementActivity.this);
            try {
                if (logTypeId == 1) {
                    db.logDao().deleteHistoryLogs();
                } else if (logTypeId == 2) {
                    db.logDao().deletePlayerActivityLogs();
                } else if (logTypeId == 3) {
                    db.quizRecordDao().deleteAllRecords();
                } else if (logTypeId == 4) {
                    db.logDao().deleteDeletedItemLogs();
                }

                runOnUiThread(() -> {
                    Toast.makeText(StorageManagementActivity.this, title + " wiped successfully!", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(StorageManagementActivity.this, "Failed to purge " + title, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // --- 🌟 NEW: FACTORY RESET SEQUENCE ---
    private void showFactoryResetWarning() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ FACTORY RESET ⚠️")
                .setMessage("Are you ABSOLUTELY sure? This will permanently delete ALL player profiles, ALL quiz scores, ALL almanac objects, and restore the app to its original factory state. \n\nThis CANNOT be undone.")
                .setPositiveButton("YES, WIPE EVERYTHING", (dialog, which) -> {
                    SoundManager.getInstance(this).playClick();
                    executeFactoryReset();
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    SoundManager.getInstance(this).playClick();
                    dialog.dismiss();
                })
                .show();
    }

    private void executeFactoryReset() {
        Toast.makeText(this, "Initiating Factory Reset...", Toast.LENGTH_LONG).show();

        new Thread(() -> {
            try {
                // 1. Drop all data from the Room Database
                AppDatabase.getInstance(StorageManagementActivity.this).clearAllTables();

                // 2. Wipe the SharedPreferences (Profiles, PINs, Active State)
                SharedPreferences prefs = getSharedPreferences("LetterLandMemory", MODE_PRIVATE);
                prefs.edit().clear().apply();

                // 3. Wipe all hidden physical caches and temp files
                Glide.get(StorageManagementActivity.this).clearDiskCache();
                deleteDirectoryTree(getCacheDir());

                // 4. Return to Main UI thread to complete the process
                runOnUiThread(() -> {
                    Glide.get(StorageManagementActivity.this).clearMemory();
                    Toast.makeText(StorageManagementActivity.this, "SYSTEM RESET COMPLETE.", Toast.LENGTH_LONG).show();

                    // 5. Force Reboot to Main Screen to prevent crashing
                    Intent intent = new Intent(StorageManagementActivity.this, MainActivity.class);
                    // This flag clears the back-history so they can't press the back button into a broken screen
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(StorageManagementActivity.this, "Factory Reset Failed.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}