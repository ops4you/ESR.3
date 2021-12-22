import java.net.InetAddress;

public class ClientInfo {
    InetAddress adress;
    int router; // closest router
    public ClientInfo(InetAddress adr, int router){
       this.adress = adr;
       this.router = router;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this.adress.equals(((ClientInfo) obj).adress );
    }
}
