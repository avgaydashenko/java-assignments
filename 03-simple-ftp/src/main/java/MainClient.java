import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainClient {

    public static void main(String[] args) throws IOException {

        if (args.length < 4) {
            error();
        }

        String host = args[0];

        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            error();
        }

        String command = args[2];
        String path = args[3];

        Client client = new Client(host, port);

        if (Objects.equals(command, "get")) {
            client.get(path, System.out);
        } else if (Objects.equals(command, "list")) {
            ArrayList<Client.FileDescription> fileDescriptionArrayList = client.list(path);
            for (Client.FileDescription fileDescription : fileDescriptionArrayList) {
                System.out.print(fileDescription.getName() + " " + fileDescription.getIsDir().toString() + "\n");
            }
        } else {
            error();
        }
    }

    private static void error() {
        System.err.println("Expected: (String)host, (int)port, (String)command, (String)path");
        System.exit(1);
    }
}
