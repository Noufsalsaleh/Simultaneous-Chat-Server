
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        //set up serverSocket that is used to accept incoming client connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //broadcast messages to the connected clients
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String name;
        //responsible for managing the communication with a single client
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        //send message to that client
        public void sendMessage(String message) {
            out.println(message);
        }
        //new client connection is established,
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//read message sent by client
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)//send messages to the client's output stream
            ) {
                this.out = out;

                name = in.readLine();
                if (name == null || name.isEmpty()) {
                   out.println("A name must be entered.");
                    return;// Don't proceed further with this client
                }
                
                out.println("Connected.");
                out.println("Hi " + name + ", you can start chatting with friends. Type 'bye' to exit.");
                
                synchronized (clients) {
                    clients.add(this);
                }
                System.out.println(name+" has joined the chat");//server summary
                broadcast(name + " has joined the chat");//to all clients 

                String message;
                while ((message = in.readLine()) != null) {//loop read message from client
                    if ("bye".equalsIgnoreCase(message)) {
                        out.println("Good bye");
                        break;
                    }
                    broadcast(name + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (name != null || !(name.isEmpty()) ) {
                    System.out.println(name + " has left the chat");
                    broadcast(name + " has left the chat");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clients) {
                    clients.remove(this);
                }
            }
        }
    }
}

