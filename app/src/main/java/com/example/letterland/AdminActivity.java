package com.example.letterland;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class AdminActivity extends AppCompatActivity {

    // 🚀 Tracks the time of the last click to prevent spamming
    private long lastClickTime = 0;

    private MaterialButton btnAdminBack;
    private MaterialButton btnAdminUserLogs;
    private MaterialButton btnAdminQuizRecord;
    private MaterialButton btnAdminAddObject;
    private MaterialButton btnAdminDeletedLogs;
    private MaterialButton btnAdminEditAlmanac;
    private MaterialButton btnResetPin;

    // Memory leak protection
    private AlertDialog pinDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Link UI
        btnAdminBack = findViewById(R.id.btnAdminBack);
        btnAdminUserLogs = findViewById(R.id.btnAdminUserLogs);
        btnAdminQuizRecord = findViewById(R.id.btnAdminQuizRecord);
        btnAdminAddObject = findViewById(R.id.btnAdminAddObject);
        btnAdminDeletedLogs = findViewById(R.id.btnAdminDeletedLogs);
        btnAdminEditAlmanac = findViewById(R.id.btnAdminEditAlmanac);
        btnResetPin = findViewById(R.id.btnResetPin);

        // Back Button
        btnAdminBack.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            finish();
        });

        // 1. OPEN HISTORY LOGS SCREEN (LOCKED)
        btnAdminUserLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, UserLogsActivity.class));
            }
        });

        // 2. OPEN QUIZ RECORD SCREEN (LOCKED)
        btnAdminQuizRecord.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, QuizRecordActivity.class));
            }
        });

        // 3. OPEN ADD OBJECT SCREEN (LOCKED)
        btnAdminAddObject.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, AddObjectActivity.class));
            }
        });

        // 4. OPEN DELETED LOGS SCREEN (UNLOCKED - System wide)
        btnAdminDeletedLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            startActivity(new Intent(AdminActivity.this, DeletedLogsActivity.class));
        });

        // 5. OPEN EDIT ALMANAC SCREEN (LOCKED)
        btnAdminEditAlmanac.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, EditAlmanacActivity.class));
            }
        });

        // 6. RESET PIN LOGIC (UNLOCKED - System wide)
        btnResetPin.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            showResetPinDialog();
        });
    }

    // 🚀 NEW ANTI-SPAM METHOD 🚀
    private boolean isSpamClick() {
        if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return true; // It's a spam click! Ignore it.
        }
        lastClickTime = android.os.SystemClock.elapsedRealtime();
        return false;
    }

    // 🚀 NEW GATEKEEPER METHOD 🚀
    private boolean hasActiveProfile() {
        SharedPreferences prefs = getSharedPreferences("LetterLandMemory", MODE_PRIVATE);
        String activePlayer = prefs.getString("ACTIVE_PROFILE", "");

        if (activePlayer.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_admin_select_player), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showResetPinDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_pin, null);
        pinDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (pinDialog.getWindow() != null) {
            pinDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCurrentPin = dialogView.findViewById(R.id.etCurrentPin);
        EditText etNewPin = dialogView.findViewById(R.id.etNewPin);

        dialogView.findViewById(R.id.btnCancelReset).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            pinDialog.dismiss();
        });

        dialogView.findViewById(R.id.btnConfirmReset).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();

            String currentEntered = etCurrentPin.getText().toString();
            String newPinEntered = etNewPin.getText().toString();

            SharedPreferences prefs = getSharedPreferences("LetterLandMemory", MODE_PRIVATE);
            String savedPin = prefs.getString("ADMIN_PIN", "1234");

            if (!currentEntered.equals(savedPin)) {
                Toast.makeText(this, getString(R.string.toast_current_pin_incorrect), Toast.LENGTH_SHORT).show();
            } else if (newPinEntered.length() < 4) {
                Toast.makeText(this, getString(R.string.toast_new_pin_length), Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putString("ADMIN_PIN", newPinEntered).apply();
                Toast.makeText(this, getString(R.string.toast_pin_updated), Toast.LENGTH_SHORT).show();
                pinDialog.dismiss();
            }
        });

        pinDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pinDialog != null && pinDialog.isShowing()) {
            pinDialog.dismiss();
        }
    }
}