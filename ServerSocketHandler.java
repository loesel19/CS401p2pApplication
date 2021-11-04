// This thread simply listens for connections on port 5000 and starts a new Connection Thread for each incoming connection

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class ServerSocketHandler extends Thread {

    Server s;
    ArrayList<Connection> connectionList;
    AtomicBoolean closing = new AtomicBoolean(false);

    public ServerSocketHandler(Server s, ArrayList<Connection> connectionList) {
        this.s = s;
        this.connectionList = connectionList;
    }

    /**
     * this method creates new threads whenever a client tries to connect to the server
     */
    public void run() {
        Socket clientSocket;
        while (!closing.get()) {
            // wait for incoming connections. Start a new Connection Thread for each incoming connection.
            try {
                if (connectionList.size() < 20) {
                    clientSocket = s.listener.accept();
                    Connection c = new Connection(clientSocket, connectionList);
                    connectionList.add(c);
                    c.start();
                    System.out.println("New connection added!");
                }else
                    System.out.println("Maximum number of clients already connected");

                for (int i = 0; i < connectionList.size(); i++) {
                    Connection t = connectionList.get(i);
                    //System.out.println(String.valueOf(t.FILE_VECTOR));
                    //System.out.println(t.toString());

                }
                System.out.println("--------------------");
            } catch (Exception e) {
                if (closing.get()) {
                    for (Connection c: connectionList) {
                        try {
                            System.out.printf("Peer %d is closing form ip: %s %n", c.peerID,c.peerIP.getHostName());
                            c.closing =true;
                        } catch (Exception exception) {

                        }
                    }
                    break;
                }
                e.printStackTrace();
            }
        }
        System.out.println("out of server socket handler loop");

    }

    //other methods may be necessary. Include them when appropriate.

}