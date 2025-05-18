package com.example.zeneblog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class BlogReadingActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "com.example.zeneblog.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "com.example.zeneblog.EXTRA_DESCRIPTION";

    private TextView titleTextView;
    private TextView descriptionTextView;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blog_reading);

        titleTextView = findViewById(R.id.detailTitleTextView);
        descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        buttonBack = findViewById(R.id.backButton);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_TITLE)) {
                titleTextView.setText(intent.getStringExtra(EXTRA_TITLE));
            }
            if (intent.hasExtra(EXTRA_DESCRIPTION)) {
                descriptionTextView.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
            }
        }
        buttonBack.setOnClickListener(v -> finish());
    }
}
