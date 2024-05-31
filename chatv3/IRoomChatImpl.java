package chatv3;

import java.util.HashMap;
import java.util.Map;

public class IRoomChatImpl implements IRoomChat{
    private Map<String, IUserChat> userList;

    public IRoomChatImpl() {
        userList = new HashMap<>();
    }

    public void sendMsg(String usrName, String msg) {
        // todo
    }
    public void joinRoom(String usrName, IUserChat user) {
        // todo
    }
    public void leaveRoom(String usrName) {
        // todo
    }
    public void closeRoom() {
        // todo
    }
    public String getRoomName() {
        // todo
        return "";
    }
}
