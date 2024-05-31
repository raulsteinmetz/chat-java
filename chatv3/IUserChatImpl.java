package chatv3;

import java.rmi.Naming;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IUserChatImpl extends JFrame implements IUserChat {
    private String serverIp;
    private int serverPort;
    private IServerChat server;
    private String userName;

    private JTextField nameField;
    private JLabel nameLabel;
    private DefaultListModel<String> roomListModel;

    public IUserChatImpl(String serverIp, int serverPort, String serverObjectName) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        // getting the remote server object
        try {
            this.server = (IServerChat) Naming.lookup("//" + serverIp + ":" + serverPort + "/" + serverObjectName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupGUI();
    }

    private void setupGUI() {
        setTitle("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        nameLabel = new JLabel("Enter your name: ");
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField = new JTextField(20);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
        JButton enterButton = new JButton("Enter");
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        namePanel.add(nameLabel);
        namePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        namePanel.add(nameField);
        namePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        namePanel.add(enterButton);

        add(namePanel, BorderLayout.NORTH);

        roomListModel = new DefaultListModel<>();
        JList<String> roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane roomScrollPane = new JScrollPane(roomList);
        add(roomScrollPane, BorderLayout.CENTER);

        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = nameField.getText();
                if (!userName.isEmpty()) {
                    nameLabel.setText("Hello, " + userName + "!");
                    nameField.setVisible(false);
                    enterButton.setVisible(false);
                    fetchRooms();
                } else {
                    JOptionPane.showMessageDialog(IUserChatImpl.this, "Please enter your name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    private void fetchRooms() {
        try {
            ArrayList<String> rooms = server.getRooms();
            for (String room : rooms) {
                roomListModel.addElement(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliverMsg(String senderName, String msg) {
        // todo
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ChatClient <host> <port> <serverObjectName>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String objName = args[2];

        SwingUtilities.invokeLater(() -> new IUserChatImpl(ip, port, objName));
    }
}