package tasmirz.chirkut;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    String encryptedPrivateKey;
    String decryptedPrivateKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); 
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button loginButton = findViewById(R.id.login_button); 
        findViewById(R.id.privacy_text).setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.terms_text).setOnClickListener(v -> {
            Intent intent = new Intent(this, TermsOfServiceActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.whitepaper_text2).setOnClickListener(v -> {
            Intent intent = new Intent(this, WhitepaperActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        TextView signUpLink = findViewById(R.id.signup_link);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                //Toast.makeText(LoginActivity.this, "Redirecting to Signup Activity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Toast.makeText(this, "firebaseAuthWithGoogle:" + acct.getId(), Toast.LENGTH_SHORT).show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserInDatabase(user);
                        } else {
                            updateUI(null);
                        }
                    }
                });
    }

    private void checkUserInDatabase(FirebaseUser user) {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
    databaseReference.get().addOnCompleteListener(task -> {
        if (task.isSuccessful() && task.getResult().exists()) {
            // Show a dialog to ask for password
            encryptedPrivateKey = task.getResult().child("encryptedPrivateKey").getValue(String.class);
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Enter Password");

            final EditText input = new EditText(LoginActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String password = input.getText().toString();

                    try {
                        byte[] keyBytes = new byte[16];
                        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
                        System.arraycopy(passwordBytes, 0, keyBytes, 0, Math.min(passwordBytes.length, keyBytes.length));
                        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

                        byte[] encryptedPrivateKeyBytes = Base64.getDecoder().decode(encryptedPrivateKey);
                        byte[] decryptedPrivateKeyBytes = cipher.doFinal(encryptedPrivateKeyBytes);

                        // Reconstruct the PrivateKey object
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decryptedPrivateKeyBytes);
                        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

                        // Optionally, encode the decrypted private key as a string
                        String decryptedPrivateKeyString = Base64.getEncoder().encodeToString(decryptedPrivateKeyBytes);

                        // Save to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", task.getResult().child("username").getValue(String.class));
                        editor.putString("PrivateKey", decryptedPrivateKeyString);
                        // Load image from userProfiles
                        DatabaseReference userProfilesRef = FirebaseDatabase.getInstance().getReference("userProfiles").child(user.getUid());
                        userProfilesRef.get().addOnCompleteListener(profileTask -> {
                            if (profileTask.isSuccessful() && profileTask.getResult().exists()) {
                                String userImage = profileTask.getResult().child("image").getValue(String.class);
                                editor.putString("userImage", userImage);
                            }
                            editor.apply();
                            updateUI(user);
                        });
                        return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Failed to decrypt private key.", Toast.LENGTH_SHORT).show();
                                signOut();
                            }

                    
                }
            });
            

            builder.show();
//            databaseReference.get().addOnCompleteListener(task -> {
//                if (task.isSuccessful() && task.getResult().exists()) {
//                    String username = task.getResult().child("username").getValue(String.class);
//                    String encryptedPrivateKey = task.getResult().child("encryptedPrivateKey").getValue(String.class);
//                    String userImage = task.getResult().child("image").getValue(String.class);
//                     String decryptedPrivateKey;
//                     try {
//                        decryptedPrivateKey = decryptPrivateKey(encryptedPrivateKey, password);
//                        editor.putString("decryptedPrivateKey", decryptedPrivateKey);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(LoginActivity.this, "Failed to decrypt private key.", Toast.LENGTH_SHORT).show();
//                        signOut();
//                    }
//                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("username", username);
//
//                    editor.putString("PrivateKey", decryptedPrivateKey);
//                    editor.putString("userImage", userImage);
//                    editor.apply();
//                }
//            });
        } else {
            mAuth.signOut();
            Toast.makeText(LoginActivity.this, "You are not signed up. Please sign up first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    });
}
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }
        private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {

        });
    }
}
