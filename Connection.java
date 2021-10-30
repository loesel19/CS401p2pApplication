// The connection Thread is spawned from the ServerSocketHandler class for every new Client connections. Responsibilities for this thread are to hnadle client specific actions like requesting file, registering to server, and client wants to quit.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class Connection extends Thread
{
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
    

    public Connection(Socket socket, ArrayList<Connection> connectionList) throws IOException
    {
        this.connectionList=connectionList;
        this.socket=socket;
        this.outputStream=new ObjectOutputStream(socket.getOutputStream());
        this.inputStream=new ObjectInputStream(socket.getInputStream());
        this.peerIP=socket.getInetAddress();
        this.peerPort=socket.getPort();
        
    }

    @Override
    public void run() {
        //wait for register packet.
        // once received, listen for packets with client requests.
        Packet p;
        System.out.println("Connection is listening for user packets");
        while (!closing){
            try { 
                
                p = (Packet) inputStream.readObject();
                System.out.println("packet has been received!");
                p.printPacket();
                //send p to event handler
                eventHandler(p);
                System.out.println(this.toString());


            }catch (SocketException e){
                System.out.println("Client " + peerID + " disconnected unexpectedly!");
                break;
            } catch (Exception e) {break;}

        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String toString(){
        String str;
        str = String.format("%s\n%s\n%s","--------------------", "Peer ID : " + this.peerID, "FILE_VECTOR : " + String.valueOf(this.FILE_VECTOR));
        return str;
    }

   

   

    public void eventHandler(Packet p) throws UnknownHostException {
        int event_type = p.event_type;
        switch (event_type)
        {
            case 0: //client register
                register(p);
            break;
            
            case 1: // client is requesting a file 
            break;

            case 5: // client wants to quit
                System.out.println( peerID +" at " + peerIP.toString() + " wishes to quit");
                connectionList.remove(this);
                this.closing = true;
            break;
           
        };
    }
    
    //other methods go here

    /**
     * this method takes in the packet that we want to register, and does such. Registering a Client will consist of
     * setting the file vector, peer listen port,  and peerID, since the other parameters are initialized in the
     * connection constructor
     * @param p
     */
    public void register(Packet p) {
        this.FILE_VECTOR = p.FILE_VECTOR;
        this.peer_listen_port = p.peer_listen_port;
        //idk if peer id is supposed to be the id of the sender of the packet or who the packet is going to.
        this.peerID = p.sender;
        System.out.println("Registered user: " + this.peerID);
    }


    void send(Packet p) throws IOException {
        outputStream.writeObject(p);
    }
}
