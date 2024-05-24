package chatv2.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
    private Map<String, Integer> clients; // list of clients (dict with id)
    private List<String> messages; // global messages
    private Map<String, List<String>> whispers; // whisper messages (user, message dict)
    private int messageIdCounter; // used for retrieve messages control (clients receive only new messages)
    private Map<String, Integer> whisperIdCounters; // used for retrieve whispers control, a counter for each client

    protected ChatServerImpl() throws RemoteException {
        clients = new HashMap<>();
        messages = new ArrayList<>();
        whispers = new HashMap<>();
        messageIdCounter = 0;
        whisperIdCounters = new HashMap<>();
    }

    @Override
    public synchronized boolean login(String name) throws RemoteException {
        // client calls with a name, gets accepted in case this name isnt on the list already
        if (clients.containsKey(name)) {
            return false;
        }
        clients.put(name, clients.size());
        whispers.put(name, new ArrayList<>());
        whisperIdCounters.put(name, 0);
        messages.add("Server: " + name + " has entered the chat.");
        messageIdCounter++;
        return true;
    }

    @Override
    public synchronized void sendMessage(String name, String message) throws RemoteException {
        // handles global messages and logouts
        if (!clients.containsKey(name)) {
            throw new RemoteException("Client not logged in.");
        }
        if (message.equals("/quit")) {
            logout(name);
            return;
        }
        messages.add(name + ": " + message);
        messageIdCounter++;
    }

    @Override
    public synchronized void sendWhisper(String sender, String receiver, String message) throws RemoteException {
        // handles private messages
        if (!clients.containsKey(sender) || !clients.containsKey(receiver)) {
            throw new RemoteException("Client not logged in or receiver not found.");
        }
        String whisperMessage = "whisper from " + sender + " to " + receiver + ": " + message;
        whispers.get(sender).add(whisperMessage);
        whispers.get(receiver).add(whisperMessage);
        whisperIdCounters.put(sender, whisperIdCounters.get(sender) + 1);
        whisperIdCounters.put(receiver, whisperIdCounters.get(receiver) + 1);
    }

    @Override
    public synchronized List<String> getNewMessages(int lastMessageId) throws RemoteException {
        // client calls to retrieve global messages
        if (lastMessageId >= messageIdCounter) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages.subList(lastMessageId, messageIdCounter));
    }

    @Override
    public synchronized List<String> getNewWhispers(String name, int lastMessageId) throws RemoteException {
        // client calls to retrieve new whispers
        if (!whispers.containsKey(name)) {
            return new ArrayList<>();
        }
        List<String> userWhispers = whispers.get(name);
        int whisperIdCounter = whisperIdCounters.get(name);
        if (lastMessageId >= whisperIdCounter) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userWhispers.subList(lastMessageId, whisperIdCounter));
    }

    @Override
    public synchronized void logout(String name) throws RemoteException {
        // removes client of the managed lists
        if (clients.containsKey(name)) {
            clients.remove(name);
            whispers.remove(name);
            whisperIdCounters.remove(name);
            messages.add("Server: " + name + " has left the chat.");
            messageIdCounter++;
        }
    }
}
