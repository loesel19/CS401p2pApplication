// The client class will implement the functions listed in the project description.

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

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

    AtomicBoolean closing = new AtomicBoolean(false);

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
        System.out.println("Attempting to connect ...");
        //loop in case server hasn't started yet
        while (true) {
            try {
                //if it successfully connects break out of the loop
                this.s = new Socket(ip, this.serverPort);
                break;
            } catch (ConnectException e) {
                //Gets thrown when attempting to connect to a server that hasn't been started yet,
                //  or isn't accepting connections
            } catch (IOException e) {
                System.out.println("Error: Could not connect");
                //escalate so that main doesn't continue without the socket connected
                throw e;
            }
        }
        System.out.println("Connected to server!");
        //i/o streams
        this.outputStream = new ObjectOutputStream(s.getOutputStream());
        this.inputStream = new ObjectInputStream(s.getInputStream());
        //for the inetadress type we say getByAddress for domain names, and getByName for IP address
        this.ip = InetAddress.getByName(ip);
        //file vector
        this.FILE_VECTOR = initData[3].toCharArray();


    }

    public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException {

        Client c = initializeClient(args);
        if (c == null) {
            System.out.println("Something went wrong, unable to create client");
            return;
        }
        // Once connected, send registration info, event_type=0
        //create a new packet for sending the registration info
        Packet p = new Packet(c.peerID, 0, c.s.getPort(), c.peerID, c.FILE_VECTOR, c.peer_listen_port);
        //send the packet to the server
        c.outputStream.writeObject(p);
        c.outputStream.flush();
        System.out.println("Registration information sent!");
        // start a thread to handle server responses. This class is not provided. You can create a new class called ClientPacketHandler to process these requests.
        ClientPacketHandler handler = new ClientPacketHandler(c, c.s, c.inputStream, c.outputStream);
        handler.start();

        //done! now loop for user input
        c.sc = new Scanner(System.in);
        String command = "";

        while (!c.closing.get()) {
            try {
                if (c.s.isClosed())
                    break;
                if (c.sc == null)
                    break;
                if (c.sc.hasNextLine()) {
                    command = c.sc.nextLine();
                    if (command.equals("f")) {//send file request packet
                        p = c.requestFile();
                        if (p == null)  //The user already has the file
                            continue;
                        c.outputStream.writeUnshared(p);
                        c.outputStream.flush();
                    } else if (command.equals("q")) {//close connection for this client
                        c.quit();
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
        c.sc.close();
        c.s.close();
        System.out.println("out of client loop");
    }


    // implement other methods as necessary

    /**
     * this method will be used to initialize new clients, by checking the command line arguments to see if there is a
     * config file.
     *
     * @return the new Client object.
     */
    public static Client initializeClient(String args[]) throws IOException, ClassNotFoundException {
        // parse client config and server ip.
        String connectionAddress = "localhost";
        // create client object and connect to server. If successful, print success message , otherwise quit.
        Client c;
        //see if args were passed in when file was ran
        String filePath = "";
        //create
        if (args.length > 0 && args[0] != "") {
            if (args[0].equals("-c")) {
                filePath = args[1];
                //System.out.println(filePath);
            } else {
                //not setting up a config file, some other arguments were passed in, but we dont need to worry about that
            }
            try {
                //just a quick check to see if .txt is in the filePath
                if (!filePath.contains(".txt"))
                    filePath = filePath + ".txt";

                c = new Client(connectionAddress, filePath);
            } catch (IOException e) {
                return null;
            }
        } else {//default client config, just set it to config1
            c = new Client(connectionAddress, "./clientconfig1.txt");
        }
        return c;

    }

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
        System.out.println("which chunk of the file would you like?");
        int requested_file_index = sc.nextInt();

        if (requested_file_index < FILE_VECTOR.length && requested_file_index >= 0) {
            if (FILE_VECTOR[requested_file_index] == '1') {
                System.out.println("I already have this file block!");
                return null;
            }
        }
        System.out.println("I don't have this file. Let me contact server...");

        Packet p = new Packet();
        p.req_file_index = requested_file_index;
        //event type for requesting files
        p.event_type = 1;

        return p;
    }

    public void quit() {
        Packet closingPacket = new Packet(peerID, 5, s.getPort(), -1, FILE_VECTOR, -1);
        try {
            outputStream.writeUnshared(closingPacket);
            outputStream.flush();
        } catch (IOException e) {
        }

        try {
            closing.set(true);
            s.close();

            //this is not pretty, but short of spoofing console input I have no idea how else to get
            //  it to exit when it was still expecting scanner input :|
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
