package tasmirz.chirkut.ui.notifications;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import tasmirz.chirkut.LoginActivity;
import tasmirz.chirkut.PrivacyPolicyActivity;
import tasmirz.chirkut.R;
import tasmirz.chirkut.TermsOfServiceActivity;
import tasmirz.chirkut.WhitepaperActivity;
import tasmirz.chirkut.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView pfp;
    private Uri imageUri;
    private FragmentNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        root.findViewById(R.id.btn_signout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        String userImage = sharedPreferences.getString("userImage", "");

        ((TextView) root.findViewById(R.id.txt_usr)).setText(username);

        pfp = root.findViewById(R.id.pfp);
        byte[] decodedString = Base64.decode(userImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        pfp.setImageBitmap(decodedByte);

        pfp.setOnClickListener(v -> openImagePicker());
        root.findViewById(R.id.privacy_text3).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        root.findViewById(R.id.terms_text3).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TermsOfServiceActivity.class);
            startActivity(intent);
        });

        root.findViewById(R.id.whitepaper_text3).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WhitepaperActivity.class);
            startActivity(intent);
        });

        return root;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                pfp.setImageBitmap(bitmap);
                uploadImageToFirebase(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        String userId = sharedPreferences.getString("userId", "");
        DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference("userProfiles").child(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        userProfileRef.child("image").setValue(imageString).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
            Toast.makeText(getActivity(), "Profile image updated", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("userImage", imageString);
            editor.apply();
            } else {
            Toast.makeText(getActivity(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


