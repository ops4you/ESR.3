import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;

public class Router {
    private Collection<InetAddress> adresses;
    private InetAddress serveradr;
    private static int networkport = 25000;
    private static int underport = 25001;
    private DatagramSocket socket;
    private DatagramPacket packet;
    byte buf[];

    public static void main(String[] args) throws Exception {
        //main server ip is arg1
        String serverip;
        //serverip = args[1];
        serverip = "10.0.12.10";
        new Router(serverip);
    }

    public Router(String server) throws Exception{
        // initiates network handler
        this.serveradr = InetAddress.getByName(server);
        adresses = new HashSet<InetAddress>();
        new RouterNetworkHandler(adresses,underport, serveradr);
        buf = new byte[15000];// needs to be enough for a hole frame
        socket = new DatagramSocket(networkport);
        packet = new DatagramPacket(buf, buf.length);
        //listens for connections on port 25000 and relays them to every adress in the adresses collection, (foreach loop)
        socket.receive(packet);

        //this means a requsest from client, receives in 25000 and sends to 25001
        if (isRequest(packet)) {
            System.out.println("got a request from:" + packet.getAddress().getHostAddress() );
            byte[] buf2 = new byte[256];
            buf2 = ("rqst:"+packet.getAddress().getHostAddress()).getBytes();
            DatagramPacket newptk = new DatagramPacket(buf2, buf2.length,serveradr,underport );
            socket.send(newptk);
        }
        // this means its just a package to relay
        else{
            System.out.println("relay packet");
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
