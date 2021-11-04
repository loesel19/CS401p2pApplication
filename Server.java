// The server class will implement the functions listed in the project description. 

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    //how long to wait for clients to close when shutting down server before force closing
    private static final int SERVER_CLOSING_TIMEOUT = 50000;   //in ms

    int serverPort;
    static int MAX_CONNECTED_CLIENTS;
    ServerSocket listener;
    int numClients;
    ArrayList<Connection> connectionList;


    public Server() {
        serverPort = 5000;
        MAX_CONNECTED_CLIENTS = 20;
        listener = null;
        numClients = 0;
        connectionList = new ArrayList<>();
    }

    public static void main(String args[]) throws IOException {
        //First, let's start our server and bind it to a port(5000).
        Server s = new Server();
        s.listener = new ServerSocket(s.serverPort);
        //Next let's start a thread that will handle incoming connections
        System.out.println("Server is up");
        ServerSocketHandler handler = new ServerSocketHandler(s, s.connectionList, MAX_CONNECTED_CLIENTS);
        handler.start();
        System.out.println("Server socket handler is listening for incoming connections");

        // Note in programs shown in class, at this point we listen for incoming connections in the main method.
        // However for this project since the server has to handle incoming connections and also handle user input
        // simultaneously, we start a separate thread to listen for incoming connections in the Server. This is
        // the ServerSocketHandler thread, which will in turn spawn new Connection Threads, for each client connection.

        //Done! Now main() will just loop for user input!.
        Scanner stdinScanner = new Scanner(System.in);
        System.out.println("Server is waiting for user inputs!");
        while (true) {
            // wait on user inputs
            String command = stdinScanner.nextLine();
            if (command.equals("q")) {
                handler.closing.set(true);
                quit(s.connectionList);
                break;
            } else if (command.equals("p")) {
                printClients(s.connectionList);
            }
        }
        //will quit on user input
        s.listener.close();
    }

    // add other methods as necessary. For example, you will probably need a method to print the incoming connection info.

    static void printClients(ArrayList<Connection> connectionList) {
        System.out.println("Printing clients:");
        for (Connection connection : connectionList) {
            System.out.printf("%5s : %s%n", connection.peerID, String.valueOf(connection.FILE_VECTOR));
        }
    }

    static void quit(ArrayList<Connection> connectionList) {
        Packet p = new Packet();
        p.event_type = 6; //Server quitting
        System.out.println("trying to quit");

        for (Connection connection : connectionList) {
            try {
                connection.send(p);
            } catch (Exception e) {
            }
        }

        long startClosingTime = System.currentTimeMillis();
        while(connectionList.size() > 0) {
            //FIXME: socket won't close when any clients are connected. (maybe because it's waiting for input)
            if ((System.currentTimeMillis() - startClosingTime) > SERVER_CLOSING_TIMEOUT)
                break;
        }
    }
}

