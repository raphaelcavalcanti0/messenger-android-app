package com.rcfin.messenger.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rcfin.messenger.utils.CheckConnection;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.ChatAdapter;
import com.rcfin.messenger.models.Message;
import com.rcfin.messenger.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    ChatAdapter adapter;
    RecyclerView recyclerChat;
    EditText txtMsg;
    Button sendBtn;
    User user, me;
    List<Message> lista = new ArrayList<>();
    String fromId, uuid;
    Handler handler = new Handler();
    Toolbar toolbar;
    ImageView chat_titleImg;
    Bitmap bitmap;
    TextView chat_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        user = getIntent().getExtras().getParcelable("user");

        fromId = FirebaseAuth.getInstance().getUid();

        txtMsg = findViewById(R.id.txt_msg);
        sendBtn = findViewById(R.id.btn_enviarMsg);
        chat_titleImg = findViewById(R.id.chat_titleImg);
        chat_title = findViewById(R.id.chat_title);

        if (user.getName() != null) {
            chat_title.setText(user.getName());
        }

        if (user.getBitmap() != null) {
            chat_titleImg.setImageBitmap(user.getBitmap());
        } else {
            chat_titleImg.setImageBitmap(fetchImages(user.getProfileUrl(), user));
        }

        recyclerChat = findViewById(R.id.recyclerChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerChat.setLayoutManager(linearLayoutManager);

        FirebaseFirestore.getInstance().collection("/users")
                .document(fromId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    me = documentSnapshot.toObject(User.class);
                    fetchMessages();
                });

        sendBtn.setOnClickListener(v -> sendMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_config:
                Intent intent = new Intent(ChatActivity.this, ConfigActivity.class);
                intent.putExtra("uuid", me.getUuid());
                startActivity(intent);
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                verifyAuthentication();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchMessages() {
        if (me != null) {
            String fromId = me.getUuid();
            String toId = user.getUuid();
            FirebaseFirestore.getInstance()
                    .collection("/conversations")
                    .document(fromId)
                    .collection(toId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (value != null) {
                            Thread t1 = new Thread(() -> {
                                List<DocumentChange> documentChanges = value.getDocumentChanges();
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        lista.add(message);
                                    }
                                }
                            });
                            handler.post(() -> {
                                t1.start();
                                try {
                                    t1.join();
                                    adapter = new ChatAdapter(lista, fromId);
                                    recyclerChat.setAdapter(adapter);
                                    if (adapter.getItemCount() > 1) {
                                        recyclerChat.smoothScrollToPosition(adapter.getItemCount() - 1);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
        }
    }

    private void sendMessage() {
        sendBtn.setEnabled(false);
        if (CheckConnection.isNetworkAvailable(getApplicationContext())) {
            if (!txtMsg.getText().toString().isEmpty()) {
                if (fromId != null) {
                    Message message = new Message();
                    message.setText(txtMsg.getText().toString());
                    message.setTimestamp(System.currentTimeMillis());
                    message.setFromId(FirebaseAuth.getInstance().getUid());
                    message.setToId(user.uuid);
                    FirebaseFirestore.getInstance().collection("/conversations")
                            .document(fromId)
                            .collection(user.uuid)
                            .add(message).addOnSuccessListener(documentReference -> {})
                            .addOnFailureListener(e -> {});

                    FirebaseFirestore.getInstance().collection("/conversations")
                            .document(user.uuid)
                            .collection(fromId)
                            .add(message).addOnSuccessListener(documentReference -> txtMsg.setText(null))
                            .addOnFailureListener(e -> {});
                    sendBtn.setEnabled(true);
                }
            } else {
                Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar!",
                        Toast.LENGTH_SHORT).show();
                sendBtn.setEnabled(true);
            }
        } else {
            Toast.makeText(ChatActivity.this, "Sem conexão com a internet.",
                    Toast.LENGTH_SHORT).show();
            sendBtn.setEnabled(true);
        }
    }

    private void verifyAuthentication() {
        uuid = FirebaseAuth.getInstance().getUid();
        if (uuid == null) {
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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