import java.io.IOException;

public class MainServer {

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            error();
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            error();
        }

        Server server = new Server(port);
        server.start();
    }

    private static void error() {
        System.err.println("Expected: (int)port");
        System.exit(1);
    }
}
