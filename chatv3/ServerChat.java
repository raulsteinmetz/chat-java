package chatv3;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private ArrayList<String> roomList;
    private int port;
    private DefaultListModel<String> roomListModel;
    private JList<String> roomListDisplay;

    public ServerChat(int port) throws RemoteException {
        super();
        this.port = port;
        roomList = new ArrayList<>();
        roomListModel = new DefaultListModel<>();
        roomListDisplay = new JList<>(roomListModel);
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
            RoomChat room = new RoomChat(roomName);
            Naming.rebind("//localhost:" + port + "/" + roomName, room);
            roomList.add(roomName);
            roomListModel.addElement(roomName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error creating room: " + e.getMessage());
        }
    }

    public void closeRoom(String roomName) {
        if (!roomList.contains(roomName)) {
            JOptionPane.showMessageDialog(null, "Room does not exist");
            return;
        }

        try {
            IRoomChat room = (IRoomChat) Naming.lookup("//localhost:" + port + "/" + roomName);
            room.closeRoom();
            Naming.unbind("//localhost:" + port + "/" + roomName);
            roomList.remove(roomName);
            roomListModel.removeElement(roomName);
            JOptionPane.showMessageDialog(null, "Room " + roomName + " has been closed.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error closing room: " + e.getMessage());
        }
    }

    private void startServerGUI() {
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());

        roomListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(roomListDisplay);

        JButton createButton = new JButton("Create Room");
        JButton removeButton = new JButton("Remove Room");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = JOptionPane.showInputDialog(frame, "Enter room name:");
                if (roomName != null && !roomName.trim().isEmpty()) {
                    try {
                        createRoom(roomName);
                        JOptionPane.showMessageDialog(frame, "Room " + roomName + " created successfully.");
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(frame, "Error creating room: " + ex.getMessage());
                    }
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomListDisplay.getSelectedValue();
                if (roomName != null) {
                    closeRoom(roomName);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a room to remove.");
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createButton);
        buttonPanel.add(removeButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java chatv3.ServerChat <port> <serverObjName>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String objName = args[1];

        try {
            LocateRegistry.createRegistry(port);
            ServerChat server = new ServerChat(port);
            Naming.rebind("//localhost:" + port + "/" + objName, server);
            System.out.println("Chat server is ready on port " + port + ".");
            server.startServerGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
