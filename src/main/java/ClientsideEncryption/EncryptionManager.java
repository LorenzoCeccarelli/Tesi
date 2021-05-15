package ClientsideEncryption;

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

enum Algorithm{
    AES128,
    AES192,
    AES256
}
public class EncryptionManager {

    public EncryptionManager(){}

    public SecretKey createSymKey(Algorithm alg) throws NoSuchAlgorithmException {
        ImmutablePair<String,Integer> ip = algorithmInfo(alg);
        SecureRandom sr = new SecureRandom();
        KeyGenerator kg = KeyGenerator.getInstance(ip.left);
        kg.init(ip.right,sr);
        return kg.generateKey();
    }

    private ImmutablePair<String, Integer> algorithmInfo(Algorithm al) throws NoSuchAlgorithmException {
        switch (al){
            case AES128: return ImmutablePair.of("AES",128);
            case AES192: return ImmutablePair.of("AES",192);
            case AES256: return ImmutablePair.of("AES",256);
            default: throw new NoSuchAlgorithmException("Algorithm not supported");
        }
    }


}
