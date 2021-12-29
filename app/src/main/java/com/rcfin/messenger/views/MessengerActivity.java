package com.rcfin.messenger.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.rcfin.messenger.R;
import com.rcfin.messenger.adapters.ViewPagerAdapter;

public class MessengerActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    Toolbar toolbar;
    FragmentStateAdapter adapter;
    String uuid;
    FragmentManager fm;
    SearchView searchView;
    FragmentMessages fragmentMessages;
    FragmentContacts fragmentContacts;
    FragmentCalls fragmentCalls;
    ProgressBar progressBar1, progressBar2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        toolbar = findViewById(R.id.toolbarPrincipal);
        progressBar1 = findViewById(R.id.loadingMsgs);
        progressBar2 = findViewById(R.id.loadingContacts);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings
                .Builder().setPersistenceEnabled(true).build();
        db.setFirestoreSettings(settings);

        toolbar.setTitle("Messenger");
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Conversas"));
        tabLayout.addTab(tabLayout.newTab().setText("Contatos"));
        tabLayout.addTab(tabLayout.newTab().setText("Chamadas"));

        verifyAuthentication();

        fm = getSupportFragmentManager();

        adapter = new ViewPagerAdapter(fm, getLifecycle());

        viewPager.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    searchView.setPressed(false);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Procurar...");
        searchView.onActionViewCollapsed();

        searchView.setOnCloseListener(() -> {
            switch (viewPager.getCurrentItem()) {
                case 0:
                    fragmentMessages = (FragmentMessages) fm.findFragmentByTag("f0");
                    if (fragmentMessages != null) {
                        fragmentMessages.loadMessagesTask.searchBack();
                    }
                    break;
                case 1:
                    fragmentContacts = (FragmentContacts) fm.findFragmentByTag("f1");
                    if (fragmentContacts != null) {
                        fragmentContacts.loadContactsTask.searchBack();
                    }
                    break;
                case 2:
                    fragmentCalls = (FragmentCalls) fm.findFragmentByTag("f2");
                    break;
            }
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    switch (viewPager.getCurrentItem()) {
                        case 0:
                            fragmentMessages = (FragmentMessages) fm.findFragmentByTag("f0");
                            if (fragmentMessages != null) {
                                fragmentMessages.searchMessages(newText);
                            }
                            break;
                        case 1:
                            fragmentContacts = (FragmentContacts) fm.findFragmentByTag("f1");
                            if (fragmentContacts != null) {
                                fragmentContacts.searchContacts(newText);
                            }
                            break;
                        case 2:
                            fragmentCalls = (FragmentCalls) fm.findFragmentByTag("f2");
                            break;
                    }
                } else {
                    // close search
                    searchView.setIconified(true);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_config:
                Intent intent = new Intent(MessengerActivity.this, ConfigActivity.class);
                intent.putExtra("uuid", uuid);
                startActivity(intent);
                break;
            case R.id.menu_logout:
                FirebaseAuth.getInstance().signOut();
                verifyAuthentication();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verifyAuthentication() {
        uuid = FirebaseAuth.getInstance().getUid();
        if (uuid == null) {
            Intent intent = new Intent(MessengerActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}