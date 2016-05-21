package sample.torrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractServer implements Server {
    private ServerSocket serverSocket;
    private short port;
    private ExecutorService taskExecutor;
    private HandlerFactory handlerFactory;

    public AbstractServer(short port) {
        this.port = port;
    }

    @Override
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("AbstractServer#start: failed to create new ServerSocket, probably the port is wrong.");
        }
        taskExecutor = Executors.newCachedThreadPool();
        taskExecutor.execute(() -> {
            while (true) {
                synchronized (this) {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        break;
                    }
                }
                try {
                    Socket clientSocket = serverSocket.accept();
                    taskExecutor.submit(handlerFactory.getHandler(clientSocket));
                } catch (IOException e) {
                    System.out.println("AbstractServer#start: failed to create clientSocket, probably the serverSocket is wrong.");
                }
            }
        });
    }

    @Override
    public void stop() {
        if (serverSocket == null) {
            return;
        }
        try {
            taskExecutor.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("AbstractServer#stop: failed to close serverSocket.");
        }
        serverSocket = null;
    }

    protected void setHandlerFactory(HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }
}
