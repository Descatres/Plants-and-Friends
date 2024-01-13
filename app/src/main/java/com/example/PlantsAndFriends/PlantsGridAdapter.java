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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private static final int VIEW_TYPE_GRID = 1;
    private static final int VIEW_TYPE_LIST = 2;
    private boolean isGridMode = true;
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

    public void switchLayoutMode() {
        isGridMode = !isGridMode;
        Log.d("PlantsGridAdapter", "Switching layout mode to Grid: " + isGridMode);
        notifyLayoutChanged();
    }

    private void notifyLayoutChanged() {
        notifyItemRangeChanged(0, plantsList.size());
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
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
        int layoutRes;
        switch (viewType) {
            case VIEW_TYPE_GRID:
                layoutRes = R.layout.grid_item_plant;
                break;
            case VIEW_TYPE_LIST:
                layoutRes = R.layout.list_item_plant;
                break;
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
        View view = inflater.inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plant plant = plantsList.get(position);

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(plant, position);
            return true;
        });

        // Image
        if (plant.getImgUri() != null && !plant.getImgUri().isEmpty() && isNetworkConnected()) {
            Picasso.get().load(plant.getImgUri()).into(holder.plantImageView);
            loadImage(holder, Uri.parse(plant.getImgUri()));
        } else if (!isNetworkConnected()) {
            loadImage(holder, Uri.parse(plant.getImgUri()));
        } else {
            // Set a placeholder image if the URI is null or empty
            holder.plantImageView.setImageResource(R.drawable.default_image_homepage);
        }
        holder.plantNameTextView.setText(plant.getName());
        // List vs Grid
        if (getItemViewType(position) == VIEW_TYPE_GRID) {
            // Grid mode: show only the plant title
            holder.plantTitleSpecieView.setVisibility(View.GONE);
            holder.plantMinTempTextView.setVisibility(View.GONE);
            holder.plantMinHumidityTextView.setVisibility(View.GONE);
        } else {
            // List mode: show additional information along with the plant title
            holder.plantTitleSpecieView.setText(plant.getSpecies());
            holder.plantMinTempTextView.setText(String.format("%.2f°C", plant.getMin_temp()));
            holder.plantMinHumidityTextView.setText(String.format("%.2f%%", plant.getMin_humidity()));
            holder.plantMaxTempTextView.setText(String.format("|  %.2f°C", plant.getMax_temp()));
            holder.plantMaxHumidityTextView.setText(String.format("|  %.2f%%", plant.getMax_humidity()));

            holder.plantTitleSpecieView.setVisibility(View.VISIBLE);
            holder.plantMinTempTextView.setVisibility(View.VISIBLE);
            holder.plantMinHumidityTextView.setVisibility(View.VISIBLE);

            // Adjust layout params for list mode
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.plantNameTextView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            layoutParams.addRule(RelativeLayout.END_OF, R.id.plant_image_view);
            holder.plantNameTextView.setLayoutParams(layoutParams);
        }

        holder.itemView.setOnClickListener(v -> {
            if (plantClickListener != null) {
                plantClickListener.onPlantClick(plantsList.get(position));
            }
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
        TextView plantNameTextView;
        TextView plantTitleSpecieView;
        TextView plantMaxHumidityTextView;
        TextView plantMinHumidityTextView;
        TextView plantMaxTempTextView;
        TextView plantMinTempTextView;
        ImageView plantImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImageView = itemView.findViewById(R.id.plant_image_view);
            plantNameTextView = itemView.findViewById(R.id.plantNameTextView);
            plantTitleSpecieView = itemView.findViewById(R.id.plantSpecieTextView); // Add this line
            plantMaxHumidityTextView = itemView.findViewById(R.id.plantMaxHumTextView); // Add this line
            plantMinHumidityTextView = itemView.findViewById(R.id.plantMinHumTextView); // Add this line
            plantMaxTempTextView = itemView.findViewById(R.id.plantMaxTempTextView); // Add this line
            plantMinTempTextView = itemView.findViewById(R.id.plantMinTempTextView); // Add this line
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
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

//    private void showRenamePlantDialog(Plant plant) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Enter a new title for your plant");
//
//        final EditText input = new EditText(context);
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(input);
//
//        builder.setPositiveButton("Rename", (dialog, which) -> {
//            String newTitle = input.getText().toString();
//            updatePlantName(plant, newTitle);
//            if (isNetworkConnected()) {
//                updatePlantNameFirestore(plant, newTitle);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
//    }

    private void showOptionsDialog(Plant plant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete plant?");
        builder.setMessage("Are you sure you want to delete this plant? This will delete the plant from your local storage and from Firestore.");

//        builder.setPositiveButton("Rename plant", (dialog, which) -> showRenamePlantDialog(plant));
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
