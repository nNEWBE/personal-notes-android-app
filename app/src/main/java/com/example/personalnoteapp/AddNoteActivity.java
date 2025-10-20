package com.example.personalnoteapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddNoteActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSave;

    private FirebaseFirestore db;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        buttonSave = findViewById(R.id.button_save);

        db = FirebaseFirestore.getInstance();

        noteId = getIntent().getStringExtra("noteId");

        if (noteId != null) {
            loadNote();
        }

        buttonSave.setOnClickListener(v -> saveNote());
    }

    private void loadNote() {
        db.collection("notes").document(noteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            editTextTitle.setText(note.getTitle());
                            editTextContent.setText(note.getContent());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AddNoteActivity.this, "Error loading note", Toast.LENGTH_SHORT).show());
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please enter a title and content", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title, content, ""); // Empty image URL for now

        if (noteId != null) {
            db.collection("notes").document(noteId).set(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("notes").add(note)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show());
        }
    }
}
