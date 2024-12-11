package tasmirz.chirkut;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import java.security.PrivateKey;
import javax.crypto.Cipher;

public class MessageAdapter extends FirebaseRecyclerAdapter<Message, MessageAdapter.MessageViewHolder> {

    private PrivateKey privateKey;
    private Context context;

    public MessageAdapter(@NonNull FirebaseRecyclerOptions<Message> options, PrivateKey privateKey, Context context) {
        super(options);
        this.privateKey = privateKey;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
        int reversePosition = getItemCount() - 1 - position;
        String messageId = getRef(reversePosition).getKey();
        model.setMessageId(messageId);

        String decryptedMessage = decryptMessage(model.getEncryptedText(), privateKey);
        String decryptedSharedKey = decryptMessage(model.getEncryptedSharedLKey(), privateKey);
        holder.messageText.setText(decryptedMessage);

        long timestamp = model.getTime();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateTime = sdf.format(new java.util.Date(timestamp));
        holder.dateText.setText(dateTime);

        holder.messageText.setOnClickListener(v -> {
            Intent intent = new Intent(context, Response.class);
            intent.putExtra("message", decryptedMessage);
            intent.putExtra("messageId", model.getMessageId());
            intent.putExtra("background", model.getBackgroundSelected());
            intent.putExtra("replySent", model.getReplySent());
            intent.putExtra("sharedKey", decryptedSharedKey);

            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public Message getItem(int position) {
        return super.getItem(getItemCount() - 1 - position);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    private String decryptMessage(String encryptedMessage, PrivateKey privateKey) {
        try {
            return RSADecrypt.decrypt(encryptedMessage, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error decrypting message";
        }
    }
}