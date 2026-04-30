package com.example.easynote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        noteList = new ArrayList<>();
        // Inisialisasi adapter dengan Click Listener untuk Edit
        adapter = new NoteAdapter(noteList, note -> showEditNoteDialog(note));
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotes();

        fabAdd.setOnClickListener(v -> showAddNoteDialog());
    }

    private void loadNotes() {
        executor.execute(() -> {
            List<Note> notes = db.noteDao().getAllNotes();
            runOnUiThread(() -> {
                noteList.clear();
                noteList.addAll(notes);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddNoteDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);
        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextContent = view.findViewById(R.id.editTextContent);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tambah Catatan Baru")
                .setView(view)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String title = editTextTitle.getText().toString().trim();
                    String content = editTextContent.getText().toString().trim();
                    if (!title.isEmpty() || !content.isEmpty()) {
                        saveNote(new Note(title, content));
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditNoteDialog(Note note) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);
        EditText editTextTitle = view.findViewById(R.id.editTextTitle);
        EditText editTextContent = view.findViewById(R.id.editTextContent);

        // Isi data lama ke dalam form
        editTextTitle.setText(note.getTitle());
        editTextContent.setText(note.getContent());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Catatan")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    note.setTitle(editTextTitle.getText().toString().trim());
                    note.setContent(editTextContent.getText().toString().trim());
                    updateNote(note);
                })
                .setNegativeButton("Batal", null)
                .setNeutralButton("Hapus", (dialog, which) -> deleteNote(note))
                .show();
    }

    private void saveNote(Note note) {
        executor.execute(() -> {
            db.noteDao().insert(note);
            loadNotes();
        });
    }

    private void updateNote(Note note) {
        executor.execute(() -> {
            db.noteDao().update(note);
            loadNotes();
        });
    }

    private void deleteNote(Note note) {
        executor.execute(() -> {
            db.noteDao().delete(note);
            loadNotes();
        });
    }
}