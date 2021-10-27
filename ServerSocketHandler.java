// This thread simply listens for connections on port 5000 and starts a new Connection Thread for each incoming connection
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class ServerSocketHandler extends Thread
{

    Server s;
    ArrayList<Connection> connectionList;

    public ServerSocketHandler(Server s, ArrayList<Connection> connectionList){
        this.s=s;
        this.connectionList=connectionList;
    }

    /**
     * this method creates new threads whenever a client tries to connect to the server
     */
    public void run(){
        Socket clientSocket;
        while (true){
           // wait for incoming connectioins. Start a new Connection Thread for each incoming connection.
            try {
                clientSocket =s.listener.accept();
                Connection c = new Connection(clientSocket, connectionList);

                connectionList.add(c);
                System.out.println("New connection added!");
                System.out.println(connectionList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    //other methods may be necessary. Include them when appropriate.

}