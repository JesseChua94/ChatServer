import java.net.*;
import java.io.*;
import java.util.ArrayList;

class ChatServerThread implements Runnable {

    /* The client socket and IO we are going to handle in this thread */
    protected Socket         socket;
    protected PrintWriter    out;
    protected BufferedReader in;
    String clientID;

    public ChatServerThread(Socket socket) {
        /* Assign local variable */
        this.socket = socket;
        
        /* Create the I/O variables */
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            
            /* Some debug */
            System.out.println("Client connected!");

            /* Say hi to the client */
            this.out.println("Welcome to the chat server!");
            this.out.println("Here's a list of connected users:");
            for (ChatServerThread thread : ChatServer.threads)
                this.out.println(thread.clientID);

            this.out.println("Enter an ID");
            clientID = this.in.readLine();
            System.out.println("ID is : " + clientID);



        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public void run() {
        /* Our thread is going to read lines from the client and parrot them back.
           It will continue to do this until an exception occurs or the connection ends
           */
        while (true) {
            try {
                /* Get string from client */
                String fromClient = this.in.readLine();
                
                /* If null, connection is closed, so just finish */
                if (fromClient == null) {
                    System.out.println(clientID + "disconnected");
                    this.in.close();
                    this.out.close();
                    this.socket.close();
                    return;
                }
                
                /* If the client said "bye", close the connection */
                if (fromClient.equals("bye")) {
                    System.out.println(clientID + " said bye, disconnecting");
                    this.out.println("bye!");
                    for (ChatServerThread thread : ChatServer.threads)
                        thread.out.println(clientID + " disconnected");
                    this.in.close();
                    this.out.close();
                    this.socket.close();

                    int index = -1;
                    for (int i = 0; i < ChatServer.threads.size(); i++)
                        if (ChatServer.threads.get(i).clientID.equals(this.clientID))
                            index = i;
                    ChatServer.threads.remove(index);
                    return;
                }
                
                /* Otherwise send the text to other clients */
                for (ChatServerThread thread : ChatServer.threads) {
                    thread.out.println(clientID + ": " + fromClient);
                }
                System.out.println(clientID + " said: " + fromClient);

            } catch (IOException e) {
                /* On exception, stop the thread */
                System.out.println("IOException: " + e);
                return;
            }
        }
    }

}

public class ChatServer {
    public static ArrayList<ChatServerThread> threads = new ArrayList<ChatServerThread>(0);

    public static void main(String [] args) {
        
        /* Check port exists */
        if (args.length < 1) {
            System.out.println("Usage: ParrotServerExample <port>");
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
