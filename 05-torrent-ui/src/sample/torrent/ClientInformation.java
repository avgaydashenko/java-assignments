package sample.torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientInformation {
    private static final int IP_BYTE_NUMBER = 4;

    private byte[] ip;
    private short port;

    public ClientInformation(byte[] ip, short port) {
        this.ip = ip;
        this.port = port;
    }

    public byte[] getIp() {
        return ip;
    }

    public short getPort() {
        return port;
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(ip);
        dataOutputStream.writeShort(port);
    }

    public static ClientInformation readFrom(DataInputStream dataInputStream) throws IOException {
        byte[] ip = new byte[IP_BYTE_NUMBER];
        dataInputStream.read(ip, 0, IP_BYTE_NUMBER);
        short port = dataInputStream.readShort();
        return new ClientInformation(ip, port);
    }
}
