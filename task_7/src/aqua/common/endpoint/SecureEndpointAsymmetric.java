package aqua.common.endpoint;

import aqua.common.msgtypes.KeyExchangeMessage;
import messaging.Endpoint;
import messaging.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.*;

import static aqua.common.endpoint.SerializableUtil.payloadFromByteArray;
import static aqua.common.endpoint.SerializableUtil.payloadToByteArray;

public class SecureEndpointAsymmetric extends Endpoint {

    private static final String CRYPTMODE = "RSA";

    private final Map<InetSocketAddress, PublicKey> keys = new HashMap<>();
    private final List<PendingMessage> messages = new LinkedList<>();

    private PublicKey publicKey;

    private Cipher decrypt;

    private final Endpoint endpoint;

    public SecureEndpointAsymmetric() {
        this.endpoint = new Endpoint();
        buildKey();
    }

    public SecureEndpointAsymmetric(int port) {
        this.endpoint = new Endpoint(port);
        buildKey();
    }

    private void buildKey() {
        PrivateKey privateKey;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CRYPTMODE);
            generator.initialize(4096);
            KeyPair keyPair = generator.generateKeyPair();

            privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

            this.decrypt = Cipher.getInstance(CRYPTMODE);
            this.decrypt.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(InetSocketAddress receiver, Serializable payload) {
        if (this.keys.get(receiver) == null) {
            this.endpoint.send(receiver, new KeyExchangeMessage(this.publicKey));
            this.keys.put(receiver, null);
            this.messages.add(new PendingMessage(receiver, payload));
            return;
        }

        byte[] encrypted = null;
        try {
            Cipher encrypt = Cipher.getInstance(CRYPTMODE);
            encrypt.init(Cipher.ENCRYPT_MODE, keys.get(receiver));
            encrypted = encrypt.doFinal(payloadToByteArray(payload));
        } catch (IllegalBlockSizeException | BadPaddingException | IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
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

        if (msg.getPayload() instanceof KeyExchangeMessage) {
            if (!this.keys.containsKey(msg.getSender())) {
                this.endpoint.send(msg.getSender(), new KeyExchangeMessage(this.publicKey));
            }
            this.keys.put(msg.getSender(), ((KeyExchangeMessage) msg.getPayload()).getKey());
            this.messages.stream().filter(entry -> entry.receiver.equals(msg.getSender())).forEach(item -> send(item.receiver,item.payload));
            this.messages.removeIf(entry -> entry.receiver.equals(msg.getSender()));
            return this.blockingReceive();
        }

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

    private static class PendingMessage {

        private final InetSocketAddress receiver;
        private final Serializable payload;

        public PendingMessage(InetSocketAddress receiver, Serializable payload) {
            this.receiver = receiver;
            this.payload = payload;
        }
    }
}
