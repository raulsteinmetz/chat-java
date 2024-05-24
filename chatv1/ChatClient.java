package chatv1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;


public class ChatClient {

    String client_name;

    String serverAddress;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    
    JTextPane messageArea = new JTextPane();



    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        messageArea.setPreferredSize(new Dimension(500, 260));
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String content = textField.getText();
                    System.out.println(content);
                    Message message;
                    if (content.toLowerCase().startsWith("/quit")) {
                        message = new Message(false, MessageType.COMMAND, client_name, content);
                    } else {
                        message = new Message(false, MessageType.MESSAGE, client_name, content);
                    }
                    
                    out.writeObject(message);
                    textField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private String getName() {
        client_name =  JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection", JOptionPane.PLAIN_MESSAGE);
        return client_name;
    }

    private void appendToPane(String msg, Color c) {
        StyledDocument doc = messageArea.getStyledDocument();
    
        Style style = messageArea.addStyle("I'm a Style", null);
        StyleConstants.setForeground(style, c);
    
        try {
            doc.insertString(doc.getLength(), msg, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                try {
                    Message message = (Message) in.readObject();
                    if (message.getType() == MessageType.REQUEST_NAME) {
                        Message send = new Message(false, MessageType.SUBMIT_NAME, getName(), null);
                        out.writeObject(send);
                    } else if (message.getType() == MessageType.CONFIRMED_NAME) {
                        this.frame.setTitle("Chatter - " + client_name);
                        textField.setEditable(true);
                    } else if (message.getType() == MessageType.MESSAGE) {
                        String sender = message.getSender();
                        if (sender.toLowerCase().startsWith(client_name)) {
                            sender = "you";
                        }
                        appendToPane(sender + ": ", Color.GREEN);
                        appendToPane(message.getContent() + "\n", Color.BLACK);
                    } else if (message.isFromServer() == true) {
                        appendToPane(message.getContent() + "\n", Color.BLUE);
                    }
                    


                } catch (ClassNotFoundException classNotFoundException) {
                    classNotFoundException.printStackTrace();
                } catch (EOFException eofException) {
                    break;
                }
            }
            socket.close();
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        var client = new ChatClient(args[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
