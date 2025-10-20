package com.example.personalnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {

    private RecyclerView notesRecyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        FloatingActionButton fab = findViewById(R.id.fab);

        db = FirebaseFirestore.getInstance();
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList);
        noteAdapter.setOnNoteListener(this);

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
        });

        loadNotes();
    }

    private void loadNotes() {
        db.collection("notes")
                .orderBy("title", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this, "Error loading notes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        noteList.clear();
                        for (DocumentSnapshot document : value.getDocuments()) {
                            Note note = document.toObject(Note.class);
                            if (note != null) {
                                note.setId(document.getId());
                                noteList.add(note);
                            }
                        }
                        noteAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(this, AddNoteActivity.class);
        intent.putExtra("noteId", noteList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("notes").document(noteList.get(position).getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
