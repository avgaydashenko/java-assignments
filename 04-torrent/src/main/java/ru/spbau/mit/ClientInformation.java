package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ClientInformation {

    private InetSocketAddress socketAddress;
    private List<Integer> filesIds;

    public ClientInformation(InetSocketAddress socketAddress, List<Integer> filesIds) {
        this.socketAddress = socketAddress;
        this.filesIds = filesIds;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public List<Integer> getFilesIds() {
        return filesIds;
    }

    public void writeTo(DataOutputStream dataOutputStream) throws IOException {
        IOHandler.writeAddress(dataOutputStream, socketAddress);
        IOHandler.writeCollection(filesIds, DataOutputStream::writeInt, dataOutputStream);
    }

    public static ClientInformation readFrom(DataInputStream dataInputStream) throws IOException {
        return new ClientInformation(IOHandler.readAddress(dataInputStream),
                IOHandler.readCollection(new ArrayList<>(), DataInputStream::readInt, dataInputStream)
        );
    }
}
