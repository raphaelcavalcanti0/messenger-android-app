package com.rcfin.messenger.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rcfin.messenger.R;
import com.rcfin.messenger.models.Message;
import com.rcfin.messenger.models.User;
import com.rcfin.messenger.utils.LoadContactsTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Message> lista;
    static List<User> users = new ArrayList<>();
    View view = null;
    String uuid;

    public ChatAdapter(List<Message> lista, String uuid) {
        this.lista = lista;
        this.uuid = uuid;
        users.addAll(LoadContactsTask.listaUsers);
    }

    @Override
    public int getItemViewType(int position) {
        if (!lista.get(position).getFromId().equals(uuid)) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {

        if (viewType == 0) {
            return new ChatViewHolder(view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_from_msgs, parent, false));
        } else {
            return new ChatViewHolder(view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_to_msgs, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder,
                                 int position) {
        Message message = lista.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{
        ImageView chatImg;
        TextView chatTxt;
        Bitmap bitmap;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImg = itemView.findViewById(R.id.chat_img);
            chatTxt = itemView.findViewById(R.id.chat_msg);
        }

        public void bind(Message message) {
            chatTxt.setText(message.getText());
            for (User user : ChatAdapter.users) {
                if (user.getBitmap() != null && message.getFromId().equals(user.getUuid())) {
                    chatImg.setImageBitmap(user.getBitmap());
                } else {
                    if (user.getBitmap() == null && message.getFromId().equals(user.getUuid())) {
                        chatImg.setImageBitmap(fetchImages(user.getProfileUrl(), user));
                    }
                }
            }
        }

        private Bitmap fetchImages(String url, User user) {
            Thread t1 = new Thread(() -> {
                try {
                    bitmap = null;
                    InputStream ims = (InputStream) new URL(url).getContent();
                    bitmap = BitmapFactory.decodeStream(ims);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t1.start();
            try {
                t1.join();
                user.setBitmap(bitmap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}
