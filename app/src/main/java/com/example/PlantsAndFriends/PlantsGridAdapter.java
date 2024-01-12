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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

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


    public PlantsGridAdapter(Context context, List<Plant> plantsList, AppDatabase appDatabase) {

        this.context = context;
        this.plantsList = plantsList;
        this.appDatabase = appDatabase;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }
    public void switchLayoutMode() {
        isGridMode = !isGridMode;
        Log.d("PlantsGridAdapter", "Switching layout mode to Grid: " + isGridMode);
        notifyLayoutChanged();
    }

    private void notifyLayoutChanged() {
        notifyDataSetChanged();
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
                layoutRes = R.layout.grid_item_plant_title;
                break;
            case VIEW_TYPE_LIST:
                layoutRes = R.layout.list_item_plant_title;
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

        if (getItemViewType(position) == VIEW_TYPE_GRID) {
            // Grid mode: show only the plant title
            holder.plantNameTextView.setText(plant.getName());
            holder.plantTitleSpecieView.setVisibility(View.GONE);
            holder.plantTempTextView.setVisibility(View.GONE);
            holder.plantHumidityTextView.setVisibility(View.GONE);
        } else {
            // List mode: show additional information along with the plant title
            holder.plantNameTextView.setText(plant.getName());
            holder.plantTitleSpecieView.setText(plant.getSpecies());
            holder.plantTempTextView.setText(String.valueOf(plant.getMax_temp()));
            holder.plantHumidityTextView.setText(String.valueOf(plant.getMax_humidity()));

            holder.plantTitleSpecieView.setVisibility(View.VISIBLE);
            holder.plantTempTextView.setVisibility(View.VISIBLE);
            holder.plantHumidityTextView.setVisibility(View.VISIBLE);

            // Adjust layout params for list mode
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.plantNameTextView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            layoutParams.addRule(RelativeLayout.END_OF, R.id.image_view);
            holder.plantNameTextView.setLayoutParams(layoutParams);
        }

        holder.itemView.setOnClickListener(v -> {
            if (plantClickListener != null) {
                plantClickListener.onPlantClick(plantsList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return plantsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantNameTextView;
        TextView plantTitleSpecieView; // Add this line
        TextView plantTempTextView; // Add this line
        TextView plantHumidityTextView; // Add this line

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.plantNameTextView);
            plantTitleSpecieView = itemView.findViewById(R.id.plantSpecieTextView); // Add this line
            plantTempTextView = itemView.findViewById(R.id.plantTempTextView); // Add this line
            plantHumidityTextView = itemView.findViewById(R.id.plantHumTextView); // Add this line
        }
    }


    private void deletePlantAndRefreshView(Plant plant, int position) {
        executor.execute(() -> {
            deletePlantFromLocalStorage(plant);
        });

        // Update the list and notify the adapter
        plantsList.remove(plant);
        notifyItemRemoved(position);
    }

    private void deletePlantFromLocalStorage(Plant plant) {
        appDatabase.plantDao().deletePlantByNumber(String.valueOf(plant.getId()));
        mainHandler.post(() -> Toast.makeText(context, "Plant deleted from local storage", Toast.LENGTH_SHORT).show());
    }

    private void updatePlantName(Plant plant, String newName) {
        executor.execute(() -> {
            appDatabase.plantDao().updatePlantName(plant.getId(), newName);
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
