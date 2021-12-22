import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;

public class RouterNetworkHandler {
    RouterInfo adresses;
    int netport = 25001;// port for all netowrk comunications
    private DatagramSocket socket;
    DatagramPacket packet;
    private boolean running;
    private byte[] buf = new byte[256];
    
    
    public RouterNetworkHandler(RouterInfo adr, int netport) throws SocketException {
        this.adresses =adr; 
        this.netport = netport;
        this.socket = new DatagramSocket(netport);
        packet = new DatagramPacket(buf, buf.length);

        //poe o listen UPD a correr
        new Thread(() -> {
            listenUdp();
        }).start();

        //poe o keepAlive a correr
        new Thread(() -> {
            keepAlive();
        }).start();
    }

    //listens for udp packages
    public void listenUdp(){
        // listen for udp packets
        
        // if server ping answer with smth 

        // if its an update to the adresses update adresses wi ththe new info ( the new info overrides all previous info so all prevuous adresses need to be erased)
        // WARNING- the adresses object needs to be the same because its shares with Routers, you need to erase all adresses and put the new ones. 

    }

    public void keepAlive(){
        //every 1s +- a random amount maybe 0.5s ping the main server with a keep alive msg to show that its still responding
    }
}
