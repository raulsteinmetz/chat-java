# Chat Application

This repository hosts two simple implementations of a chat application in Java. It was developed as a task for the Distributed Systems subject in college.

![_1](https://github.com/raulsteinmetz/chat-java/assets/85199336/d29fba89-d24e-4b21-ac54-50a1334337b7)
![_2](https://github.com/raulsteinmetz/chat-java/assets/85199336/c906685b-1218-43e0-8c1e-0cef422823b2)
![_3](https://github.com/raulsteinmetz/chat-java/assets/85199336/79aca8e7-8a3d-4e9d-a8e4-0099d23380fd)
![_4](https://github.com/raulsteinmetz/chat-java/assets/85199336/831150e9-c6f0-44b8-94f7-cf0c73984b5c)


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
