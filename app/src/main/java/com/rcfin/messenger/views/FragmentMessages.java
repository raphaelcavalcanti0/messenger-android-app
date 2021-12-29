package com.rcfin.messenger.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rcfin.messenger.utils.LoadMessagesTask;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.MsgsAdapter;
import com.rcfin.messenger.models.Chat;
import com.rcfin.messenger.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FragmentMessages extends Fragment {

    RecyclerView recyclerMsgs;
    MsgsAdapter adapter;
    String uuid;
    List<Chat> chats = new ArrayList<>();
    HashMap<String, User> usersMap = new HashMap<>();
    LoadMessagesTask loadMessagesTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerMsgs = view.findViewById(R.id.recyclerMsgs);
        recyclerMsgs.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MsgsAdapter(chats, usersMap);
        recyclerMsgs.setAdapter(adapter);

        return view;
    }

    private void verifyAuthentication() {
        uuid = FirebaseAuth.getInstance().getUid();
        if (uuid == null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        verifyAuthentication();
        loadMessagesTask = new LoadMessagesTask(requireActivity(),
                adapter, recyclerMsgs, uuid);
        loadMessagesTask.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessagesTask.searchBack();
    }

    public void searchMessages(String search) {
        loadMessagesTask.searchMessages(search);
    }
}