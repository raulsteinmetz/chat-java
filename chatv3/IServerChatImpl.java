package chatv3;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class IServerChatImpl extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;

    public IServerChatImpl() throws RemoteException {
        super();
        roomList = new ArrayList<>();
        // adding dummies for now (working on client side)
        roomList.add("room1");
        roomList.add("room2");
        roomList.add("room3");
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return roomList;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        // todo
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
            IServerChatImpl server = new IServerChatImpl();
            Naming.rebind("//localhost:" + port + "/" + objName, server);
            System.out.println("Chat server is ready on port " + port + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
