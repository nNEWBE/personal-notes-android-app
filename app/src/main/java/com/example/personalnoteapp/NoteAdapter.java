package com.example.personalnoteapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> notes;
    private OnNoteListener onNoteListener;

    public interface OnNoteListener {
        void onNoteClick(int position);
        void onDeleteClick(int position);
    }

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    public void setOnNoteListener(OnNoteListener onNoteListener) {
        this.onNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());

        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext()).load(note.getImageUrl()).into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onNoteListener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onNoteListener.onNoteClick(currentPosition);
                }
            }
        });

        holder.deleteNote.setOnClickListener(v -> {
            if (onNoteListener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onNoteListener.onDeleteClick(currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        ImageView image;
        ImageView deleteNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            content = itemView.findViewById(R.id.note_content);
            image = itemView.findViewById(R.id.note_image);
            deleteNote = itemView.findViewById(R.id.delete_note);
        }
    }
}
