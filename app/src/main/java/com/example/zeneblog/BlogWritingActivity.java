package com.example.zeneblog;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BlogWritingActivity extends AppCompatActivity {

    private static final String LOG_TAG = "BlogWritingActivity";

    private EditText editTextPostTitle;
    private EditText editTextPostDescription;
    private Button buttonSavePost;
    private Button buttonBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference blogPostsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_writing);

        editTextPostTitle = findViewById(R.id.editTextPostTitle);
        editTextPostDescription = findViewById(R.id.editTextPostDescription);
        buttonSavePost = findViewById(R.id.buttonSavePost);
        buttonBack = findViewById(R.id.backButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "A bejegyzés mentéséhez be kell jelentkezni!", Toast.LENGTH_LONG).show();
            Log.w(LOG_TAG, "Nincs bejelentkezett felhasználó, a tevékenység bezárul.");
            finish();
            return;
        }

        blogPostsRef = db.collection("blogposts");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Új bejegyzés írása");
        }

        buttonSavePost.setOnClickListener(v -> savePostToFirestore());
        buttonBack.setOnClickListener(v -> finish());
    }

    private void savePostToFirestore() {
        String title = editTextPostTitle.getText().toString().trim();
        String description = editTextPostDescription.getText().toString().trim();

        if (currentUser == null) {
            Toast.makeText(this, "Hiba: Nincs bejelentkezett felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        if (TextUtils.isEmpty(title)) {
            editTextPostTitle.setError("A cím megadása kötelező!");
            editTextPostTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            editTextPostDescription.setError("A tartalom megadása kötelező!");
            editTextPostDescription.requestFocus();
            return;
        }

        BlogPost newPost = new BlogPost(title, description, userId);

        blogPostsRef.add(newPost)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(LOG_TAG, "Bejegyzés sikeresen mentve Firestore-ba, ID: " + documentReference.getId());
                        Toast.makeText(BlogWritingActivity.this, "Bejegyzés mentve!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Hiba a bejegyzés mentésekor", e);
                        Toast.makeText(BlogWritingActivity.this, "Hiba a mentés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}