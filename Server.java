// The server class will implement the functions listed in the project description. 

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    int serverPort;
    int MAX_CONNECTED_CLIENTS;
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
        ServerSocketHandler handler = new ServerSocketHandler(s, s.connectionList);
        handler.start();
        System.out.println("Server socket handler is listening for incoming connections");

        // Note in programs shown in class, at this point we listen for incoming connections in the main method.
        // However for this project since the server has to handle incoming connections and also handle user input
        // simultaneously, we start a separate thread to listen for incoming connections in the Server. This is
        // the ServerSocketHandler thread, which will in turn spawn new Connection Threads, for each client connection.

        //Done! Now main() will just loop for user input!.
        Scanner stdinScanner = new Scanner(System.in);
        System.out.println("Server is waiting for user inputs!");
        userInputLoop:
        while (true) {
            // wait on user inputs
            String command = stdinScanner.nextLine();
            if (command.equals("q")) {
                quit(s.connectionList);
                break userInputLoop;
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
        //TODO: make sure all clients have quit before closing
        Packet p = new Packet();
        p.event_type = 6; //Server quitting

        for (Connection connection : connectionList) {
            try {
                connection.send(p);
            } catch (Exception e) {
            }
        }
    }
}

