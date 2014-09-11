import java.net.*;
import java.io.*;
import java.util.ArrayList;

class ChatServerThread implements Runnable {

    /* The client socket and IO we are going to handle in this thread */
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;
    String clientID = "-1";

    public ChatServerThread(Socket socket) {
        /* Assign local variable */
        this.socket = socket;
        
        /* Create the I/O variables */
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            
            /* Some debug */
            System.out.println("Client connected!");

            /* Say hi to the client */
            this.out.println("Welcome to the chat server!");
            if (ChatServer.threads.size() > 0) {
                this.out.println("Here's a list of connected users:");
                int id = 0;
                for (ChatServerThread thread : ChatServer.threads) {
                    this.out.println("     " + thread.clientID);
                    if (isInteger(thread.clientID) && Integer.parseInt(thread.clientID) == id)
                        id++;
                }
                clientID = "" + id;
            } else {
                clientID = "0";
                this.out.println("You're the only one here right now.");
            }
            //clientID = this.in.readLine();
            this.out.println("Your ID is: " + clientID);


        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
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

                String[] fromClientArr = fromClient.split("\\s+"); // split fromClient at spaces

                /* If the client said "bye", close the connection */
                if (fromClient.equals("bye")) {
                    System.out.println(clientID + " said bye, disconnecting");
                    this.out.println("bye!");
                    for (ChatServerThread thread : ChatServer.threads) {
                        if (thread.clientID.equals(clientID)) continue;
                        thread.out.println(clientID + " disconnected");
                    }
                    this.in.close();
                    this.out.close();
                    this.socket.close();

                    int index = -1;
                    for (int i = 0; i < ChatServer.threads.size(); i++)
                        if (ChatServer.threads.get(i).clientID.equals(this.clientID))
                            index = i;
                    ChatServer.threads.remove(index);
                    return;
                } else if (fromClient.length() >= 3 && fromClient.substring(0, 3).equals("/id")) {
                    if (fromClient.substring(3).equals("") || fromClient.substring(4).equals("")) {
                        System.out.println(clientID + " submitted an invalid /id command.");
                        this.out.println("Your ID change request did not contain an ID. Please try again. The proper format is '/id <your_new_id>'");
                    }
                    String newID = fromClient.substring(4);
                    for (ChatServerThread thread : ChatServer.threads) {
                        if (thread.clientID.equals(clientID)) continue;
                        thread.out.println("Poof! " + clientID + " is now " + newID + ".");
                    }
                    clientID = newID;
                    System.out.println(clientID + " changed ID to '" + newID + "'." );
                    this.out.println("Poof! You're now '" + clientID + "'.");
                }  else if (fromClient.length() >= 8 && fromClient.substring(0, 8).equals("/whisper")) {
                    if (fromClient..split("\\s+"); || fromClient.substring(4).equals("")) {

                    }
                } else {
                /* Otherwise send the text to other clients */
                    for (ChatServerThread thread : ChatServer.threads) {
                        if (thread.clientID.equals(clientID)) continue;
                        thread.out.println(clientID + ": " + fromClient);
                    }
                    System.out.println(clientID + " said: " + fromClient);
                }
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

    public static void main(String[] args) {
        
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
