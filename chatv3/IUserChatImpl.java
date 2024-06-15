package chatv3;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class IUserChatImpl extends UnicastRemoteObject implements IUserChat, Serializable {
    private static final long serialVersionUID = 1L;

    private String serverIp;
    private int serverPort;
    private IServerChat server;
    private String userName;
    private IRoomChat currentRoom;
    private Scanner scanner;

    public IUserChatImpl(String serverIp, int serverPort, String serverObjectName) throws RemoteException {
        super(); // Call the UnicastRemoteObject constructor
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.scanner = new Scanner(System.in);

        try {
            this.server = (IServerChat) Naming.lookup("//" + serverIp + ":" + serverPort + "/" + serverObjectName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startChat();
    }

    private void clearTerminal() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startChat() {
        clearTerminal();
        System.out.println("Enter your name: ");
        userName = scanner.nextLine();

        if (!userName.isEmpty()) {
            clearTerminal();
            System.out.println("Hello, " + userName + "!");
            fetchRooms();
            showChatMenu();
        } else {
            System.out.println("Please enter your name.");
            startChat();
        }
    }

    private void showChatMenu() {
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Create Room");
            System.out.println("2. Fetch Rooms");
            System.out.println("3. Join Room");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            clearTerminal();

            switch (choice) {
                case "1":
                    createRoom();
                    break;
                case "2":
                    fetchRooms();
                    break;
                case "3":
                    joinRoom();
                    break;
                case "4":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    private void fetchRooms() {
        try {
            ArrayList<String> rooms = server.getRooms();
            System.out.println("Available rooms:");
            for (String room : rooms) {
                System.out.println(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRoom() {
        System.out.print("Enter room name: ");
        String roomName = scanner.nextLine();

        if (!roomName.isEmpty()) {
            try {
                server.createRoom(roomName);
                System.out.println("Room " + roomName + " created successfully.");
            } catch (RemoteException e) {
                if (e.getMessage().contains("Room already exists")) {
                    System.out.println("Room with this name already exists.");
                } else {
                    e.printStackTrace();
                    System.out.println("Error creating room: " + e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error creating room: " + e.getMessage());
            }
        } else {
            System.out.println("Please enter a room name.");
        }
    }

    private void joinRoom() {
        System.out.print("Enter room name to join: ");
        String roomName = scanner.nextLine();

        try {
            currentRoom = (IRoomChat) Naming.lookup("//" + serverIp + ":" + serverPort + "/" + roomName);
            currentRoom.joinRoom(userName, this);
            chatInRoom();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error joining room: " + e.getMessage());
        }
    }

    private void chatInRoom() {
        clearTerminal();
        System.out.println("Joined room. Type 'leave' to leave the room.");

        while (true) {
            System.out.print("> ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("leave")) {
                leaveRoom();
                break;
            }

            sendMessage(message);
        }
    }

    private void leaveRoom() {
        try {
            if (currentRoom != null) {
                currentRoom.leaveRoom(userName);
                System.out.println("Left the room.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error leaving room: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        try {
            if (!message.isEmpty() && currentRoom != null) {
                currentRoom.sendMsg(userName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending message: " + e.getMessage());
        }
    }

    @Override
    public void deliverMsg(String senderName, String msg) {
        System.out.println(senderName + ": " + msg);
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java chatv3.IUserChatImpl <host> <port> <serverObjectName>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String objName = args[2];

        try {
            new IUserChatImpl(ip, port, objName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
