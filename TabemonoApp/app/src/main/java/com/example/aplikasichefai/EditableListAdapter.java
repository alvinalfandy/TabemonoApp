package com.example.aplikasichefai;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EditableListAdapter extends RecyclerView.Adapter<EditableListAdapter.EditableViewHolder> {

    private Context context;
    private ArrayList<String> items;
    private String itemType;

    public EditableListAdapter(Context context, ArrayList<String> items, String itemType) {
        this.context = context;
        this.items = items;
        this.itemType = itemType;
    }

    @NonNull
    @Override
    public EditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable, parent, false);
        return new EditableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditableViewHolder holder, int position) {
        String item = items.get(position);

        // Set item number (1-based)
        holder.itemNumber.setText(itemType + " " + (position + 1));

        // Set item content
        holder.itemEditText.setText(item);

        // Add TextWatcher to update the list when text changes
        holder.itemEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    items.set(adapterPosition, s.toString());
                }
            }
        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    items.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, items.size() - adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class EditableViewHolder extends RecyclerView.ViewHolder {
        TextView itemNumber;
        EditText itemEditText;
        ImageButton deleteButton;

        public EditableViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNumber = itemView.findViewById(R.id.itemNumber);
            itemEditText = itemView.findViewById(R.id.itemEditText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}