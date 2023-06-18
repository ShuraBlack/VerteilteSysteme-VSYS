package aqua.common.endpoint;

import java.io.*;

public class SerializableUtil {

    public static byte[] payloadToByteArray(Serializable payload) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(payload);
        oos.flush();
        return bos.toByteArray();
    }

    public static Serializable payloadFromByteArray(byte[] payload) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(payload);
        ObjectInputStream is = new ObjectInputStream(bais);
        return (Serializable) is.readObject();
    }

}
