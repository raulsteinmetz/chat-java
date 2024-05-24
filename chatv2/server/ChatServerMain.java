package chatv2.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ChatServerMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatServerMain <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            LocateRegistry.createRegistry(port);
            ChatServerImpl server = new ChatServerImpl();
            Naming.rebind("//" + host + ":" + port + "/ChatServer", server);
            System.out.println("Chat server is ready on " + host + ":" + port + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
