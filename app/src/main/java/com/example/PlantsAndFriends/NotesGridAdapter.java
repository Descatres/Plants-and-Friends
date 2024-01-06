package com.example.PlantsAndFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotesGridAdapter extends RecyclerView.Adapter<NotesGridAdapter.ViewHolder> {
    private List<Plant> notesList;
    private LayoutInflater inflater;
    private Context context;
    private OnNoteClickListener noteClickListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;


    public NotesGridAdapter(Context context, List<Plant> notesList, AppDatabase appDatabase) {
        this.context = context;
        this.notesList = notesList;
        this.appDatabase = appDatabase;
        inflater = LayoutInflater.from(context);
    }


    public interface OnNoteClickListener {
        void onNoteClick(Plant plant);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.noteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.grid_item_note_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plant plant = notesList.get(position);
        holder.noteTitleTextView.setText(plant.getTitle());

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(plant, position);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (noteClickListener != null) {
                noteClickListener.onNoteClick(notesList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitleTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitleTextView = itemView.findViewById(R.id.noteTitleTextView);
        }
    }

    private void deleteNoteAndRefreshView(Plant plant, int position) {
        executor.execute(() -> {
            deleteNoteFromLocalStorage(plant);
        });

        // Update the list and notify the adapter
        notesList.remove(plant);
        notifyItemRemoved(position);
    }

    private void deleteNoteFromLocalStorage(Plant plant) {
        appDatabase.noteDao().deleteNoteByNumber(plant.getNumber());
        mainHandler.post(() -> Toast.makeText(context, "Note deleted from local storage", Toast.LENGTH_SHORT).show());
    }

    private void updateNoteTitle(Plant plant, String newTitle) {
        executor.execute(() -> {
            appDatabase.noteDao().updateNoteTitle(plant.getNumber(), newTitle);
        });
    }

    private void showRenameNoteDialog(Plant plant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a new title for your note");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString();
            updateNoteTitle(plant, newTitle);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
    }

    private void showOptionsDialog(Plant plant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose an action");

        builder.setPositiveButton("Rename note", (dialog, which) -> showRenameNoteDialog(plant));
        builder.setNegativeButton("Delete note", (dialog, which) -> deleteNoteAndRefreshView(plant, position));
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }

    public void updateNotes(List<Plant> newNotesList) {
        int oldSize = notesList.size();
        int newSize = newNotesList.size();

        // Find the common prefix between old and new lists
        int commonPrefix = 0;
        while (commonPrefix < oldSize && commonPrefix < newSize &&
                notesList.get(commonPrefix).equals(newNotesList.get(commonPrefix))) {
            commonPrefix++;
        }

        // Notify items that were removed
        for (int i = oldSize - 1; i >= commonPrefix; i--) {
            notesList.remove(i);
            notifyItemRemoved(i);
        }

        // Notify items that were added
        for (int i = commonPrefix; i < newSize; i++) {
            notesList.add(newNotesList.get(i));
            notifyItemInserted(i);
        }

        // Notify items that were changed
        for (int i = commonPrefix; i < newSize; i++) {
            if (!notesList.get(i).equals(newNotesList.get(i))) {
                notesList.set(i, newNotesList.get(i));
                notifyItemChanged(i);
            }
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
            }
        }

        // If connectivity manager is null, assume there is no active network connection
        return false;
    }
}
