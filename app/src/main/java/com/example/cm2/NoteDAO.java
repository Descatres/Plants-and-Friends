package com.example.cm2;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteEntity note);

    @Query("SELECT * FROM notes")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM notes WHERE number = :noteNumber LIMIT 1")
    NoteEntity getNoteByNumber(String noteNumber);

    @Query("DELETE FROM notes WHERE number = :noteNumber")
    void deleteNoteByNumber(String noteNumber);

    @Query("DELETE FROM notes")
    void deleteAllNotes();

    @Query("UPDATE notes SET title = :title WHERE number = :noteNumber")
    void updateNoteTitle(String noteNumber, String title);

    @Query("UPDATE notes SET content = :content WHERE number = :noteNumber")
    void updateNoteContent(String noteNumber, String content);
}
