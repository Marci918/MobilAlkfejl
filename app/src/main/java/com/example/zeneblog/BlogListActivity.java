package com.example.zeneblog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlogListActivity extends AppCompatActivity {
    private static final String LOG_TAG = BlogListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private RecyclerView blogRecyclerView;
    private FloatingActionButton addPostFab;
    private List<BlogPost> blogPostsList;
    private BlogAdapter adapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mBlogPostsCollectionRef;
    private ListenerRegistration mFirestoreListener;

    private static final int ADD_POST_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_list);

        blogRecyclerView = findViewById(R.id.recyclerView);
        addPostFab = findViewById(R.id.addPostFab);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.blink);
        addPostFab.startAnimation(slideIn);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            Log.d(LOG_TAG, "Nincs hitelesített felhasználó! Átirányítás a bejelentkezéshez.");

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        Log.d(LOG_TAG, "Hitelesített felhasználó: " + user.getEmail());

        mFirestore = FirebaseFirestore.getInstance();
        mBlogPostsCollectionRef = mFirestore.collection("blogposts");

        blogPostsList = new ArrayList<>();
        adapter = new BlogAdapter(this, blogPostsList, post -> {
            Intent intent = new Intent(BlogListActivity.this, BlogReadingActivity.class);
            intent.putExtra(BlogReadingActivity.EXTRA_TITLE, post.getTitle());
            intent.putExtra(BlogReadingActivity.EXTRA_DESCRIPTION, post.getDescription());

            startActivity(intent);
        });

        blogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        blogRecyclerView.setAdapter(adapter);

        addPostFab.setOnClickListener(v -> {
            Intent intent = new Intent(BlogListActivity.this, BlogWritingActivity.class);
            startActivityForResult(intent, ADD_POST_REQUEST);
        });
    }

    private void setupFirestoreListener() {
        if (user == null) return;

        mFirestoreListener = mBlogPostsCollectionRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(LOG_TAG, "Firestore listener hiba.", e);
                            Toast.makeText(BlogListActivity.this, "Hiba a bejegyzések betöltésekor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (snapshots == null) {
                            Log.w(LOG_TAG, "Firestore snapshot null volt.");
                            return;
                        }

                        Log.d(LOG_TAG, "Firestore snapshot esemény. Dokumentumváltozások száma: " + snapshots.getDocumentChanges().size());

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            BlogPost post = dc.getDocument().toObject(BlogPost.class);
                            post.setId(dc.getDocument().getId());

                            int index = findPostIndexById(post.getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(LOG_TAG, "Új bejegyzés hozzáadva Firestore-ból: " + post.getTitle());
                                    if (index == -1) {
                                        blogPostsList.add(post);
                                    }
                                    break;
                                case MODIFIED:
                                    Log.d(LOG_TAG, "Bejegyzés módosítva Firestore-ból: " + post.getTitle());
                                    if (index != -1) {
                                        blogPostsList.set(index, post);
                                    }
                                    break;
                                case REMOVED:
                                    Log.d(LOG_TAG, "Bejegyzés eltávolítva Firestore-ból: " + post.getTitle());
                                    if (index != -1) {
                                        blogPostsList.remove(index);
                                    }
                                    break;
                            }
                        }

                        Collections.sort(blogPostsList, (p1, p2) -> {
                            if (p1.getTimestamp() == null && p2.getTimestamp() == null) return 0;
                            if (p1.getTimestamp() == null) return 1;
                            if (p2.getTimestamp() == null) return -1;
                            return p2.getTimestamp().compareTo(p1.getTimestamp());
                        });

                        adapter.notifyDataSetChanged();

                        if (blogPostsList.isEmpty()) {
                            Log.d(LOG_TAG, "A blogbejegyzések listája üres a frissítés után.");
                        }
                    }
                });
    }

    private int findPostIndexById(String postId) {
        for (int i = 0; i < blogPostsList.size(); i++) {
            if (blogPostsList.get(i).getId().equals(postId)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_POST_REQUEST && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Új bejegyzés sikeresen elmentve!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            Log.d(LOG_TAG, "Firestore listener beállítása onStart-ban.");
            setupFirestoreListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFirestoreListener != null) {
            Log.d(LOG_TAG, "Firestore listener eltávolítása onStop-ban.");
            mFirestoreListener.remove();
            mFirestoreListener = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blog_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        Log.d(LOG_TAG, "Kijelentkezés...");
        mAuth.signOut();
        Intent intent = new Intent(BlogListActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sikeresen kijelentkezett.", Toast.LENGTH_SHORT).show();
    }
}