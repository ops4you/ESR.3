import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;

public class RouterNetworkHandler {
    Collection<InetAddress> adresses;
    int netport = 25001;// port for all netowrk comunications
    private DatagramSocket socket;
    DatagramPacket packet;
    private byte[] buf = new byte[256];
    InetAddress server;

    public RouterNetworkHandler(Collection<InetAddress> adr, int netport, InetAddress serveradr) throws Exception {
        this.adresses = adr;
        this.netport = netport;
        server = serveradr;
        this.socket = new DatagramSocket(netport);
        packet = new DatagramPacket(buf, buf.length);

        // thingy to shutdown safely
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                byte[] buf2 = new byte[256];
                buf2 = "stop:".getBytes();
                DatagramPacket newptk = new DatagramPacket(buf2, buf2.length, server,netport);
                try {
                    socket.send(newptk);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        // poe o listen UPD a correr
        new Thread(() -> {
            try {
                listenUdp();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();

        // poe o keepAlive a correr
        new Thread(() -> {
            try {
                keepAlive();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }

    // listens for udp packages
    public void listenUdp() throws Exception {
        // listen for udp packets
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        System.out.println("Listening on: " + socket.getLocalPort());
        socket.receive(packet);
        System.out.println("Received packet from:" + packet.getAddress().getHostName());
        // if server ping answer with smth
        if (isNewIp(packet)) {
            // gets the ip as a string
            String ip = (new String(packet.getData())).split("send:")[1];
            adresses.add(InetAddress.getByName(ip));
        } else if (isErase(packet)) {
            this.adresses.clear();
        } else {
            System.out.print("received a packet and dont know what it is");
        }
        // if its an update to the adresses update adresses wi ththe new info ( the new
        // info overrides all previous info so all prevuous adresses need to be erased)
        // WARNING- the adresses object needs to be the same because its shares with
        // Routers, you need to erase all adresses and put the new ones.

    }

    public boolean isNewIp(DatagramPacket pkt) {
        byte[] data = pkt.getData();
        return (new String(truncate(data, 5))).compareTo("send:") == 0;
    }

    public boolean isErase(DatagramPacket pkt) {
        byte[] data = pkt.getData();
        return (new String(truncate(data, 5))).compareTo("erase") == 0;
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

    public void keepAlive() throws Exception {
        // every 1s +- a random amount maybe 0.5s ping the main server with a keep alive
        // msg to show that its still responding
        while (true) {
            byte[] buf2 = new byte[256];
            buf2 = "ping:".getBytes();
            DatagramPacket newptk = new DatagramPacket(buf2, buf2.length, this.server, netport);
            // System.out.println("sending ping to:" + newptk.getPort() + " " +
            // newptk.getAddress().getHostAddress());
            socket.send(newptk);
            int time = 500 + ((int) (Math.random() * (500)));
            // System.out.println("Sent a keep alive ping to main server, next one in:" +
            // time);

            Thread.sleep(time);
        }
    }
}
