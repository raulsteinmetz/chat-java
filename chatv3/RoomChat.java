package chatv3;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private String roomName;
    private Map<String, IUserChat> userList;

    public RoomChat(String roomName) throws RemoteException {
        super();
        this.roomName = roomName;
        userList = new HashMap<>();
    }

    @Override
    public void deliverAll(String usrName, String msg) throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public void joinRoom(String usrName, IUserChat user) throws RemoteException {
        userList.put(usrName, user);
        deliverAll("System", usrName + " has joined the room.");
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException {
        userList.remove(usrName);
        deliverAll("System", usrName + " has left the room.");
    }

    @Override
    public void closeRoom() throws RemoteException {
        for (IUserChat user : userList.values()) {
            user.deliverMsg("System", "The room has been closed.");
        }
        userList.clear();
    }

    @Override
    public String getRoomName() throws RemoteException {
        return roomName;
    }
}
