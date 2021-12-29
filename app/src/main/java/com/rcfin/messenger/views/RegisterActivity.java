package com.rcfin.messenger.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rcfin.messenger.utils.CheckConnection;
import com.rcfin.messenger.R;
import com.rcfin.messenger.models.User;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    EditText textName;
    EditText textEmail;
    EditText textPass;
    Button btn_register;
    ImageView imageButton;
    TextView textFoto;
    Uri selectedImage;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textName = findViewById(R.id.registerName);
        textEmail = findViewById(R.id.registerEmail);
        textPass = findViewById(R.id.registerPassword);
        imageButton = findViewById(R.id.register_image);
        btn_register = findViewById(R.id.btn_register);
        textFoto = findViewById(R.id.textFoto);

        btn_register.setOnClickListener(v -> {
            String name = textName.getText().toString().trim();
            String email = textEmail.getText().toString().trim();
            String pass = textPass.getText().toString().trim();

            if (name.equals("")) {
                Toast.makeText(this, "Digite um nome!", Toast.LENGTH_SHORT).show();
            } else {
                if (email.equals("")) {
                    Toast.makeText(this, "Digite um email!", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals("")) {
                        Toast.makeText(this, "Digite uma senha!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (CheckConnection.isNetworkAvailable(getApplicationContext())) {
                            btn_register.setEnabled(false);
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            saveUserInFirebase();
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }).addOnFailureListener(e -> {
                                if (e.toString().contains("at least 6")) {
                                    Toast.makeText(this, "A senha deve ter ao menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                                }
                                btn_register.setEnabled(true);
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Sem conexão com a internet.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        imageButton.setOnClickListener(v -> openChoosePhoto());
    }

    private void saveUserInFirebase() {
        String filename = FirebaseAuth.getInstance().getUid();
        StorageReference firebaseStorage = FirebaseStorage.getInstance()
                .getReference("/images/" + filename);

        if (bmp != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            firebaseStorage.putBytes(data)
                    .addOnFailureListener(e -> {})
                    .addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot.getTask().isSuccessful()) {
                            firebaseStorage.getDownloadUrl().addOnSuccessListener(uri -> {
                                String profileUrl = uri.toString();
                                String uid = FirebaseAuth.getInstance().getUid();
                                String username = textName.getText().toString().trim();
                                String useremail = textEmail.getText().toString().trim();

                                User user = new User(uid, username, useremail, profileUrl);

                                assert uid != null;
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(documentReference -> {})
                                        .addOnFailureListener(e -> {});
                            });
                        }

                    });
        }
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
                            imageButton.setImageBitmap(bmp);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        /*
                        try {
                            InputStream ims = getContentResolver().openInputStream(selectedImage);
                            Bitmap bmp = BitmapFactory.decodeStream(ims);

                            Bitmap crop_bmp;
                            if (bmp.getWidth() >= bmp.getHeight()) {
                                crop_bmp = Bitmap.createBitmap(bmp,
                                        bmp.getWidth()/2 - bmp.getHeight()/2,
                                        0,
                                        bmp.getHeight(), bmp.getHeight()
                                );

                            } else {
                                crop_bmp = Bitmap.createBitmap(bmp,
                                        0,
                                        bmp.getHeight()/2 - bmp.getWidth()/2,
                                        bmp.getWidth(), bmp.getWidth()
                                );
                            }

                            crop_bmp = getRoundedBitmap(crop_bmp);
                            imageButton.setImageBitmap(crop_bmp);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        */
                        textFoto.setText("");
                    }
                }
            });

    private void openChoosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        chooseImg.launch(intent);
    }

    public Bitmap getRoundedBitmap(@NonNull Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle((float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2,
                (float) bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}