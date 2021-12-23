import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.crypto.Data;

public class Router {
    private Collection<InetAddress> adresses;
    private InetAddress serveradr;
    private static int networkport = 25000;
    private DatagramSocket socket;
    private DatagramPacket packet;
    byte buf[];

    public static void main(String[] args) throws Exception {
        //main server ip is arg1
        Router r = new Router();
    }

    public Router() throws Exception{
        // initiates network handler
        adresses = new ArrayList<InetAddress>();
        RouterNetworkHandler rnh = new RouterNetworkHandler(adresses,networkport, serveradr);
        buf = new byte[15000];// needs to be enough for a hole frame
        socket = new DatagramSocket(networkport);
        packet = new DatagramPacket(buf, buf.length);
        //listens for connections on port 25000 and relays them to every adress in the adresses collection, (foreach loop)
        socket.receive(packet);

        //this means a requsest from client, receives in 25000 and sends to 25001
        if (isRequest(packet)) {
            byte[] buf2 = new byte[256];
            buf2 = ("rqst:"+packet.getAddress().getHostAddress()).getBytes();
            DatagramPacket newptk = new DatagramPacket(buf2, buf2.length,serveradr,networkport+1);
            socket.send(newptk);
        }
        // this means its just a package to relay
        else{
            for (InetAddress inetAddress : adresses) {
                packet.setAddress(inetAddress);
                packet.setPort(networkport);
                socket.send(packet);
            }
        }

    }

    //chest if its a clients wanting a request or just a packat to relay
    public boolean isRequest(DatagramPacket pkt){
        byte[] data = pkt.getData();
        return (new String(truncate(data,5))).compareTo("rqst:")==0;
    }

    public static byte[] truncate(byte[] array, int newLength) {
        if (array.length < newLength) {
            return array;
        } else {
            byte[] truncated = new byte[newLength];
            System.arraycopy(array, 0, truncated, 0, newLength);

            return truncated;
        }
    }
}
