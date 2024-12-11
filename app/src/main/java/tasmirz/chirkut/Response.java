package tasmirz.chirkut;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Response extends AppCompatActivity {

    private TextView respMsgTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        respMsgTextView = findViewById(R.id.resp_msg);

        // Get data from intent
        String message = getIntent().getStringExtra("message");
        String date = getIntent().getStringExtra("date");
        boolean replySent = getIntent().getBooleanExtra("replySent", false);
        Button privateReplyButton = findViewById(R.id.resp_priv);
        int backgroundColor = getIntent().getIntExtra("background", 0);
        findViewById(R.id.response_layout).setBackgroundColor(backgroundColor);
        if (replySent) {
            privateReplyButton.setEnabled(false);
        }
        respMsgTextView.setText(message);
        Button backButton = findViewById(R.id.resp_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Response.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        Button deleteButton = findViewById(R.id.resp_del);
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
                String messageId = getIntent().getStringExtra("messageId");

                if (messageId != null) {
                databaseReference.child(messageId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    Intent intent = new Intent(Response.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    } else {
                    // Handle failure
                    }
                });
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .show();
        });
    }
}