import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientPacketHandler extends Thread {
    Client c;
    Socket s;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;

    public ClientPacketHandler(Client c, Socket s, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException, ClassNotFoundException {
        this.c = c;
        this.s = s;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        Packet p;
        while (true) {
            try {
                if (!s.isClosed()) {
                    p = (Packet) inputStream.readObject();
                    eventHandler(p);
                }else{
                    break;
                }

            } catch (Exception e) {
                System.out.println("leaving client packet handler loop!");
                try {
                    s.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                break;
            }
        }
    }

    public void eventHandler(Packet p) throws IOException {
        if (p.event_type == 2) {            //Server replying to file request
            if (p.peerID == -1) {
                System.out.println("Server says that no client has file " + p.req_file_index);

            } else {
                System.out.printf("Server says that peer %d on listening port %d has file %d%n", p.peerID, p.peer_listen_port, p.req_file_index);
            }
        }else if (p.event_type == 5){       //client is quitting
            s.close();
        }
        else if (p.event_type == 6) {     //Server is quitting
            System.out.println("Server is closing. Shutting down...");
            System.out.println("scanner closed");
        }
    }
}
