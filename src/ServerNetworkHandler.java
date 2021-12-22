import java.io.FileNotFoundException;
import java.net.DatagramSocket;

public class ServerNetworkHandler implements Runnable {
    private int[][] matrix;
    private long[] contacts; //date in milisecconds since last contact
    private String[] ips;
    private DatagramSocket socket;
    private int netport = 25001;
    static String filename = "src/network.txt";

    private RouterInfo clients;
    // saves server statate
    // sends the new routing tables to the routers
    // updates the client list
    public ServerNetworkHandler(RouterInfo c){
        //the clients class is shared by both runnig threads, the main one and this "run". this is how there 2 thread "comunicate"
        this.clients = c;
        try {
            matrix = Parser.parsegraph(filename);
            ips = Parser.parseips(filename);
        } catch (FileNotFoundException e) {
            System.out.println("/src/network file does not exist");
            e.printStackTrace();
        }
    }

    public void run() {
        //do server stuff
        netPing();

        //call listen upd assync
        new Thread(() -> {
            listenForUdp();
        }).start();
       
        //checks if matrix needs updating
        new Thread(() -> {
            updateAlive();
        }).start();
        

    }

    void netPing(){
        //sends a ping to every router
    }

    void listenForUdp(){
        //loop to w8 upd packets
        while(true){
            //w8 for packet

            //if request  (both play and stop)
                // update the client list
                // recalculate paths
                // update the routers on what they should do
            //if keepalive
                // update the matrix to show that they are alive
        }
    }

    void updateAlive(){
        //checks date matrix for routers that arent calling back and removes them rom curretn pool,
        // if any are removed also updates matrix
    }
}
