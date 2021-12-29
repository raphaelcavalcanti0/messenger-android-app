package com.rcfin.messenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rcfin.messenger.R;
import com.rcfin.messenger.models.Chat;
import com.rcfin.messenger.models.User;
import com.rcfin.messenger.views.ChatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MsgsAdapter extends RecyclerView.Adapter<MsgsAdapter.MsgsViewHolder> {

    private final List<Chat> chats;
    private final HashMap<String, User> usersMap;
    Context context;

    public MsgsAdapter(List<Chat> chats, HashMap<String, User> usersMap) {
        this.chats = chats;
        this.usersMap = usersMap;
        ordenarTimestamp();
    }

    @NonNull
    @Override
    public MsgsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_msgs, parent, false);
        return new MsgsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgsViewHolder holder,
                                 int position) {

        Chat chat = chats.get(position);

        User user = usersMap.get(chat.getUuid());
        if (user != null) {
            holder.bind(chat, user);
            holder.relativeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                user.setBitmap(null);
                intent.putExtra("user", user);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    private void ordenarTimestamp() {
        for (int init = 0; init < chats.size(); init++) {
            for (int current = 1; current < chats.size(); current++) {
                if (chats.get(current).getTimestamp() > chats.get(init).getTimestamp()) {
                    Collections.swap(chats, current, init);
                }
            }
        }
    }

    static class MsgsViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView msg_user, msg_last;
        ImageView msg_img;
        Bitmap bitmap;

        public MsgsViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.msg_list);
            msg_user = itemView.findViewById(R.id.msg_user);
            msg_last = itemView.findViewById(R.id.msg_last);
            msg_img = itemView.findViewById(R.id.msg_img);
        }

        public void bind(Chat chat, User user) {
            msg_user.setText(chat.getName());
            msg_last.setText(chat.getLastMsg());
            if (user.getBitmap() != null) {
                msg_img.setImageBitmap(user.getBitmap());
            } else {
                if (user.getProfileUrl() != null) {
                    msg_img.setImageBitmap(fetchImages(user.getProfileUrl(), user));
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
