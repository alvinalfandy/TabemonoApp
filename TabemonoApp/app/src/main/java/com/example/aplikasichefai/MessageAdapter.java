package com.example.aplikasichefai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    private String chatId; // Add chat ID to identify the chat

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId, String chatId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.chatId = chatId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Format timestamp
        String formattedTime = timeFormat.format(new Date(message.getTimestamp()));

        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.messageText.setText(message.getText());
            sentHolder.timeText.setText(formattedTime);

            // Set long click listener for sent messages
            sentHolder.itemView.setOnLongClickListener(v -> {
                showMessageOptions(v, message, position, true);
                return true;
            });
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.messageText.setText(message.getText());
            receivedHolder.timeText.setText(formattedTime);

            // Set long click listener for received messages
            receivedHolder.itemView.setOnLongClickListener(v -> {
                showMessageOptions(v, message, position, false);
                return true;
            });
        }
    }

    private void showMessageOptions(View view, Message message, int position, boolean isOwnMessage) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add("Copy");

        // Only allow delete for your own messages
        if (isOwnMessage) {
            popupMenu.getMenu().add("Delete");
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Copy")) {
                copyMessageToClipboard(message.getText());
                return true;
            } else if (item.getTitle().equals("Delete") && isOwnMessage) {
                deleteMessage(message, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void copyMessageToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Message", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void deleteMessage(Message message, int position) {
        // Reference to the message in Firebase
        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(chatId)
                .child(message.getId());

        // Delete the message
        messageRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Message deleted successfully
                    messageList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, messageList.size());
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}