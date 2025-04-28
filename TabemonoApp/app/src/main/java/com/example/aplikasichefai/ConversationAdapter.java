package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private Context context;
    private List<Conversation> conversationList;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        User user = conversation.getUser();

        // Set user info
        if (user != null) {
            holder.username.setText(user.getUsername());

            // Load profile image
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(user.getProfileImageUrl())
                        .circleCrop()
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.ic_profile);
            }
        }

        // Set last message
        holder.lastMessage.setText(conversation.getLastMessage());

        // Format and set time
        Date messageDate = new Date(conversation.getTimestamp());
        Date today = new Date();

        // Compare if message is from today
        String formattedTime;
        if (isSameDay(messageDate, today)) {
            formattedTime = timeFormat.format(messageDate);
        } else {
            formattedTime = dateFormat.format(messageDate);
        }
        holder.time.setText(formattedTime);

        // Handle unread indicator
        if (conversation.isUnread()) {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.lastMessage.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            holder.unreadIndicator.setVisibility(View.GONE);
            holder.lastMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.putExtra("username", user.getUsername());
            context.startActivity(intent);
        });
    }

    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(date1).equals(fmt.format(date2));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, lastMessage, time;
        View unreadIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
}
