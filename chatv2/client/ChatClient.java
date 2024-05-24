package chatv2.client;

import chatv2.server.ChatServer;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.util.List;

public class ChatClient {
    private ChatServer server; // remote object
    private String name; // client name
    private int lastMessageId; // used for global message retrieval
    private int lastWhisperId; // used for private message retrieval

    // interface
    private JFrame frame;
    private JTextPane chatPane;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField nameField;
    private JButton loginButton;
    private JPanel loginPanel;
    private JPanel chatPanel;
    private StyledDocument doc;

    public ChatClient(String serverAddress, int port) {
        try {
            server = (ChatServer) Naming.lookup("//" + serverAddress + ":" + port + "/ChatServer"); // gets the remote server object
            lastMessageId = 0;
            lastWhisperId = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        createUI();
    }

    private void createUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        doc = chatPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatPane);
        frame.add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                if (message.startsWith("/whisper ")) {
                    sendWhisper(message);
                } else if (message.equals("/quit")) {
                    logout();
                } else {
                    sendMessage(message);
                }
                messageField.setText("");
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);
        chatPanel.setVisible(false); // initially hidden

        nameField = new JTextField();
        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login(nameField.getText());
            }
        });

        loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        loginPanel.add(new JLabel("Enter your name: "), BorderLayout.WEST);
        loginPanel.add(nameField, BorderLayout.CENTER);
        loginPanel.add(loginButton, BorderLayout.EAST);

        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(chatPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void login(String name) {
        try {
            boolean success = server.login(name);
            if (success) {
                this.name = name;
                JOptionPane.showMessageDialog(frame, "Login successful!", "Info", JOptionPane.INFORMATION_MESSAGE);
                loginPanel.setVisible(false); // hide login panel
                chatPanel.setVisible(true); // show chat panel
                appendColoredMessage("you have entered the chat.", Color.GRAY);
            } else {
                JOptionPane.showMessageDialog(frame, "Name not accepted, choose a new one", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        // calls remote server object to send global message
        try {
            server.sendMessage(name, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendWhisper(String message) {
        // calls remote server object to send private message
        try {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                JOptionPane.showMessageDialog(frame, "Invalid whisper format. Use /whisper <name> <message>", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String receiver = parts[1];
            String whisperMessage = parts[2];
            server.sendWhisper(name, receiver, whisperMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getNewMessages() {
        // retrieves global messages
        try {
            List<String> newMessages = server.getNewMessages(lastMessageId);
            for (String message : newMessages) {
                appendMessage(message);
            }
            lastMessageId += newMessages.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getNewWhispers() {
        // retrieves private messages
        try {
            List<String> newWhispers = server.getNewWhispers(name, lastWhisperId);
            for (String whisper : newWhispers) {
                appendMessage(whisper);
            }
            lastWhisperId += newWhispers.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String message) {
        try {
            if (message.startsWith("Server: ")) {
                if (!message.contains(name + " has entered the chat.")) {
                    message = message.replace("Server: ", "");
                    appendColoredMessage(message, Color.GRAY);
                }
            } else if (message.contains("whisper from " + name)) {
                message = message.replace("whisper from " + name, "you whispered");
                appendColoredMessage(message, Color.MAGENTA);
            } else if (message.contains("whisper from ")) {
                message = message.replace(name, "you");
                appendColoredMessage(message, Color.MAGENTA);
            } else if (message.startsWith(name + ": ")) {
                message = message.replace(name + ": ", "you: ");
                appendColoredMessage(message, Color.BLUE);
            } else {
                appendColoredMessage(message, Color.BLACK);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendColoredMessage(String message, Color color) throws BadLocationException {
        Style style = chatPane.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        doc.insertString(doc.getLength(), message + "\n", style);
    }

    public void startMessageRetrievalThread() {
        // this thread keeps calling the retrieve messages method
        Thread messageRetrievalThread = new Thread(() -> {
            while (true) {
                getNewMessages();
                getNewWhispers();
                try {
                    Thread.sleep(500); // wait for .5 seconds before checking for new messages again
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageRetrievalThread.start();
    }

    public void logout() {
        try {
            server.logout(name);
            frame.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ChatClient client = new ChatClient(host, port);
        client.startMessageRetrievalThread();
    }
}
