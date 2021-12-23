import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ServerNetworkHandler implements Runnable {
    private int[][] matrix;// represents every possible network node  0= no conecttion; 1 means conection; 2 means should be conection but rn is dead
    private long[] contacts; //date in milisecconds since last contact
    private String[] ips; // ips for all routers
    private DatagramSocket socket;
    private int netport = 25001;
    static String filename = "src/network.txt";
    static long maxdiff = 2000;

    private RouterInfo clients; //RouterInfo is a bad name, this stores all the Clients's info 

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
            try {
                listenForUdp();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("Error with the inner network socket");
                e.printStackTrace();
            }
        }).start();
       
        //checks if matrix needs updating
        new Thread(() -> {
            try {
                updateAlive();
            } catch (Exception e) {
                System.out.println("idk what happened");
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

    void listenForUdp() throws Exception{
        //loop to w8 upd packets
        while(true){
            //w8 for packet
            byte[] buf =  new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String data = new String(packet.getData());
            //if keepalive
                // update the matrix to show that they are alive
            if(data.contentEquals("ping")){
                String ip =packet.getAddress().getHostAddress();
                int i=0;
                for (; i < ips.length; i++) {
                    if(ips[i].contentEquals(ip)){
                        contacts[i]=System.currentTimeMillis();
                    }
                }
                updateAlive();
            }
            
            //if request  (both play and stop)
                // update the client list
                // recalculate paths
                // update the routers on what they should do
        }
    }

    void updateAlive() throws Exception{
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
                        // if connection doewsnt exist its stays 0, if exists goes to 2 signaling that its not fucntional rn
                        matrix[count][j]*=2;
                        matrix[j][count]*=2;
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
    void calcPath() throws Exception{
        int routs[][] = new int[clients.size()][];
        int i=0;
        for (ClientInfo c : clients.getClients()) {
            routs[i] = Dijkstra.printPathInt(Dijkstra.dijkstra(matrix, 0, c.router));
            i++;
        }
        int k=0;
        //ressets the server's own forwarding adresses
        clients.rmAllAdr();
        for (int[] route : routs) {

            //loop for each node in a specific route
            for (int j = 0; j < route.length; j++) {
                //send a packet saying "send ur stuff to the ip of the client corresponding to route[j+1]
                
                //j==0 so we need to update the servers own forwarding
                if (j==0) {
                    clients.addAdress(ips[1]);
                }                
                // ifi j=route.length-1 (last element in the route) insteds tells to send direcly to client
                else if(j==route.length-1){
                    byte[] buf = ("send:"+clients.getClients().get(k).adress).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ips[j]) ,netport);
                    socket.send(packet);
                }
                else{
                    byte[] buf = ("send:"+ips[j+1]).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ips[j]) ,netport);
                    socket.send(packet);
                }
            }
            k++;
        }
    }
}
