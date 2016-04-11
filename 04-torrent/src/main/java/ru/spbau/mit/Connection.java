package ru.spbau.mit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;

public abstract class Connection implements AutoCloseable {

    private Socket socket;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    protected Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public DataInputStream getInput() {
        return dataInputStream;
    }

    public DataOutputStream getOutput() {
        return dataOutputStream;
    }

    public String getHost() {
        return ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket in Connection");
        }
    }

    public int readRequest() throws IOException {
        return dataInputStream.readUnsignedByte();
    }

    protected <T> void writeCollection(
            Collection<T> collection,
            IOHandler.Writer<? super T> writer
    ) throws IOException {
        IOHandler.writeCollection(collection, writer, dataOutputStream);
    }

    protected <T, C extends Collection<T>> C readCollection(
            C collection,
            IOHandler.Reader<? extends T> reader
    ) throws IOException {
        return IOHandler.readCollection(collection, reader, dataInputStream);
    }
}
