import java.net.InetAddress;
import java.util.Collection;

public class RouterNetworkHandler {
    Collection<InetAddress> adresses;
    int netport;// port for all netowrk comunications
    
    public RouterNetworkHandler(Collection<InetAddress> adr, int netport) {
        this.adresses =adr; 
        this.netport = netport;
    }

    //listens for udp packages
    public void listenUdp(){
        // listen for udp packets
        
        // if server ping answer with smth 

        // if its an update to the adresses update adresses wi ththe new info ( the new info overrides all previous info so all prevuous adresses need to be erased)
        // WARNING- the adresses collection needs to be the same because its shares with Routers, you need to erase all adresses and put the new ones. 

    }

    public void keepAlive(){
        //every 1s +- a random amount maybe 0.5s ping the main server with a keep alive msg to show that its still responding
    }
}
