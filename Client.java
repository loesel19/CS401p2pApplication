// The client class will implement the functions listed in the project description.

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    int serverPort;
    InetAddress ip = null;
    Socket s;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;
    int peerID;
    int peer_listen_port;
    char FILE_VECTOR[];
    Scanner sc;

    public Client(String ip, String filepath) throws IOException {
        String[] initData = parseFile(filepath);
        System.out.println("CLIENT DATA LOADED ... ");
        System.out.println(initData[0] + ": " + initData[1] + ": " + initData[2] + ": " + initData[3]);
        //id
        this.peerID = Integer.parseInt(initData[0]);
        //server port
        this.serverPort = Integer.parseInt(initData[1]);
        //peer listen port
        this.peer_listen_port = Integer.parseInt(initData[2]);
        //socket
        this.s = new Socket(ip, this.serverPort);
        //i/o streams
        this.outputStream = new ObjectOutputStream(s.getOutputStream());
        this.inputStream = new ObjectInputStream(s.getInputStream());
        //for the inetadress type we say getByAddress for domain names, and getByName for IP address
        this.ip = InetAddress.getByName(ip);
        //file vector
        this.FILE_VECTOR = initData[3].toCharArray();


    }

    public static void main(String args[]) throws IOException, InterruptedException {
        //TODO: implement command line arguments

        // parse client config and server ip.
        String connectionAddress = "localhost";
        // create client object and connect to server. If successful, print success message , otherwise quit.
        Client c = new Client(connectionAddress, "./clientconfig1.txt");
        // Once connected, send registration info, event_type=0
        //create a new packet for sending the registration info
        Packet p = new Packet(c.peerID, 0, c.s.getPort(), c.peerID, c.FILE_VECTOR, c.peer_listen_port);
        //send the packet to the server
        c.outputStream.writeObject(p);
        c.outputStream.flush();
        System.out.println("Registration information sent!");
        // start a thread to handle server responses. This class is not provided. You can create a new class called ClientPacketHandler to process these requests.
        //done! now loop for user input
        c.sc = new Scanner(System.in);
        while (true) {
            String command = c.sc.nextLine();
            if (command.equals("f")) {//send file request packet
                p = c.requestFile();
                c.outputStream.writeUnshared(p);
                c.outputStream.flush();
            } else if (command.equals("q")) {//close connection for this client
                p.event_type = 5;
                //using writeUnshared so that it will send the updated packet instead of the cached registration packet
                c.outputStream.writeUnshared(p);
                c.outputStream.flush();
                break;
            }
            // wait for user commands.
        }
        c.s.close();
    }


    // implement other methods as necessary

    /**
     * this method takes in the filepath for a client config file and returns a string array with values ID, server Port,
     * client port, file vector. since all config files will be of the same structure (for now at least), we can just
     * read in each line of the file (which will contain a key-value pair essentially), split it, and add the second value
     * to our array
     *
     * @param path file path for config file
     * @return String array with our values
     */
    public String[] parseFile(String path) throws FileNotFoundException {
        Scanner s = new Scanner(new File(path));
        String[] arr = new String[4];
        //set up for loop with condition of scanner having next line. this way its easy to index array
        for (int i = 0; s.hasNext(); i++) {
            arr[i] = s.nextLine().split(" ")[1].trim();
        }
        //return array format
        /*
        index 0: client ID
              1: server port num
              2: client port num
              3: file vector
         */
        return arr;
    }

    /**
     * @return
     */
    public Packet requestFile() {
        //TODO: check for client already having the requested file
        System.out.println("which chunk of the file would you like?");
        int requested_file_index = sc.nextInt();
        Packet p = new Packet();
        p.req_file_index = requested_file_index;
        //event type for requesting files
        p.event_type = 1;


        return p;
    }

}
