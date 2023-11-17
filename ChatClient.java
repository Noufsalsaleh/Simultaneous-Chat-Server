
import java.io.*;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345; //0-1023 reserved 

        try (
            Socket socket = new Socket(serverAddress, serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // Prompt for the name and send it to the server
            System.out.print("What is your name? ");
            String name = consoleIn.readLine();
            out.println(name);

            String line;
            boolean hasJoined = false;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (!hasJoined && line.contains("has joined the chat")) {
                    hasJoined = true;
                    break;
                }
            }

            if (!hasJoined) {
                System.out.println("Failed to join the chat. Please try again.");
                return;
            }
            //responsible for handling user input and displaying received messages from the server
            Thread inputThread = new Thread(() -> {
                try {
                    String userInput;
                    while ((userInput = consoleIn.readLine()) != null) {//responsible for reading user input and sending it to the server. 
                        out.println(userInput);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            while (inputThread.isAlive()) { //runs as long as the client is connected 
                String message = in.readLine();
                if (message != null) {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
