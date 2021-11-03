import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientPacketHandler extends Thread {
    Socket s;
    ObjectInputStream inputStream;

    public ClientPacketHandler(Socket s, ObjectInputStream inputStream) {
        this.s = s;
        this.inputStream = inputStream;
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

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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
        } else if (p.event_type == 6) {     //Server is quitting
            System.out.println("client " + p.sender + " is closing");
            s.close();
        }
    }
}
