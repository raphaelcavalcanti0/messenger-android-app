package com.rcfin.messenger.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rcfin.messenger.utils.CheckConnection;
import com.rcfin.messenger.R;
import com.rcfin.messenger.models.User;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText editName;
    ImageView configImage, btn_editImage, btn_editNome;
    Button buttonAlterar, buttonCancel;
    String uuid;
    Bitmap bitmap, bmp;
    Uri selectedImage;
    boolean changedImg = false;
    ProgressBar configProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        toolbar = findViewById(R.id.toolbarPrincipal);
        editName = findViewById(R.id.editName);
        configImage = findViewById(R.id.config_image);
        btn_editImage = findViewById(R.id.btn_editImage);
        btn_editNome = findViewById(R.id.btn_editNome);
        buttonAlterar = findViewById(R.id.buttonAlterar);
        buttonCancel = findViewById(R.id.buttonCancel);
        configProgressbar = findViewById(R.id.configProgressbar);

        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        uuid = getIntent().getExtras().getString("uuid");

        btn_editNome.setOnClickListener(v -> {
            editName.setEnabled(true);
            editName.requestFocus(View.FOCUS_RIGHT);
            editName.setFocusableInTouchMode(true);
            editName.setSelection(editName.getText().length());
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editName, InputMethodManager.SHOW_FORCED);
        });

        btn_editImage.setOnClickListener(v -> openChoosePhoto());
        buttonCancel.setOnClickListener(v -> onBackPressed());
        buttonAlterar.setOnClickListener(v -> {
            buttonAlterar.setEnabled(false);
            if (CheckConnection.isNetworkAvailable(getApplicationContext())) {
                FirebaseFirestore.getInstance().collection("/users")
                        .document(uuid)
                        .update("name", editName.getText().toString().trim())
                        .addOnSuccessListener(unused -> {
                            if (changedImg && bmp != null) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference("/images");

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] data = baos.toByteArray();

                                storageReference.child(uuid)
                                        .putBytes(data).addOnFailureListener(e ->
                                        Toast.makeText(getApplicationContext(), "Não foi possível salvar a foto.", Toast.LENGTH_SHORT).show())
                                        .addOnSuccessListener(taskSnapshot -> storageReference.child(uuid).getDownloadUrl()
                                                .addOnSuccessListener(uri -> FirebaseFirestore.getInstance().collection("users")
                                                        .document(uuid).update("profileUrl", uri.toString())
                                                        .addOnSuccessListener(unused1 -> {
                                                            buttonAlterar.setEnabled(true);
                                                            onBackPressed();
                                                        })));
                            }
                            Toast.makeText(getApplicationContext(), "Alterações salvas.", Toast.LENGTH_SHORT).show();
                            editName.setEnabled(false);
                            changedImg = false;

                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Alterações não foram salvas.", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getApplicationContext(), "Sem conexão com a internet.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        fetchUserData();
    }

    ActivityResultLauncher<Intent> chooseImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedImage = data.getData();
                        try {
                            InputStream ims = getContentResolver().openInputStream(selectedImage);
                            bmp = BitmapFactory.decodeStream(ims);
                            configImage.setImageBitmap(bmp);
                            changedImg = true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void openChoosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        chooseImg.launch(intent);
    }

    private void fetchUserData() {
        if (CheckConnection.isNetworkAvailable(getApplicationContext())) {
            FirebaseFirestore.getInstance()
                    .collection("/users").addSnapshotListener((value, error) -> {
                        if (value != null) {
                            List<DocumentSnapshot> docs = value.getDocuments();
                            for (DocumentSnapshot doc : docs) {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    if (user.getUuid().equals(uuid)) {
                                        editName.setText(user.getName());
                                        fetchUserImg(user.getProfileUrl());
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void fetchUserImg(String url) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            Bitmap foto = fetchImages(url);
            runOnUiThread(() -> {
                configProgressbar.setVisibility(View.GONE);
                configImage.setImageBitmap(foto);
            });
        });
    }

    private Bitmap fetchImages(String url) {
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            verifyAuthentication();
        }
        return super.onOptionsItemSelected(item);
    }

    private void verifyAuthentication() {
        uuid = FirebaseAuth.getInstance().getUid();
        if (uuid == null) {
            Intent intent = new Intent(ConfigActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}