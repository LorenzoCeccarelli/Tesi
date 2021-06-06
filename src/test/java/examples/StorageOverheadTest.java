package examples;

import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.EncryptionError;
import clientsideEncryption.core.token.ClearToken;
import clientsideEncryption.core.token.EncryptedToken;

import javax.crypto.SecretKey;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class StorageOverheadTest {
    public static void main(String[] args){
        try {


            //Create Master Encryption Key
            SecretKey masterKey = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);


            //Ciphertext Token
            //AES128
            SecretKey aes128key = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES128);
            System.out.println("--------- AES128 -------");
            FileWriter csvWriter1 = new FileWriter("aes128.csv");
            computeStatistics(masterKey,aes128key,csvWriter1);
            csvWriter1.flush();

            //AES192
            SecretKey aes192key = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES192);
            System.out.println("--------- AES192 -------");
            FileWriter csvWriter2 = new FileWriter("aes192.csv");
            computeStatistics(masterKey, aes192key,csvWriter2);
            csvWriter2.flush();

            //AES256
            SecretKey aes256key = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
            System.out.println("--------- AES256 -------");
            FileWriter csvWriter3 = new FileWriter("aes256.csv");
            computeStatistics(masterKey, aes256key,csvWriter3);
            csvWriter3.flush();

            //Overall statistics
            FileWriter csvWriter = new FileWriter("overall.csv");
            computeOverheadStatistics(masterKey,aes128key,aes192key,aes256key,csvWriter);
            csvWriter.flush();

            //Plaintext Token
            System.out.println("------ PLAINTEXT -------");
            FileWriter csvWriter4 = new FileWriter("plaintext.csv");
            computeStatistics(csvWriter4);
            csvWriter4.flush();



        } catch (NoSuchAlgorithmException | EncryptionError | IOException keystoreOperationError) {
            keystoreOperationError.printStackTrace();
        }

    }

    public static String generateAlphaNumericalString(int length){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public static void computeOverheadStatistics(SecretKey masterKey, SecretKey aes128key, SecretKey aes192key, SecretKey aes256key, FileWriter fileWriter) throws IOException, EncryptionError {
        fileWriter.append("Dati originali")
                .append(",")
                .append("AES128 Overhead [%]")
                .append(",")
                .append("AES192 Overhead [%]")
                .append(",")
                .append("AES256 Overhead [%]")
                .append("\n");
        byte[] aes128encKey = CryptoUtils.encryptDataWithPrefixIV(masterKey,aes128key.getEncoded());
        byte[] aes192encKey = CryptoUtils.encryptDataWithPrefixIV(masterKey,aes192key.getEncoded());
        byte[] aes256encKey = CryptoUtils.encryptDataWithPrefixIV(masterKey,aes256key.getEncoded());
        String testString;
        for (int i = 0; i<255; i++){
            testString = generateAlphaNumericalString(i + 1);
            byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(aes128key,testString.getBytes());
            String etaes128 = new EncryptedToken(aes128encKey,ciphertext).generateToken();
            String etaes192 = new EncryptedToken(aes192encKey,ciphertext).generateToken();
            String etaes256 = new EncryptedToken(aes256encKey,ciphertext).generateToken();
            fileWriter.append(String.valueOf(testString.length()))
                    .append(",")
                    .append(String.valueOf(Math.round(((double)(etaes128.length() - testString.length())/testString.length())*100)))
                    .append(",")
                    .append(String.valueOf(Math.round(((double)(etaes192.length() - testString.length())/testString.length())*100)))
                    .append(",")
                    .append(String.valueOf(Math.round(((double)(etaes256.length() - testString.length())/testString.length())*100)))
                    .append("\n");


        }
    }

    public static void computeStatistics(SecretKey masterKey, SecretKey encryptionKey, FileWriter fileWriter) throws EncryptionError, IOException {
        fileWriter.append("Dimensione dati")
                .append(",")
                .append("Dimensione dati cifrati (Base64)")
                .append(",")
                .append("Dimensione token")
                .append(",")
                .append("Overhead (%)")
                .append("\n");

        byte[] encryptedKey = CryptoUtils.encryptDataWithPrefixIV(masterKey,encryptionKey.getEncoded());
        String testString;
        for (int i = 0; i < 255; i++) {
            testString = generateAlphaNumericalString(i + 1);
            byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(encryptionKey,testString.getBytes());
            EncryptedToken et = new EncryptedToken(encryptedKey,ciphertext);
            String token = et.generateToken();
            String[] tokens = token.split("\\.");
            fileWriter.append(String.valueOf(testString.length()))
                    .append(",")
                    .append(String.valueOf(tokens[1].length()))
                    .append(",")
                    .append(String.valueOf(token.length()))
                    .append(",")
                    .append(String.valueOf(Math.round(((double)(token.length() - testString.length())/testString.length())*100)))
                    .append("\n");
            System.out.print("Initial length: " + testString.length());
            System.out.print(" Encrypted data length: " + tokens[1].length());
            System.out.print(" Encrypted key length: " + tokens[2].length());
            System.out.print(" Header length: " + tokens[0].length());
            System.out.print(" Key length: " + encryptionKey.getEncoded().length);
            System.out.print(" Token length: " + token.length());
            System.out.print(" Overhead: " + Math.round(((double)(token.length() - testString.length())/testString.length())*100) + " %\n");
        }
    }

    public static void computeStatistics(FileWriter fileWriter) throws IOException {
        fileWriter.append("Dimensione dati")
                .append(",")
                .append("Dimensione dati (Base64)")
                .append(",")
                .append("Dimensione token")
                .append(",")
                .append("Overhead (%)")
                .append("\n");

        String testString;
        for (int i = 0; i < 255; i++) {
            testString = generateAlphaNumericalString(i + 1);
            ClearToken et = new ClearToken(testString);
            String token = et.generateToken();
            String[] tokens = token.split("\\.");
            fileWriter.append(String.valueOf(testString.length()))
                    .append(",")
                    .append(String.valueOf(tokens[1].length()))
                    .append(",")
                    .append(String.valueOf(token.length()))
                    .append(",")
                    .append(String.valueOf(Math.round(((double)(token.length() - testString.length())/testString.length())*100)))
                    .append("\n");
            System.out.print("Initial length: " + testString.length());
            System.out.print(" token data length (BASE64) " + tokens[1].length());
            System.out.print(" Header length (BASE64): " + tokens[0].length());
            System.out.print(" Token length: " + token.length());
            System.out.print(" Overhead: " + Math.round(((double)(token.length() - testString.length())/testString.length())*100) + " %\n");
        }
    }
}
