package android.security;

public class NetworkSecurityPolicy {
    public static NetworkSecurityPolicy getInstance() {
        return new NetworkSecurityPolicy();
    }

    public boolean isCleartextTrafficPermitted() {
        return true;
    }
}
