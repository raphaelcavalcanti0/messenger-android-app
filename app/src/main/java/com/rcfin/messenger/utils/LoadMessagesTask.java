package com.rcfin.messenger.utils;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.MsgsAdapter;
import com.rcfin.messenger.models.Chat;
import com.rcfin.messenger.models.Message;
import com.rcfin.messenger.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadMessagesTask {

    FragmentActivity activity;
    ExecutorService service;
    Context context;
    MsgsAdapter adapter;
    RecyclerView recyclerView;
    List<Message> msgs = new ArrayList<>();
    List<User> users = new ArrayList<>();
    List<Chat> chats = new ArrayList<>();
    HashMap<String, User> usersMap = new HashMap<>();
    Map<String, Chat> chatsMap = new HashMap<>();
    String toId, uuid;
    ProgressBar loadingMsgs;

    public LoadMessagesTask(FragmentActivity activity, MsgsAdapter adapter, RecyclerView recyclerView,
                            String uuid) {
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
            loadingMsgs = activity.findViewById(R.id.loadingMsgs);
            if (loadingMsgs != null) {
                activity.runOnUiThread(() -> loadingMsgs.setVisibility(View.VISIBLE));
            }
            // doInBackground
            verifyConnection();
            if (LoadContactsTask.listaUsers.isEmpty()) {
                FirebaseFirestore.getInstance().collection("/users")
                        .addSnapshotListener(activity, (value, error) -> {
                            if (value != null) {
                                List<DocumentSnapshot> docs = value.getDocuments();
                                users = new ArrayList<>();
                                for (DocumentSnapshot doc : docs) {
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        usersMap.put(user.getUuid(), user);
                                        users.add(user);
                                    }
                                }
                                LoadContactsTask.listaUsers.addAll(users);
                                fetchMessages();
                            }
                        });
            } else {
                users.addAll(LoadContactsTask.listaUsers);
                if (usersMap.isEmpty()) {
                    for (User user : users) {
                        if (user != null) {
                            usersMap.put(user.getUuid(), user);
                        }
                    }
                }
                fetchMessages();
            }
            // onPostExecute
            if (loadingMsgs != null) {
                activity.runOnUiThread(() -> loadingMsgs.setVisibility(View.GONE));
            }
        });
    }

    private void fetchMessages() {
        toId = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance()
                .collectionGroup(toId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        Message message = doc.toObject(Message.class);
                        assert message != null;
                        if (message.getToId().equals(toId) || message.getFromId().equals(toId)) {
                            msgs.add(message);
                        }
                    }

                    Collections.sort(msgs, Message::compareTo);

                    for (Message msg : msgs) {
                        Chat chat = new Chat();
                        if (msg.getToId().equals(uuid)) {
                            chat.setTimestamp(msg.getTimestamp());
                            chat.setUuid(msg.getFromId());
                            chat.setName(Objects.requireNonNull(usersMap.get(msg.getFromId())).getName());
                            chat.setLastMsg(msg.getText());
                        } else {
                            if (msg.getFromId().equals(uuid)) {
                                chat.setTimestamp(msg.getTimestamp());
                                chat.setUuid(msg.getToId());
                                chat.setName(Objects.requireNonNull(usersMap.get(msg.getToId())).getName());
                                chat.setLastMsg("Você: " + msg.getText());
                            }
                        }
                        if (!chatsMap.containsKey(chat.getUuid())) {
                            chatsMap.put(chat.getUuid(), chat);
                        } else {
                            Objects.requireNonNull(chatsMap.get(chat.getUuid())).setTimestamp(chat.getTimestamp());
                            Objects.requireNonNull(chatsMap.get(chat.getUuid())).setLastMsg(chat.getLastMsg());
                        }
                    }
                    for (String c : chatsMap.keySet()) {
                        chats.add(chatsMap.get(c));
                    }
                    adapter = new MsgsAdapter(chats, usersMap);
                    recyclerView.setAdapter(adapter);
                });
    }

    public void searchMessages(String search) {
        List<Chat> filteredChats = new ArrayList<>();

        for (Chat chat : chats) {
            if (chat.getName().toLowerCase(Locale.ROOT).trim().contains(search.toLowerCase(Locale.ROOT))) {
                filteredChats.add(chat);
            }
        }

        adapter = new MsgsAdapter(filteredChats, usersMap);
        recyclerView.setAdapter(adapter);
    }

    public void searchBack() {
        adapter = new MsgsAdapter(chats, usersMap);
        recyclerView.setAdapter(adapter);
    }

    public void verifyConnection() {
        if (!CheckConnection.isNetworkAvailable(context)) {
            activity.runOnUiThread(() -> Toast.makeText(context, "Sem conexão com a internet.",
                    Toast.LENGTH_SHORT).show());
        }
    }
}
