package com.rcfin.messenger.utils;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.ContactsAdapter;
import com.rcfin.messenger.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadContactsTask {

    FragmentActivity activity;
    ExecutorService service;
    Context context;
    List<DocumentSnapshot> docs;
    List<User> users = new ArrayList<>();
    public static List<User> listaUsers = new ArrayList<>();
    ContactsAdapter adapter;
    RecyclerView recyclerView;
    String uuid;
    ProgressBar loadingContacts;

    public LoadContactsTask(FragmentActivity activity, ContactsAdapter adapter,
                            RecyclerView recyclerView, String uuid) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.uuid = uuid;
    }

    public void start() {
        service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            // onPreExecute
            loadingContacts = activity.findViewById(R.id.loadingContacts);
            if (loadingContacts != null) {
                activity.runOnUiThread(() -> loadingContacts.setVisibility(View.VISIBLE));
            }
            // doInBackgroud
            verifyConnection();
            if (listaUsers.isEmpty()) {
                FirebaseFirestore.getInstance().collection("/users").orderBy("name")
                        .addSnapshotListener(activity, (value, error) -> {
                            if (value != null) {
                                docs = value.getDocuments();
                                users = new ArrayList<>();
                                for (DocumentSnapshot doc : docs) {
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        users.add(user);
                                    }
                                }
                                listaUsers.addAll(users);
                                activity.runOnUiThread(() -> {
                                    adapter = new ContactsAdapter(users, uuid);
                                    recyclerView.setAdapter(adapter);
                                });

                            }
                        });
            } else {
                users.addAll(listaUsers);
                activity.runOnUiThread(() -> {
                    adapter = new ContactsAdapter(users, uuid);
                    recyclerView.setAdapter(adapter);
                });
            }
            // onPostExecute
            if (loadingContacts != null) {
                activity.runOnUiThread(() -> loadingContacts.setVisibility(View.GONE));
            }
        });
    }

    public void searchContacts(String search) {
        List<User> filteredUsers = new ArrayList<>();

        for (User user : users) {
            if (user.getName().toLowerCase(Locale.ROOT).trim().contains(search.toLowerCase(Locale.ROOT))) {
                filteredUsers.add(user);
            }
        }

        adapter = new ContactsAdapter(filteredUsers, uuid);
        recyclerView.setAdapter(adapter);
    }

    public void searchBack() {
        adapter = new ContactsAdapter(users, uuid);
        recyclerView.setAdapter(adapter);
    }

    public void verifyConnection() {
        if (!CheckConnection.isNetworkAvailable(context)) {
            activity.runOnUiThread(() -> Toast.makeText(context, "Sem conexão com a internet.",
                    Toast.LENGTH_SHORT).show());
        }
    }
}
