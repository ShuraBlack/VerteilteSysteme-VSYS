package aqua.common.endpoint;

import messaging.Endpoint;
import messaging.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static aqua.common.endpoint.SerializableUtil.payloadFromByteArray;
import static aqua.common.endpoint.SerializableUtil.payloadToByteArray;

public class SecureEndpointSymmetric extends Endpoint {

    private static final String KEY_MAT = "CAFEBABECAFEBABE";
    private static final String CRYPTMODE = "AES";
    private Cipher encrypt;
    private Cipher decrypt;

    private final Endpoint endpoint;

    public SecureEndpointSymmetric() {
        this.endpoint = new Endpoint();
        buildKey();
    }

    public SecureEndpointSymmetric(int port) {
        this.endpoint = new Endpoint(port);
        buildKey();
    }

    private void buildKey() {
        SecretKeySpec spec = new SecretKeySpec(KEY_MAT.getBytes(StandardCharsets.UTF_8), CRYPTMODE);
        try {
            this.encrypt = Cipher.getInstance(CRYPTMODE);
            this.encrypt.init(Cipher.ENCRYPT_MODE, spec);
            this.decrypt = Cipher.getInstance(CRYPTMODE);
            this.decrypt.init(Cipher.DECRYPT_MODE, spec);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(InetSocketAddress receiver, Serializable payload) {
        byte[] encrypted = null;
        try {
            encrypted = this.encrypt.doFinal(payloadToByteArray(payload));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException e) {
            e.printStackTrace();
        }
        if (encrypted == null) {
            return;
        }
        this.endpoint.send(receiver, encrypted);
    }

    @Override
    public Message blockingReceive() {
        final Message msg = this.endpoint.blockingReceive();

        Serializable payload = null;
        try {
            byte[] decrypted = this.decrypt.doFinal((byte[]) msg.getPayload());
            if (decrypted == null) {
                return null;
            }
            payload = payloadFromByteArray(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Message(payload, msg.getSender());
    }
}
