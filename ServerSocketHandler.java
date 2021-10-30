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
                clientSocket = s.listener.accept();
                Connection c = new Connection(clientSocket, connectionList);
                connectionList.add(c);
                c.start();
                System.out.println("New connection added!");

                for (int i = 0; i < connectionList.size(); i++) {
                    Connection t = connectionList.get(i);
                    //System.out.println(String.valueOf(t.FILE_VECTOR));
                    //System.out.println(t.toString());

                }
                System.out.println("--------------------");
            } catch (IOException e) {
                if (closing.get()) break;
                e.printStackTrace();
            }
        }
    }

    //other methods may be necessary. Include them when appropriate.

}