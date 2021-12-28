import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class ServerNetworkHandler implements Runnable {
    private int[][] matrix;// represents every possible network node 0= no conecttion; 1 means conection
                           // [x][x] positions indicate if the node itself is ofline(0) or online(1)
                           // means should be conection but rn is dead
    private long[] contacts; // date in milisecconds since last contact
    private String[] ips; // ips for all routers
    private DatagramSocket socket;
    private int netport = 25001;
    static String filename = "network.txt";
    static long maxdiff = 2000;

    private RouterInfo clients; // RouterInfo is a bad name, this stores all the Clients's info

    // saves server statate
    // sends the new routing tables to the routers
    // updates the client list
    public ServerNetworkHandler(RouterInfo c) {
        // the clients class is shared by both runnig threads, the main one and this
        // "run". this is how there 2 thread "comunicate"
        this.clients = c;
        try {
            System.out.println("networkhandler Initiated");
            matrix = Parser.parsegraph(filename);
            ips = Parser.parseips(filename);
            this.contacts = new long[ips.length];
        } catch (FileNotFoundException e) {
            System.out.println("/src/network file does not exist");
            e.printStackTrace();
        }
    }

    public void run() {
        // do server stuff
        try {
            socket = new DatagramSocket(this.netport);
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

        // call listen upd assync
        new Thread(() -> {
            try {
                listenForUdp();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println("Error with the inner network socket");
                e.printStackTrace();
            }
        }).start();

        // checks if matrix needs updating
        new Thread(() -> {
            try {
                updateAliveLoop();
            } catch (Exception e) {
                System.out.println("idk what happened");
                e.printStackTrace();
            }
        }).start();
    }

    void netPing() throws IOException {
        // sends a ping to every router
        byte[] buf = "ping".getBytes();

        for (String l : ips) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(l), netport);
            socket.send(packet);
        }

    }

    void listenForUdp() throws Exception {
        // loop to w8 upd packets
        while (true) {
            // w8 for packet
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            System.out.println("Listening on: " + socket.getLocalPort());
            socket.receive(packet);
            // if keepalive
            // update the matrix to show that they are alive
            if (isPing(packet)) {
                System.out.println("got a ping!");
                String ip = packet.getAddress().getHostAddress();
                int i = 0;
                for (; i < ips.length; i++) {
                    if (ips[i].contentEquals(ip)) {
                        contacts[i] = System.currentTimeMillis();
                    }
                }
                updateAlive();
            } else if (isRqst(packet)) {
                System.out.println("got a request!");
                int router = 0; // value by default as to not give an error
                for (int i = 0; i < ips.length; i++) {
                    if (packet.getAddress().equals(InetAddress.getByName(ips[i]))) {
                        router = i;
                    }
                }
                InetAddress a = InetAddress.getByName((new String(packet.getData())).split("rqst:")[1]);// loads the
                                                                                                        // clients ip
                ClientInfo c = new ClientInfo(a, router);
                clients.addClient(c);
                updateAlive();
            } else if (isStop(packet)) {
                System.out.println("got a Stop!");
                InetAddress a = InetAddress.getByName((new String(packet.getData())).split("stop:")[1]);
                clients.rmClient(a);
                updateAlive();
            } else {
                System.out.println("got an unrecognizd packet\n" + new String(packet.getData()));
            }
        }
    }

    void updateAliveLoop() throws Exception {
        while (true) {
            updateAlive();
            Thread.sleep(1000);
        }
    }

    void updateAlive() throws Exception {
        // checks date matrix for routers that arent calling back and removes them rom
        // curretn pool,
        // if any are removed also updates matrix
        // if a router is offline, sends a ping to check if its up
        long rn = System.currentTimeMillis();
        int updateflag = 0;
        int count = 0;// basicly the "i" for the 1st for loop
        //for (long l : this.contacts) {
        //System.out.println("matrix:");
        //printMatrix(matrix);
        for (count = 1 ; count <this.contacts.length ; count++){
            long l = this.contacts[count];
            //timing ou
            //System.out.println("rn:" + rn +" l: " +l + "result"+ (rn-l));

            if (rn - l >= maxdiff) {

                if (matrix[count][count] == 1) {
                    // only updates flag if it changes from 1 to 0
                    matrix[count][count] = 0;
                    updateflag++;
                    //System.out.println("Timmed out the router " + count);
                }
                // making available
            } else  {
                if (matrix[count][count] == 0) {
                    // only updates flag if it changes from more than 1 to one
                    System.out.println("Made router available: "+ count);
                    updateflag++;
                    matrix[count][count] = 1;
                }
            }
        }
        if (updateflag != 0) {
            calcPath();
        }

    }

    // pathfinding for the packets
    // calculates the route for all clients with the client mattrix graph
    // sets up all the diferent paths, 1 for each client
    // sends each router theyr new "forwarding table" witch is a list of clients
    // that they need to resend theyr packages too
    // b4 sending an ip to a router checks if ip is already eing sent, this is the
    // way that we stop multiple packages in the same route.
    // also updates the "routerinfo" as to trully update the server's own routes
    // (they shouls just be the 2nd node on all calculated routes (1st is the
    // server, 2n is the next node))
    void calcPath() throws Exception {
        int routs[][] = new int[clients.size()][];
        int i = 0;
        int[][] m = prepMatrix(matrix);
        for (ClientInfo c : clients.getClients()) {
            routs[i] = Dijkstra.printPathInt(Dijkstra.dijkstra(m, 0, c.router));
            i++;
        }
        int k = 0;
        // ressets the server's own forwarding adresses
        clients.rmAllAdr();
        System.out.println("new paths calculated");
        for (int[] route : routs) {
            System.out.println("now handling route nr " + i + ": " + Arrays.toString(route));
            // loop for each node in a specific route
            for (int j = 0; j < route.length; j++) {
                // send a packet saying "send ur stuff to the ip of the client corresponding to
                // route[j+1]

                // j==0 so we need to update the servers own forwarding
                if (j == 0) {
                    clients.addAdress(ips[1]);
                }
                // ifi j=route.length-1 (last element in the route) insteds tells to send
                // direcly to client
                else if (j == route.length - 1) {
                    byte[] buf = ("send:" + clients.getClients().get(k).adress).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ips[j]), netport);
                    socket.send(packet);
                } else {
                    byte[] buf = ("send:" + ips[j + 1]).getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ips[j]), netport);
                    socket.send(packet);
                }
            }
            k++;
        }
    }

    public int[][] prepMatrix(int[][] matrix) {
        int[][] m = new int[matrix.length][matrix.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
               m[i][j]=matrix[i][j];
            }
        }        

        for (int i = 0; i < m.length - 1; i++) {
            // node is online
            // checks if every self connection is good
            if (m[i][i] == 1) {
                // if the node is already online no need to do anything
                /*
                 * for (int j = i + 1; j < m.length; j++) {
                 * 
                 * // if there is a connection to a node not online
                 * if (m[i][j] == 1 && m[j][j] == 0) {
                 * m[i][j]=0;//close connection
                 * for (int k = i + 1; k < m.length; k++) {
                 * m[i][k] = intor(m[j][k],m[i][k]);
                 * m[k][i] = intor(m[j][k],m[i][k]);
                 * }
                 * }
                 * }
                 */
            }

            // node not online, need to fix future and back conections
            else {
                // for every connection back
                for (int j = 0; j < i; j++) {
                    // checks if connected
                    if (m[i][j] != 0 && i != j) {
                        // for ecery connection front
                        for (int k = i + 1; k < m.length; k++) {
                            // checks if connected
                            if (m[i][k] != 0) {
                                // makes the connection between node back and node front
                                m[j][k] = 1;
                                m[k][j] = 1;
                            }
                        }
                        m[i][j] = 0;
                        m[j][i] = 0;
                    }
                }
            }
        }
        for (int i = 0; i < m.length; i++) {
            // remove self connection because we cant have that for pathfinding purposes
            m[i][i] = 0;
        }
        return m;
    }

    public int intor(int a, int b) {
        return a != 0 && b != 0 ? 0 : 1;
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

    public boolean isRqst(DatagramPacket pkt) {
        byte[] data = pkt.getData();
        return (new String(truncate(data, 5))).compareTo("rqst:") == 0;
    }

    public boolean isStop(DatagramPacket pkt) {
        byte[] data = pkt.getData();
        return (new String(truncate(data, 5))).compareTo("stop:") == 0;
    }

    public boolean isPing(DatagramPacket pkt) {
        byte[] data = pkt.getData();
        return (new String(truncate(data, 5))).compareTo("ping:") == 0;
    }

    public void printMatrix(int[][] m){
        for (int[] is : m) {
            for (int i : is) {
                System.out.print(i+" ");
            }
            System.out.println("");
        }
    }
}