package tasmirz.chirkut;

public class UserProfile {
    public String userId;
    public String username;
    public String image;
    public String publicKey;

    public UserProfile(String userId, String username, String image, String publicKey) {
        this.userId = userId;
        this.username = username;
        this.image = image;
        this.publicKey = publicKey;
    }
}