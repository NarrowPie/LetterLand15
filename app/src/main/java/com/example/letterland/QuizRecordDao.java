package com.example.letterland;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuizRecordDao {

    // Save a new score
    @Insert
    void insertRecord(QuizRecord record);

    // Grab all scores for the Admin Panel, newest first
    @Query("SELECT * FROM quiz_record_table ORDER BY timestamp DESC")
    List<QuizRecord> getAllRecords();
}