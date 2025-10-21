package com.example.personalnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener {

    private RecyclerView notesRecyclerView;
    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private List<Note> filteredNoteList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextInputEditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        searchInput = findViewById(R.id.search_input);
        FloatingActionButton fab = findViewById(R.id.fab);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        noteList = new ArrayList<>();
        filteredNoteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(filteredNoteList);
        noteAdapter.setOnNoteListener(this);

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
        });

        setupSearch();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotes(String query) {
        filteredNoteList.clear();
        if (query.isEmpty()) {
            filteredNoteList.addAll(noteList);
        } else {
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(query)) {
                    filteredNoteList.add(note);
                }
            }
        }
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            loadNotes(currentUser.getUid());
        }
    }

    private void loadNotes(String userId) {
        db.collection("users").document(userId).collection("my_notes")
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
                        // Update filtered list with all notes initially
                        filteredNoteList.clear();
                        filteredNoteList.addAll(noteList);
                        noteAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(this, AddNoteActivity.class);
        intent.putExtra("noteId", filteredNoteList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String noteId = filteredNoteList.get(position).getId();
                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("my_notes")
                            .document(noteId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Remove from both lists
                                for (int i = 0; i < noteList.size(); i++) {
                                    if (noteList.get(i).getId().equals(noteId)) {
                                        noteList.remove(i);
                                        break;
                                    }
                                }
                                filteredNoteList.remove(position);
                                noteAdapter.notifyItemRemoved(position);
                                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}