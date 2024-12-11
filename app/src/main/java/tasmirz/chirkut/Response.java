package tasmirz.chirkut;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import java.io.ByteArrayOutputStream;

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
        int background = getIntent().getIntExtra("background", 0);

        switch (background) {
            case 1:
                respMsgTextView.setBackgroundResource(R.drawable.background_image1);
                break;
            case 2:
                respMsgTextView.setBackgroundResource(R.drawable.background_image2);
                break;
            case 3:
                respMsgTextView.setBackgroundResource(R.drawable.background_image3);
                break;
            case 4:
                respMsgTextView.setBackgroundResource(R.drawable.background_image4);
                break;
            default:
                respMsgTextView.setBackgroundResource(R.drawable.background_image1);
                break;
        }
        
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
        Button shareButton = findViewById(R.id.resp_share);
        shareButton.setOnClickListener(v -> {
            View captureView = findViewById(R.id.capture);
            Bitmap bitmap = Bitmap.createBitmap(captureView.getWidth(), captureView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            captureView.draw(canvas);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            Uri imageUri = Uri.parse(path);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
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