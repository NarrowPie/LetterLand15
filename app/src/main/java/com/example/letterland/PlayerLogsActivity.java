package com.example.letterland;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlayerLogsActivity extends AppCompatActivity {

    private RecyclerView rvPlayerLogs;
    private TextView tvEmptyLogs;
    private PlayerLogAdapter adapter;
    private List<LogEntry> allLogs = new ArrayList<>();

    // Buttons
    private MaterialButton btnFilterAll, btnFilterDeleted, btnFilterAdded, btnFilterEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_logs);

        // Header Back Button
        findViewById(R.id.btnBackPlayerLogs).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            finish();
        });

        tvEmptyLogs = findViewById(R.id.tvEmptyLogs);
        rvPlayerLogs = findViewById(R.id.rvPlayerLogs);
        rvPlayerLogs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlayerLogAdapter(new ArrayList<>());
        rvPlayerLogs.setAdapter(adapter);

        // Filter Buttons
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterDeleted = findViewById(R.id.btnFilterDeleted);
        btnFilterAdded = findViewById(R.id.btnFilterAdded);
        btnFilterEdited = findViewById(R.id.btnFilterEdited);

        btnFilterAll.setOnClickListener(v -> applyFilter("ALL"));
        btnFilterDeleted.setOnClickListener(v -> applyFilter("DELETED"));
        btnFilterAdded.setOnClickListener(v -> applyFilter("ADDED"));
        btnFilterEdited.setOnClickListener(v -> applyFilter("EDITED"));

        loadLogsFromDatabase();
    }

    private void loadLogsFromDatabase() {
        new Thread(() -> {
            List<LogEntry> rawLogs = AppDatabase.getInstance(this).logDao().getAllLogs();
            allLogs.clear();

            // ONLY grab logs that are specifically Player Activity logs
            for (LogEntry log : rawLogs) {
                if ("PLAYER_LOG".equals(log.action)) {
                    allLogs.add(log);
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                applyFilter("ALL"); // Default filter
            });
        }).start();
    }

    private void applyFilter(String filterType) {
        SoundManager.getInstance(this).playClick();
        List<LogEntry> filteredList = new ArrayList<>();

        for (LogEntry log : allLogs) {
            String[] parts = log.details.split("\\|");
            if (parts.length < 2) continue;
            String type = parts[0]; // "ADDED", "DELETED", "EDITED", "RESTORED"

            if (filterType.equals("ALL")) {
                filteredList.add(log);
            } else if (filterType.equals("DELETED") && type.equals("DELETED")) {
                filteredList.add(log);
            } else if (filterType.equals("ADDED") && (type.equals("ADDED") || type.equals("RESTORED"))) {
                filteredList.add(log);
            } else if (filterType.equals("EDITED") && type.equals("EDITED")) {
                filteredList.add(log);
            }
        }

        if (filteredList.isEmpty()) {
            tvEmptyLogs.setVisibility(View.VISIBLE);
            rvPlayerLogs.setVisibility(View.GONE);
        } else {
            tvEmptyLogs.setVisibility(View.GONE);
            rvPlayerLogs.setVisibility(View.VISIBLE);
            adapter.updateData(filteredList);
        }
    }

    // ==========================================
    // THE ADAPTER
    // ==========================================
    private class PlayerLogAdapter extends RecyclerView.Adapter<PlayerLogAdapter.LogViewHolder> {
        private List<LogEntry> logs;
        private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US);

        public PlayerLogAdapter(List<LogEntry> logs) {
            this.logs = logs;
        }

        public void updateData(List<LogEntry> newLogs) {
            this.logs = newLogs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_log, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            LogEntry log = logs.get(position);

            // Our details string looks like: "ADDED|PlayerName"
            String[] parts = log.details.split("\\|");
            String type = parts.length > 0 ? parts[0] : "UNKNOWN";
            String playerName = parts.length > 1 ? parts[1] : "Unknown Player";

            holder.tvLogPlayerName.setText(playerName);
            holder.tvLogDate.setText(sdf.format(new Date(log.timestamp)));

            // 🎨 FORMAT ROW BASED ON ACTIVITY TYPE
            if (type.equals("DELETED")) {
                holder.tvLogAction.setText("Profile was deleted");
                holder.tvLogAction.setTextColor(Color.parseColor("#F44336")); // Red text
                holder.ivLogIcon.setColorFilter(Color.parseColor("#F44336"));
                holder.ivLogIcon.setImageResource(android.R.drawable.ic_menu_delete);

                // ⭐ SHOW RESTORE BUTTON ONLY FOR DELETED ITEMS!
                holder.btnRestorePlayer.setVisibility(View.VISIBLE);

            } else if (type.equals("ADDED")) {
                holder.tvLogAction.setText("Added as a new profile");
                holder.tvLogAction.setTextColor(Color.parseColor("#4CAF50")); // Green text
                holder.ivLogIcon.setColorFilter(Color.parseColor("#4CAF50"));
                holder.ivLogIcon.setImageResource(android.R.drawable.ic_menu_add);
                holder.btnRestorePlayer.setVisibility(View.GONE);

            } else if (type.equals("RESTORED")) {
                holder.tvLogAction.setText("Profile was restored by Admin");
                holder.tvLogAction.setTextColor(Color.parseColor("#4CAF50")); // Green text
                holder.ivLogIcon.setColorFilter(Color.parseColor("#4CAF50"));
                holder.ivLogIcon.setImageResource(android.R.drawable.ic_menu_revert);
                holder.btnRestorePlayer.setVisibility(View.GONE);

            } else if (type.equals("EDITED")) {
                holder.tvLogAction.setText("Changed avatar image");
                holder.tvLogAction.setTextColor(Color.parseColor("#FF9800")); // Orange text
                holder.ivLogIcon.setColorFilter(Color.parseColor("#FF9800"));
                holder.ivLogIcon.setImageResource(android.R.drawable.ic_menu_edit);
                holder.btnRestorePlayer.setVisibility(View.GONE);
            }

            // 🛠️ THE RESTORE BUTTON LOGIC
            holder.btnRestorePlayer.setOnClickListener(v -> {
                SoundManager.getInstance(PlayerLogsActivity.this).playClick();

                // 1. Add them back to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("LetterLandMemory", MODE_PRIVATE);
                Set<String> oldProfiles = prefs.getStringSet("ALL_PROFILES", new HashSet<>());
                Set<String> newProfiles = new HashSet<>(oldProfiles);

                if (newProfiles.contains(playerName)) {
                    Toast.makeText(PlayerLogsActivity.this, "This profile already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }

                newProfiles.add(playerName);
                prefs.edit().putStringSet("ALL_PROFILES", newProfiles).apply();

                Toast.makeText(PlayerLogsActivity.this, playerName + " has been RESTORED!", Toast.LENGTH_LONG).show();

                // 2. Add a log saying they were restored
                new Thread(() -> {
                    LogEntry restoredLog = new LogEntry("PLAYER_LOG", "RESTORED|" + playerName, System.currentTimeMillis());
                    AppDatabase.getInstance(PlayerLogsActivity.this).logDao().insertLog(restoredLog);

                    // 3. Reload the UI!
                    runOnUiThread(PlayerLogsActivity.this::loadLogsFromDatabase);
                }).start();
            });
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class LogViewHolder extends RecyclerView.ViewHolder {
            ImageView ivLogIcon;
            TextView tvLogPlayerName, tvLogAction, tvLogDate;
            MaterialButton btnRestorePlayer;

            public LogViewHolder(@NonNull View itemView) {
                super(itemView);
                ivLogIcon = itemView.findViewById(R.id.ivLogIcon);
                tvLogPlayerName = itemView.findViewById(R.id.tvLogPlayerName);
                tvLogAction = itemView.findViewById(R.id.tvLogAction);
                tvLogDate = itemView.findViewById(R.id.tvLogDate);
                btnRestorePlayer = itemView.findViewById(R.id.btnRestorePlayer);
            }
        }
    }
}