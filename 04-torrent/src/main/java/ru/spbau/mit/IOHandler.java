package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

public abstract class IOHandler {

    private static final int IP4_LENGTH = 4;

    public static <T> void writeCollection(Collection<T> collection, Writer<? super T> writer,
                                           DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(collection.size());
        for (T elem : collection) {
            writer.write(dataOutputStream, elem);
        }
    }

    public static <T, C extends Collection<T>> C readCollection(C collection, Reader<? extends T> reader,
                                                                DataInputStream dataInputStream) throws IOException {
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            collection.add(reader.read(dataInputStream));
        }
        return collection;
    }

    public static void writeAddress(DataOutputStream dataOutputStream, InetSocketAddress address) throws IOException {
        dataOutputStream.write(address.getAddress().getAddress());
        dataOutputStream.writeShort(address.getPort());
    }

    public static InetSocketAddress readAddress(DataInputStream dataInputStream) throws IOException {
        byte[] buffer = new byte[IP4_LENGTH];
        for (int i = 0; i < IP4_LENGTH; i++) {
            buffer[i] = dataInputStream.readByte();
        }
        int port = dataInputStream.readUnsignedShort();
        return new InetSocketAddress(InetAddress.getByAddress(buffer), port);
    }

    public interface Writer<T> {
        void write(DataOutputStream dataOutputStream, T object) throws IOException;
    }

    public interface Reader<T> {
        T read(DataInputStream dataInputStream) throws IOException;
    }
}
