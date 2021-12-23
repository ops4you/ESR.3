import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RouterInfo {
    private List<ClientInfo> clients;
    final Lock l = new ReentrantLock();
    //by being a set naturaly repeated ips are not counted
    Set<InetAddress> adresses;// adresses that the server needs to send theyr packets to
    
    public RouterInfo(){
        clients = new ArrayList<ClientInfo>();
        adresses = new HashSet<InetAddress>();
    }

    public int size(){
        l.lock();
        try {
            return clients.size();
        } finally {
            l.unlock();
        }
    }

    public List<ClientInfo> getClients(){
        l.lock();
        try {
            return new ArrayList<>(clients);
        } finally {
            l.unlock();
        }
    }

    public void addClient(ClientInfo adr){
        l.lock();
        try {
            clients.add(adr);  
        } finally{
            l.unlock();
        }
    }

    //this may not work but we'll see
    @SuppressWarnings("all")
    public void rmClient(InetAddress adr){
        l.lock();
        try {
            clients.remove(adr);  
        } finally{
            l.unlock();
        }
    }

    public void rmAll(){
        for (ClientInfo inetAddress : this.clients) {
            this.clients.remove(inetAddress);
        }
    }

    public Set<InetAddress> getAdresses(){
        l.lock();
        try {
            return new HashSet<>(adresses);
        } finally {
            l.unlock();
        }
    }

    public void addAdress(InetAddress adr){
        l.lock();
        try {
            adresses.add(adr);  
        } finally{
            l.unlock();
        }
    }
    
    public void addAdress(String adr) throws Exception{
        l.lock();
        try {
            adresses.add(InetAddress.getByName(adr) );  
        } finally{
            l.unlock();
        }
    }
    //this may not work but we'll see
    public void rmAdress(InetAddress adr){
        l.lock();
        try {
            adresses.remove(adr);  
        } finally{
            l.unlock();
        }
    }

    public void rmAllAdr(){
        this.adresses = new HashSet<InetAddress>();
    }

    
}
