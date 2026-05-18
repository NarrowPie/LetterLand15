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

    private long lastClickTime = 0;

    private MaterialButton btnAdminBack;
    private MaterialButton btnAdminUserLogs;
    private MaterialButton btnAdminPlayerLogs; // 🌟 ADDED OUR NEW BUTTON HERE
    private MaterialButton btnAdminQuizRecord;
    private MaterialButton btnAdminAddObject;
    private MaterialButton btnAdminDeletedLogs;
    private MaterialButton btnAdminEditAlmanac;
    private MaterialButton btnResetPin;

    private AlertDialog pinDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Link UI
        btnAdminBack = findViewById(R.id.btnAdminBack);
        btnAdminUserLogs = findViewById(R.id.btnAdminUserLogs);
        btnAdminPlayerLogs = findViewById(R.id.btnAdminPlayerLogs); // 🌟 LINKED IT TO THE XML
        btnAdminQuizRecord = findViewById(R.id.btnAdminQuizRecord);
        btnAdminAddObject = findViewById(R.id.btnAdminAddObject);
        btnAdminDeletedLogs = findViewById(R.id.btnAdminDeletedLogs);
        btnAdminEditAlmanac = findViewById(R.id.btnAdminEditAlmanac);
        btnResetPin = findViewById(R.id.btnResetPin);

        btnAdminBack.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            finish();
        });

        // 1. History Logs
        btnAdminUserLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, UserLogsActivity.class));
            }
        });

        // 🌟 2. OUR NEW CLICK EVENT: Open the Player Logs Screen!
        btnAdminPlayerLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            // This opens the new java file we are about to create below!
            startActivity(new Intent(AdminActivity.this, PlayerLogsActivity.class));
        });

        // 3. Quiz Records
        btnAdminQuizRecord.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, QuizRecordActivity.class));
            }
        });

        // 4. Add Object
        btnAdminAddObject.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, AddObjectActivity.class));
            }
        });

        // 5. Deleted Logs
        btnAdminDeletedLogs.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            startActivity(new Intent(AdminActivity.this, DeletedLogsActivity.class));
        });

        // 6. Edit Almanac
        btnAdminEditAlmanac.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            if (hasActiveProfile()) {
                startActivity(new Intent(AdminActivity.this, EditAlmanacActivity.class));
            }
        });

        // 7. Reset Pin
        btnResetPin.setOnClickListener(v -> {
            if (isSpamClick()) return;
            SoundManager.getInstance(this).playClick();
            showResetPinDialog();
        });
    }

    private boolean isSpamClick() {
        if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = android.os.SystemClock.elapsedRealtime();
        return false;
    }

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