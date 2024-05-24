# Chat Application

This repository hosts two simple implementations of a chat application in Java. It was developed as a task for the Distributed Systems subject in college.

![_1](https://github.com/raulsteinmetz/chat-application-java/assets/85199336/ceb751a5-b41d-4230-b637-a0b34162bb15)
![_2](https://github.com/raulsteinmetz/chat-application-java/assets/85199336/be6cef01-fbdd-483d-87fc-1f99f5faf049)
![_3](https://github.com/raulsteinmetz/chat-application-java/assets/85199336/ca4d0cb0-e60e-4ebf-b238-7bd0b75b251c)
![_4](https://github.com/raulsteinmetz/chat-application-java/assets/85199336/c4de7027-1c67-475f-a0de-ea40ff79d392)



## Implementations

### Chatv1

Chatv1 implements a chat using threads, sockets, and serializable objects. The server maintains one socket for each client and broadcasts the messages received. It also keeps track of the number of clients in the application and manages the clients entering and leaving the chat.

### Chatv2

Chatv2 implements the same chat functionality but uses remote objects with Java RMI (Remote Method Invocation). In this implementation, the server is an object, and clients can call it to send messages, retrieve messages, send whispers, and retrieve whispers. The server maintains a memory of the messages and whispers.

## Running the Application

- To clean and compile the project, run:
```bash
  ./clean.sh
  ./compile.sh
 ```

### For Chatv1
- Start the Server and the Clients (each instance in a new terminal)
```bash
java chatv1.ChatServer
java chatv1.ChatClient <ip>
```


### For Chatv2
- Start the RMI registry in one terminal
```bash
rmiregistry
```
- Start the Server and the Clients (each instance in a new terminal)
```bash
java chatv2.server.ChatServerMain <ip> <port>
java chatv2.client.ChatClient <ip> <port>
```
