package com.example.letterland;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Insert
    void insertLog(LogEntry log);

    // Grab all logs, newest at the top
    @Query("SELECT * FROM log_table ORDER BY timestamp DESC")
    List<LogEntry> getAllLogs();
}