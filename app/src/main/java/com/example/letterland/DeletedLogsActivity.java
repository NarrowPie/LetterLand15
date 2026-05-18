package com.example.letterland;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeletedLogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_logs);

        MaterialButton btnBack = findViewById(R.id.btnDeletedLogsBack);
        ListView lvDeletedLogs = findViewById(R.id.lvDeletedLogs);

        btnBack.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            finish();
        });

        // Get logs from Database
        AppDatabase db = AppDatabase.getInstance(this);
        List<LogEntry> logs = db.logDao().getAllLogs();

        // Format the logs to display in a simple ListView
        ArrayList<String> displayList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

        for (LogEntry log : logs) {
            String dateString = sdf.format(new Date(log.timestamp));
            displayList.add("Action: " + log.action + "\n" + log.details + "\nTime: " + dateString);
        }

        // 🚀 THIS LINE WAS UPDATED TO USE YOUR NEW CUSTOM LAYOUT 🚀
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_deleted_log, displayList);
        lvDeletedLogs.setAdapter(adapter);
    }
}