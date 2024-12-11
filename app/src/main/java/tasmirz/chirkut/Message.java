package tasmirz.chirkut;

public class Message {
    private String messageId;
    private String encryptedText;
    private String encryptedSharedLKey;
    private String to_usr;
    private int backgroundSelected;
    private long Time; 
    private boolean replySent;

    // Default constructor required for Firebase
    public Message() {
    }

    public Message(String messageId, String encryptedContent, String encryptedSharedLKey, String to_usr, String date, long timestamp) {
        this.messageId = messageId;
        this.encryptedText = encryptedContent;
        this.encryptedSharedLKey = encryptedSharedLKey;
        this.to_usr = to_usr;
        this.Time = timestamp;

    }
    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId){
        this.messageId = messageId;
    }

    public String getEncryptedText() {
        return encryptedText;
    }

    public void setEncryptedText(String encryptedText){
        this.encryptedText = encryptedText;
    }

    public String getEncryptedSharedLKey() {
        return encryptedSharedLKey;
    }

    public void setEncryptedSharedLKey(String encryptedSharedLKey){
        this.encryptedSharedLKey = encryptedSharedLKey;
    }

    public String getTo_usr() {
        return to_usr;
    }

    public void setTo_usr(String to_usr){
        this.to_usr = to_usr;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long timestamp){
        this.Time=timestamp;
    }
    public int getBackgroundSelected() {
        return backgroundSelected;
    }

    public void setBackgroundSelected(int backgroundSelected) {
        this.backgroundSelected = backgroundSelected;
    }
    public boolean getReplySent() {
        return replySent;
    }

    public void setReplySent(boolean replySent) {
        this.replySent = replySent;
    }

}