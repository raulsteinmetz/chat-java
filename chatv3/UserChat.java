package chatv3;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class UserChat extends UnicastRemoteObject implements IUserChat, Serializable {
    private static final long serialVersionUID = 1L;

    private String serverIp;
    private int serverPort;
    private IServerChat server;
    private String userName;
    private IRoomChat currentRoom;

    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private DefaultListModel<String> roomListModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private Timer roomFetchTimer;

    public UserChat(String serverIp, int serverPort, String serverObjectName) throws RemoteException {
        super();
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        try {
            this.server = (IServerChat) Naming.lookup("//" + serverIp + ":" + serverPort + "/" + serverObjectName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initGUI();
        showLoginDialog();
    }

    private void initGUI() {
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel roomPanel = createRoomPanel();
        JPanel chatPanel = createChatPanel();

        mainPanel.add(roomPanel, "rooms");
        mainPanel.add(chatPanel, "chat");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void showLoginDialog() {
        userName = JOptionPane.showInputDialog(frame, "Enter your name:", "Login", JOptionPane.PLAIN_MESSAGE);

        if (userName != null && !userName.trim().isEmpty()) {
            cardLayout.show(mainPanel, "rooms");
            startRoomFetchTimer();
        } else {
            JOptionPane.showMessageDialog(frame, "Name cannot be empty. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            showLoginDialog();
        }
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        roomListModel = new DefaultListModel<>();
        JList<String> roomList = new JList<>(roomListModel);
        JScrollPane scrollPane = new JScrollPane(roomList);

        JButton joinButton = new JButton("Join Room");
        JButton createButton = new JButton("Create Room");

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = roomList.getSelectedValue();
                if (roomName != null) {
                    joinRoom(roomName);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a room to join.");
                }
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = JOptionPane.showInputDialog(frame, "Enter room name:");
                if (roomName != null && !roomName.trim().isEmpty()) {
                    try {
                        server.createRoom(roomName);
                        JOptionPane.showMessageDialog(frame, "Room " + roomName + " created successfully.");
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(frame, "Error creating room: " + ex.getMessage());
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageField.setText("");
                }
            }
        });

        JButton leaveButton = new JButton("Leave Room");
        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leaveRoom();
                cardLayout.show(mainPanel, "rooms");
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(leaveButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startRoomFetchTimer() {
        roomFetchTimer = new Timer();
        roomFetchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchRooms();
            }
        }, 0, 3000);
    }

    private void fetchRooms() {
        try {
            ArrayList<String> rooms = server.getRooms();
            SwingUtilities.invokeLater(() -> {
                roomListModel.clear();
                for (String room : rooms) {
                    roomListModel.addElement(room);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinRoom(String roomName) {
        try {
            currentRoom = (IRoomChat) Naming.lookup("//" + serverIp + ":" + serverPort + "/" + roomName);
            currentRoom.joinRoom(userName, this);
            chatArea.setText("");
            cardLayout.show(mainPanel, "chat");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error joining room: " + e.getMessage());
        }
    }

    private void leaveRoom() {
        try {
            if (currentRoom != null) {
                currentRoom.leaveRoom(userName);
                currentRoom = null;
                roomFetchTimer.cancel();
                startRoomFetchTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error leaving room: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        try {
            if (currentRoom != null) {
                currentRoom.deliverAll(userName, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error sending message: " + e.getMessage());
        }
    }

    @Override
    public void deliverMsg(String senderName, String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(senderName + ": " + msg + "\n");
        });
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java chatv3.UserChat <host> <port> <serverObjectName>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String objName = args[2];

        try {
            new UserChat(ip, port, objName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
