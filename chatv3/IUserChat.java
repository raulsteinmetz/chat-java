package chatv3;

public interface IUserChat extends java.rmi.Remote {
    public void deliverMsg(String senderName, String msg);
}