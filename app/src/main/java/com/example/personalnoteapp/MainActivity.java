package com.example.personalnoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
    private ImageButton logoutButton;
    private LinearLayout emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        searchInput = findViewById(R.id.search_input);
        FloatingActionButton fab = findViewById(R.id.fab);
        logoutButton = findViewById(R.id.logout_button);
        emptyView = findViewById(R.id.empty_view);

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

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        setupSearch();
        updateEmptyViewVisibility();
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
        updateEmptyViewVisibility();
    }

    private void updateEmptyViewVisibility() {
        if (filteredNoteList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            notesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            notesRecyclerView.setVisibility(View.VISIBLE);
        }
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
                        filterNotes(searchInput.getText().toString().toLowerCase().trim());
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
        if (currentUser == null) {
            return;
        }

        // To avoid index out of bounds, check if position is valid.
        if (position < 0 || position >= filteredNoteList.size()) {
            return;
        }

        String noteId = filteredNoteList.get(position).getId();

        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("my_notes")
                            .document(noteId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // The snapshot listener will handle UI updates automatically.
                                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
