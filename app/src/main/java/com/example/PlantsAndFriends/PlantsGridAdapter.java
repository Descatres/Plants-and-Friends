package com.example.PlantsAndFriends;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlantsGridAdapter extends RecyclerView.Adapter<PlantsGridAdapter.ViewHolder> {
    private List<Plant> plantsList;
    private LayoutInflater inflater;
    private Context context;
    private OnPlantClickListener plantClickListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AppDatabase appDatabase;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public PlantsGridAdapter(Context context, List<Plant> plantsList, AppDatabase appDatabase) {
        this.context = context;
        this.plantsList = plantsList;
        this.appDatabase = appDatabase;
        inflater = LayoutInflater.from(context);
    }


    public interface OnPlantClickListener {
        void onPlantClick(Plant plant);
    }

    public void setOnPlantClickListener(OnPlantClickListener listener) {
        this.plantClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.grid_item_plant, parent, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plant plant = plantsList.get(position);
        holder.plantTitleTextView.setText(plant.getName());
        if (plant.getImgUri() != null && !plant.getImgUri().isEmpty() && isNetworkConnected()) {
            Picasso.get().load(plant.getImgUri()).into(holder.plantImageView);
            loadImage(holder, Uri.parse(plant.getImgUri()));
        } else if (!isNetworkConnected()) {
            loadImage(holder, Uri.parse(plant.getImgUri()));
        } else {
            // Set a placeholder image if the URI is null or empty
            holder.plantImageView.setImageResource(R.drawable.plant_logo);
        }

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(plant, position);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (plantClickListener != null && position < plantsList.size()) {
                plantClickListener.onPlantClick(plantsList.get(position));
            }
        });
    }

    private void loadImage(@NonNull ViewHolder holder, Uri imageUri) {
        Glide.with(context).load(imageUri).into(holder.plantImageView);
    }

    @Override
    public int getItemCount() {
        return plantsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantTitleTextView;
        ImageView plantImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plantTitleTextView = itemView.findViewById(R.id.plant_title_text_view);
            plantImageView = itemView.findViewById(R.id.plant_image_view);
        }
    }

    private void deletePlantAndRefreshView(Plant plant, int position) {
        if (isNetworkConnected()) {
            deletePlantFromFirestore(plant);
            mainHandler.post(() -> Toast.makeText(context, "Pant deleted from local storage and Firestore", Toast.LENGTH_SHORT).show());
        } else {
            mainHandler.post(() -> Toast.makeText(context, "Pant deleted from local storage", Toast.LENGTH_SHORT).show());
        }

        deletePlantFromLocalStorage(plant);

        mainHandler.post(() -> {
            plantsList.remove(position);
            notifyItemRemoved(position);
        });

    }

    private void deletePlantFromLocalStorage(Plant plant) {
        executor.execute(() -> {
            appDatabase.plantDao().deletePlantByNumber(plant.getNumber());
        });
    }

    private void deletePlantFromFirestore(Plant plant) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String currentUserUid = currentUser.getUid();
        executor.execute(() -> {
            db.collection("users").document(currentUserUid).collection("plants")
                    .document(plant.getNumber())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        mainHandler.post(() -> Log.d("deletePlantFromFirestore", "Plant deleted from Firestore"));
                    })
                    .addOnFailureListener(e -> {
                        mainHandler.post(() -> Toast.makeText(context, "Failed to delete plant from Firestore", Toast.LENGTH_SHORT).show());
                    });
        });
    }

    private void updatePlantName(Plant plant, String newName) {
        executor.execute(() -> {
            appDatabase.plantDao().updatePlantName(plant.getNumber(), newName);
        });
    }

    private void updatePlantNameFirestore(Plant plant, String newName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        executor.execute(() -> {
            db.collection("users").document(currentUser.getUid()).collection("plants")
                    .document(plant.getNumber())
                    .update("name", newName)
                    .addOnSuccessListener(aVoid -> {
                        mainHandler.post(() -> Log.d("updatePlantNameFirestore", "Plant name updated in Firestore"));
                    })
                    .addOnFailureListener(e -> {
                        mainHandler.post(() -> Toast.makeText(context, "Failed to update plant name in Firestore", Toast.LENGTH_SHORT).show());
                    });
        });
    }

    private void showRenamePlantDialog(Plant plant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a new title for your plant");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newTitle = input.getText().toString();
            updatePlantName(plant, newTitle);
            if (isNetworkConnected()) {
                updatePlantNameFirestore(plant, newTitle);
            }
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

        builder.setPositiveButton("Rename plant", (dialog, which) -> showRenamePlantDialog(plant));
        builder.setNegativeButton("Delete plant", (dialog, which) -> deletePlantAndRefreshView(plant, position));
        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }

    public void updatePlants(List<Plant> newPlantsList) {
        int oldSize = plantsList.size();
        int newSize = newPlantsList.size();

        // Find the common prefix between old and new lists
        int commonPrefix = 0;
        while (commonPrefix < oldSize && commonPrefix < newSize &&
                plantsList.get(commonPrefix).equals(newPlantsList.get(commonPrefix))) {
            commonPrefix++;
        }

        // Notify items that were removed
        for (int i = oldSize - 1; i >= commonPrefix; i--) {
            plantsList.remove(i);
            notifyItemRemoved(i);
        }

        // Notify items that were added
        for (int i = commonPrefix; i < newSize; i++) {
            plantsList.add(newPlantsList.get(i));
            notifyItemInserted(i);
        }

        // Notify items that were changed
        for (int i = commonPrefix; i < newSize; i++) {
            if (!plantsList.get(i).equals(newPlantsList.get(i))) {
                plantsList.set(i, newPlantsList.get(i));
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
