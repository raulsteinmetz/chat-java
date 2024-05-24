package chatv2.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatServer extends Remote {
    boolean login(String name) throws RemoteException;
    void sendMessage(String name, String message) throws RemoteException;
    void sendWhisper(String sender, String receiver, String message) throws RemoteException;
    List<String> getNewMessages(int lastMessageId) throws RemoteException;
    List<String> getNewWhispers(String name, int lastMessageId) throws RemoteException;
    void logout(String name) throws RemoteException; 
}
