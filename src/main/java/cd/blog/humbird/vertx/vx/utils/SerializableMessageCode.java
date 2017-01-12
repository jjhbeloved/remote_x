package cd.blog.humbird.vertx.vx.utils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.*;

public class SerializableMessageCode implements MessageCodec<Serializable, Serializable> {

    public static final String CODE_NAME = "serializable";

    @Override
    public void encodeToWire(Buffer buffer, Serializable s) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(s);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            buffer.appendInt(bytes.length);
            buffer.appendBytes(bytes);
        } catch (Exception ex) {

        }
    }

    @Override
    public Serializable decodeFromWire(int pos, Buffer buffer) {
        try {
            int length = buffer.getInt(pos);
            byte[] bytes = buffer.getBytes(pos + 4, pos + 4 + length);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (Serializable) ois.readObject();
        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public Serializable transform(Serializable s) {
        return s;
    }

    @Override
    public String name() {
        return CODE_NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

}
