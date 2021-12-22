import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.Port;

public class ServerNetworkHandler implements Runnable {
    private int[][] matrix;
    private long[] contacts; //date in milisecconds since last contact
    private String[] ips; // ips for all routers
    private DatagramSocket socket;
    private int netport = 25001;
    static String filename = "src/network.txt";
    static long maxdiff = 2000;

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
        try {
            socket = new DatagramSocket();
        } catch (SocketException e2) {
            // TODO Auto-generated catch block
            System.out.println("datagram socket creating failed");
            e2.printStackTrace();
        }
        try {
            netPing();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            System.out.println("invalid inet adress OR error sending the packet");
            e1.printStackTrace();
        }

        //call listen upd assync
        new Thread(() -> {
            listenForUdp();
        }).start();
       
        //checks if matrix needs updating
        new Thread(() -> {
            try {
                updateAlive();
            } catch (InterruptedException e) {
                System.out.println("sleep interrupted");
                e.printStackTrace();
            }
        }).start();
    }

    void netPing() throws IOException{
        //sends a ping to every router
        byte[] buf = "ping".getBytes();

        for (String l : ips) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length,InetAddress.getByName(l),netport ); 
            socket.send(packet);
        }

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

    void updateAlive() throws InterruptedException{
        //checks date matrix for routers that arent calling back and removes them rom curretn pool, 
        // if any are removed also updates matrix
        // if a router is offline, sends a ping to check if its up
        while (true) {
            long rn = System.currentTimeMillis();
            int updateflag =0;
            for (long l : this.contacts) {
                if(l-rn <= maxdiff){
                    updateflag++;
                    int count =0;
                    for(int j=0 ; j<= matrix.length; j++){
                        matrix[count][j]=0;
                        matrix[j][count]=0;
                    }
                count++;
                }
            }
            if (updateflag !=0) {
                calcPath();
            }
            Thread.sleep(1000);
        }
        
    }

    //pathfinding for the packets
    // calculates the route for all clients with the client mattrix graph
    // sets up all the diferent paths, 1 for each client
    // sends each router theyr new "forwarding table" witch is a list of clients that they need to resend theyr packages too
    // b4 sending an ip to a router checks if ip is already eing sent, this is the way that we stop multiple packages in the same route.
    // also updates the "routerinfo" as to trully update the server's own routes (they shouls just be the 2nd node on all calculated routes (1st is the server, 2n is the next node))
    void calcPath(){
        
    }
}
