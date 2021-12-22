import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

public class Router {
    private RouterInfo adresses;
    private InetAddress serveradr;
    private static int networkport = 25001;

    public static void main(String[] args) {
        //main server ip is arg1

        Router r = new Router();
    }

    public Router(){
        // initiates network handler
        adresses = new RouterInfo();
        RouterNetworkHandler rnh = new RouterNetworkHandler(adresses,networkport);

        //listens for connections on port 25000 and relays them to every adress in the adresses collection, (foreach loop)

    }
    
}
