import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * Created by ethan on 9/10/14.
 */
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
            for (ChatServerThread thread : ChatServer.threads)
                thread.out.println("'" + clientID + "' has connected.");

        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
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
                } else if (fromClient.length() >= 3 && fromClientArr[0].equals("/id")) {
                    if (fromClient.substring(3).equals("") || fromClient.substring(4).equals("")) {
                        System.out.println(clientID + " submitted an invalid /id command.");
                        this.out.println("Your ID change request did not contain an ID. Please try again. The proper format is '/id <your_new_id>'");
                    }
                    String newID = fromClient.substring(4);
                    if (newID.equals(clientID)) {
                        this.out.println("You're already " + clientID);
                        System.out.println(clientID + " tried to change their name to '" + clientID + "', but that's already his/her name.");
                    }

                    boolean validName = true;
                    for (ChatServerThread thread : ChatServer.threads) {
                        if (newID.equals(thread.clientID)) {
                            System.out.println(clientID + " tried to change names to '" + newID + "', but that's already taken.");
                            this.out.println("'" + newID + "' is already taken.");
                            validName = false;
                        }
                    }
                    if (validName) {
                        for (ChatServerThread thread : ChatServer.threads) {
                            if (thread.clientID.equals(clientID)) continue;
                            thread.out.println("Poof! " + clientID + " is now " + newID + ".");
                        }
                        clientID = newID;
                        System.out.println(clientID + " changed ID to '" + newID + "'.");
                        this.out.println("Poof! You're now '" + clientID + "'.");
                    }
                } else if (fromClientArr.length >= 3 && fromClientArr[0] != null && fromClientArr[1] != null && fromClientArr[2] != null && fromClientArr[0].equals("/whisper")) {
                    String recipient = fromClientArr[1];
                    String message = fromClientArr[2];
                    for (int i = 3; i < fromClientArr.length; i++) {
                        if (fromClientArr[i] != null)
                            message += " " + fromClientArr[i];
                    }
                    boolean notFound = true;
                    if (recipient.equals(clientID)) {
                        System.out.println("'" + clientID + "' tried to whisper to himself.");
                        this.out.println("You can't message yourself!");
                    } else {
                        for (ChatServerThread thread : ChatServer.threads) {
                            if (thread.clientID.equals(recipient)) {
                                thread.out.println(clientID + " >> " + message);
                                notFound = false;
                            }
                        }
                        if (notFound) this.out.println("User '" + recipient + "' not found.");
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