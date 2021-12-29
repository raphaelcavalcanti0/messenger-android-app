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
import com.rcfin.messenger.models.User;
import com.rcfin.messenger.views.ChatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private final List<User> users;
    Context context;
    String uuid;

    public ContactsAdapter(List<User> users, String uuid) {
        this.users = users;
        this.uuid = uuid;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_contacts, parent,false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder,
                                 int position) {

        User user = users.get(position);
        if (user != null) {
            holder.bind(user, uuid);
            holder.relativeLayout.setOnClickListener(v -> {
                user.setBitmap(null);
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("user", user);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        ImageView imgContacts;
        TextView txtContacts;
        Bitmap bitmap;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.contacts_list);
            imgContacts = itemView.findViewById(R.id.img_contacts);
            txtContacts = itemView.findViewById(R.id.txt_contacts);
        }

        public void bind(User user, String me) {
            if (!user.getUuid().equals(me)) {
                txtContacts.setText(user.getName());
                bitmap = user.getBitmap();
                if (bitmap != null) {
                    imgContacts.setImageBitmap(bitmap);
                } else {
                    if (user.getProfileUrl() != null) {
                        imgContacts.setImageBitmap(fetchImages(user.getProfileUrl(), user));
                    }
                }
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
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
