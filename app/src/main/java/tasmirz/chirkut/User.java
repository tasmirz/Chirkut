package tasmirz.chirkut;

public class User {
    public String userId;
    public String username;
    public String encryptedPrivateKey;

    public User(String userId, String username, String encryptedPrivateKey) {
        this.userId = userId;
        this.username = username;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}