package com.example.letterland;

import androidx.room.Dao;
import androidx.room.Delete;
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

    // 🌟 NEW: Deletes the log record when an item is successfully restored
    @Delete
    void deleteLog(LogEntry log);

    // 🌟 NEW: Fixes the rename bug by updating older profile name strings stored within past logs
    @Query("UPDATE log_table SET details = REPLACE(details, '|' || :oldName, '|' || :newName) WHERE action = 'DELETED WORD' AND details LIKE '%|' || :oldName")
    void updateProfileNameInDeletedLogs(String oldName, String newName);
}