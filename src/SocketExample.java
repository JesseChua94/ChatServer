import java.net.*;
import java.io.*;

public class SocketExample {
    
    /* I/O Variables */
    public static Socket         socket = null;
    public static PrintWriter    out = null;
    public static BufferedReader in = null;
    
    public static void main(String [] args) {
        
        /* Check arguments */
        if (args.length < 2) {
            System.out.println("Usage: SocketExample <host> <port>");
            System.exit(1);
        }
        
        
        
        /* This part needs exception handling */
        try {
            /* Create socket */
            SocketExample.socket = new Socket(args[0], Integer.parseInt(args[1]));
            
            /* Create IO */
            SocketExample.out = new PrintWriter(socket.getOutputStream(), true);
            SocketExample.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            /* Why autoflush = true for PrintWriter? */
            /* Why BufferedReader? */
            
        } catch (UnknownHostException e) {
            /* Exceptions will print and exit */
            
            System.out.println("Unknown host: " + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }
        
        
        /* If we get here, we have a well-connected socket! */
        
        /* First thing is to start a thread that will get lines from the server
         * and output them to the user.
         */
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String fromServer = SocketExample.in.readLine();
                        
                        /* Check connection terminated */
                        if (fromServer == null) {
                            System.out.println("Connected terminated!");
                            System.exit(1);
                        }
                        
                        /* Otherwise it's valid data, print it! */
                        System.out.println(fromServer);
                    } catch (IOException e) {
                        System.out.println("IOException: " + e);
                        System.exit(1);
                    }
                }
            }
        }).start();
        
        /* Create a buffered reader for user input */
        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
        
        /* In the main thread, we'll create an infinite loop that gets
         * input from the user and sends it to the server.
         */    
        while (true) {
            try {
                String fromUser = userIn.readLine();
                SocketExample.out.println(fromUser);
            } catch (IOException e) {
                System.out.println("IOException: " + e);
                System.exit(1);
            }
        }
        
        
    }
}
