package com.rcfin.messenger.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rcfin.messenger.utils.LoadContactsTask;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.ContactsAdapter;
import com.rcfin.messenger.models.User;

import java.util.ArrayList;
import java.util.List;


public class FragmentContacts extends Fragment {

    RecyclerView recyclerContacts;
    ContactsAdapter adapter;
    String uuid;
    List<User> users = new ArrayList<>();
    LoadContactsTask loadContactsTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerContacts = view.findViewById(R.id.recyclerContacts);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        uuid = FirebaseAuth.getInstance().getUid();

        adapter = new ContactsAdapter(users, uuid);
        recyclerContacts.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadContactsTask = new LoadContactsTask(requireActivity(),
                adapter, recyclerContacts, uuid);
        loadContactsTask.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContactsTask.searchBack();
    }

    public void searchContacts(String search) {
        loadContactsTask.searchContacts(search);
    }

}