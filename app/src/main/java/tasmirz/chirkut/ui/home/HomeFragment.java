package tasmirz.chirkut.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.*;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import android.util.Base64;
import tasmirz.chirkut.Message;
import tasmirz.chirkut.MessageAdapter;
import tasmirz.chirkut.R;
import tasmirz.chirkut.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    private String currentUsername;
    private PrivateKey privateKey;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.home_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get current username and private key from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");
        String privateKeyString = sharedPreferences.getString("PrivateKey", "");

        // Convert the private key string to PrivateKey object
        privateKey = getPrivateKeyFromString(privateKeyString);

        // Query to get messages where to_usr equals currentUsername
        messagesRef = FirebaseDatabase.getInstance().getReference().child("messages");
        Query query = messagesRef.orderByChild("to_usr").equalTo(currentUsername);

        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();

        messageAdapter = new MessageAdapter(options, privateKey, getContext());
        recyclerView.setAdapter(messageAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        messageAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        messageAdapter.stopListening();
    }

    private PrivateKey getPrivateKeyFromString(String key) {
        try {
            byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}