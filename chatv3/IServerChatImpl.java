package chatv3;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class IServerChatImpl extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private int port;

    public IServerChatImpl(int port) throws RemoteException {
        super();
        this.port = port;
        roomList = new ArrayList<>();
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (roomList.contains(roomName)) {
            throw new RemoteException("Room already exists");
        }

        try {
            IRoomChatImpl room = new IRoomChatImpl(roomName);
            Naming.rebind("//localhost:" + port + "/" + roomName, room);
            roomList.add(roomName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error creating room: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatServer <port> <serverObjName>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String objName = args[1];

        try {
            LocateRegistry.createRegistry(port);
            IServerChatImpl server = new IServerChatImpl(port);
            Naming.rebind("//localhost:" + port + "/" + objName, server);
            System.out.println("Chat server is ready on port " + port + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
