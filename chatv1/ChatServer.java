package chatv1;

import java.io.EOFException;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;

import java.util.concurrent.Executors;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



// implements chat server
public class ChatServer {

    // stores client names
    private static Set<String> names = new HashSet<>();

    // set of all the print writers for all the clients, used for broadcast
    private static Set<ObjectOutputStream> writers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        var pool = Executors.newFixedThreadPool(500); // limits number of client sockets
        try (var listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept())); // listens for new clients
            }
        }
    }

    // client handler
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;


        private ObjectInputStream in;
        private ObjectOutputStream out;

        // handler for new clients
        public Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            Message message;
            Message received;

            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());

                // requests for client identification (name)
                while (true) {
                    message = new Message(true, MessageType.REQUEST_NAME, "server", null);
                    out.writeObject(message);
                    received = (Message) in.readObject();
                    if (received == null) {
                        return;
                    }
                    name = received.getSender();
                    synchronized (names) {
                        if (!name.isBlank() && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                message = new Message(true, MessageType.CONFIRMED_NAME, "server", null);
                out.writeObject(message);

                synchronized(writers) {
                    for (ObjectOutputStream writer : writers) { // informs other clients of the new one, broadcast
                        message = new Message(true, MessageType.LOG, "server", name + " has joined");
                        writer.writeObject(message);
                    }
                    writers.add(out); // add new client in the broadcast list
                }

                // listens for client's messages
                while (true) {
                    received = (Message) in.readObject();
                    if (received.getType() == MessageType.COMMAND && received.getContent().toLowerCase().startsWith("/quit")) {
                        return;
                    }

                    synchronized(writers) {
                        for (ObjectOutputStream writer : writers) {
                            writer.writeObject(received);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    synchronized(writers) {
                        writers.remove(out);
                    }
                }
                if (name != null) {
                    System.out.println(name + " is leaving");
                    synchronized(names) {
                        names.remove(name);
                    }
                    synchronized(writers) {
                        for (ObjectOutputStream writer : writers) {
                            message = new Message(true, MessageType.LOG, "server", name + " has left");
                            try {
                                writer.writeObject(message);
                            } catch (EOFException eofException) {
                                break;
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
