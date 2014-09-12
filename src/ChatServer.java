import java.net.*;
import java.io.*;
import java.util.ArrayList;
/**
 * Created by ethan on 9/10/14.
 */
public class ChatServer {
    public static ArrayList<ChatServerThread> threads = new ArrayList<ChatServerThread>(0);
    public static ArrayList<ChatRoom> rooms = new ArrayList<ChatRoom>(0);

    public static void main(String[] args) {
        
        /* Check port exists */
        if (args.length < 1) {
            System.out.println("Usage: ChatServer <port>");
            System.exit(1);
        }
        
        /* This is the server socket to accept connections */
        ServerSocket serverSocket = null;
        
        /* Create the server socket */
        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }
        
        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (true) {
            try {
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();
                
                /* Create a thread for it and start! */
                ChatServerThread clientThread = new ChatServerThread(clientSocket);
                threads.add(clientThread);
                new Thread(clientThread).start();

            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
        }
    }

}
