package tasmirz.chirkut.ui.send_msg;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import tasmirz.chirkut.R;
import tasmirz.chirkut.RSAEncrypt;

public class MsgSendFragment extends Fragment {
    int backgroundSelected = 1;
    String toSendUser,toSendUsrPub;
    private EditText editText;
    private ChipGroup chipGroup;
    private Chip chip1, chip2, chip3, chip4;
    ProgressDialog progressDialog;
    boolean userFound =false;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_msgsend, container, false);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        editText = root.findViewById(R.id.text_dashboard);
        EditText msgUser = root.findViewById(R.id.msg_user);
        Button msgSel1 = root.findViewById(R.id.msg_sel_1);
        Button msgSel2 = root.findViewById(R.id.msg_sel_2);
        Button msgSel3 = root.findViewById(R.id.msg_sel_3);
        Button msgSel4 = root.findViewById(R.id.msg_sel_4);
        ImageView msgAvatar = root.findViewById(R.id.msg_avatar);
        Button msgSend = root.findViewById(R.id.msg_send);
        msgSend.setEnabled(false);
        msgUser.setOnFocusChangeListener((v, hasFocus) -> {
        progressDialog.show();
            if (!hasFocus) {
                String username = msgUser.getText().toString().trim();
                if (!username.isEmpty()) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userProfiles");
                    userRef.orderByChild("username").equalTo(username).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                String publicKey = snapshot.child("publicKey").getValue(String.class);
                                String avatarUrl = snapshot.child("image").getValue(String.class);
                                if (publicKey != null) {
                                    toSendUsrPub = publicKey;
                                    toSendUser = username;
                                    // Update avatar
                                    if (avatarUrl != null) {
                                        byte[] decodedString = Base64.decode(avatarUrl, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        msgAvatar.setImageBitmap(decodedByte);
                                        progressDialog.dismiss();
                                        userFound = true;
                                        msgSend.setEnabled(true);
                                    }
                                    progressDialog.dismiss();
                                    return;
                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            userFound = false;
                            msgSend.setEnabled(false);
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    userFound = false;
                    msgSend.setEnabled(false);
                    progressDialog.dismiss();
                }
            } else {
                userFound = false;
                msgSend.setEnabled(false);
                progressDialog.dismiss();
            }
        });
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.msg_sel_1) {
                editText.setBackgroundResource(R.drawable.background_image1);
                backgroundSelected=1;
            } else if (id == R.id.msg_sel_2) {
                editText.setBackgroundResource(R.drawable.background_image2);
                backgroundSelected=2;
            } else if (id == R.id.msg_sel_3) {
                editText.setBackgroundResource(R.drawable.background_image3);
                backgroundSelected=3;
            } else if (id == R.id.msg_sel_4) {
                editText.setBackgroundResource(R.drawable.background_image4);
                backgroundSelected=4;
            }
        }
    };

    msgSel1.setOnClickListener(listener);
    msgSel2.setOnClickListener(listener);
    msgSel3.setOnClickListener(listener);
    msgSel4.setOnClickListener(listener);


    msgSend.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = editText.getText().toString().trim();
            if (message.isEmpty()) {
                editText.setError("Message cannot be empty");
            } else {
                new AlertDialog.Builder(getContext())
                    .setTitle("Private Response")
                    .setMessage("Do you want to receive a private response?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Code to insert data into the real-time database
                            String username =toSendUser;
                            String encryptedMessage = null;
                            try {
                                PublicKey key = getPublicKeyFromString(toSendUsrPub);
                                encryptedMessage = RSAEncrypt.encrypt(message, key);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Encryption error", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String encryptedSharedKey = null;
                            SecureRandom secureRandom = new SecureRandom();
                            byte[] sharedKey = new byte[32]; // 256 bits
                            secureRandom.nextBytes(sharedKey);
                            String sharedKeyString = Base64.encodeToString(sharedKey, Base64.DEFAULT);
                            try {
                                PublicKey key = getPublicKeyFromString(toSendUsrPub);
                                encryptedSharedKey = RSAEncrypt.encrypt(sharedKeyString, key);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Encryption error", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
                            String messageId = databaseReference.push().getKey();
                            Map<String, Object> messageData = new HashMap<>();
                            messageData.put("to_usr", username);
                            messageData.put("encryptedText", encryptedMessage);
                            messageData.put("encryptedSharedLKey", encryptedSharedKey);
                            messageData.put("Time", System.currentTimeMillis());
                            messageData.put("backgroundSelected", backgroundSelected);
                            messageData.put("replySent", false);

                            if (messageId != null) {
                                databaseReference.child(messageId).setValue(messageData)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                                            editText.setText("");
                                        } else {
                                            Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle the negative button action
                        }
                    })
                    .show();
            }
        }
    });
        return root;
    }

    
    public PublicKey getPublicKeyFromString(String publicKey) {
            try {
                byte[] keyBytes = Base64.decode(publicKey, Base64.DEFAULT);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(spec);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
