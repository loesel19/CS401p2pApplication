// The connection Thread is spawned from the ServerSocketHandler class for every new Client connections. Responsibilities for this thread are to hnadle client specific actions like requesting file, registering to server, and client wants to quit.

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

class Connection extends Thread {
    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    int peerPort;
    int peer_listen_port;
    int peerID;
    InetAddress peerIP;
    char FILE_VECTOR[];
    ArrayList<Connection> connectionList;

    boolean closing = false;


    public Connection(Socket socket, ArrayList<Connection> connectionList) throws IOException {
        this.connectionList = connectionList;
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.peerIP = socket.getInetAddress();
        this.peerPort = socket.getPort();

    }

    //TODO: handle unexpected server disconnection when waiting for packet
    @Override
    public void run() {
        //wait for register packet.
        // once received, listen for packets with client requests.
        Packet p;
        System.out.println("Connection is listening for user packets");
        while (!closing) {
            try {
                p = (Packet) inputStream.readObject();
                System.out.println("packet has been received!");
                p.printPacket();
                //send p to event handler
                eventHandler(p);
                System.out.println(this.toString());
            } catch (SocketException e) {
                System.out.println("Client " + peerID + " disconnected unexpectedly!");
                break;
            } catch (Exception e) {
                break;
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        String str;
        str = String.format("%s\n%s\n%s", "--------------------", "Peer ID : " + this.peerID, "FILE_VECTOR : " + String.valueOf(this.FILE_VECTOR));
        return str;
    }


    public void eventHandler(Packet p) throws IOException {
        int event_type = p.event_type;
        switch (event_type) {
            case 0: //client register
                register(p);
                break;
            case 1: // client is requesting a file
                getFile(p);
                break;
            case 5: // client wants to quit
                System.out.println(peerID + " at " + peerIP.toString() + " wishes to quit");
                connectionList.remove(this);
                this.closing = true;
                break;
        }
    }

    //other methods go here

    /**
     * this method takes in the packet that we want to register, and does such. Registering a Client will consist of
     * setting the file vector, peer listen port,  and peerID, since the other parameters are initialized in the
     * connection constructor
     *
     * @param p
     */
    public void register(Packet p) {
        this.FILE_VECTOR = p.FILE_VECTOR;
        this.peer_listen_port = p.peer_listen_port;
        //idk if peer id is supposed to be the id of the sender of the packet or who the packet is going to.
        this.peerID = p.sender;
        System.out.println("Registered user: " + this.peerID);
    }

    /**
     * this method takes in a file request packet, and sends back a packet with the peerID of a client that has
     * the requested file or -1 if no clients have the file. It also prints out a list of the peerIDs of all clients
     * that have the file.
     *
     * @param p The request packet from a client
     * @throws IOException
     */
    private void getFile(Packet p) throws IOException {
        System.out.println(peerID + " at " + peerIP.toString() + " is requesting file " + p.req_file_index);

        //loop through all clients and add their peerID to a list if they have the requested file
        ArrayList<Integer> clientsWithFile = new ArrayList<>();
        for (Connection connection : connectionList) {
            if (connection.FILE_VECTOR[p.req_file_index] == '1')
                clientsWithFile.add(connection.peerID);
        }
        //if no clients have the file the list will contain only -1
        if (clientsWithFile.size() == 0) {
            clientsWithFile.add(-1);
        }
        System.out.println("Clients with file: " + clientsWithFile.toString());

        //For now, just sending back the first client. Might add logic to pick the client in a later version.
        Packet response = new Packet();
        response.event_type = 2;
        response.req_file_index = p.req_file_index;
        response.peerID = clientsWithFile.get(0);
        if (response.peerID != -1) {
            for (Connection connection : connectionList) {
                if (connection.peerID == response.peerID) {
                    response.peer_listen_port = connection.peerPort;
                    break;
                }
            }
        }

        send(response);
    }

    void send(Packet p) throws IOException {
        outputStream.writeObject(p);
        outputStream.flush();
    }
}
