package chatv1;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // needed for class compatibility during serialization
    private boolean isFromServer; // true if the message is from the server, false if from a client
    private MessageType type; // type of the message (enum)
    private String sender; // name of the sender
    private String content; // content of the message

    public Message(boolean isFromServer, MessageType type, String sender, String content) {
        this.isFromServer = isFromServer;
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public boolean isFromServer() {
        return isFromServer;
    }

    public String getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setFromServer(boolean isFromServer) {
        this.isFromServer = isFromServer;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "isFromServer=" + isFromServer +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
