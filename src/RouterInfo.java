import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RouterInfo {
    private List<ClientInfo> clients;
    final Lock l = new ReentrantLock();
    List<InetAddress> adresses;
    
    public RouterInfo(){
        clients = new ArrayList<ClientInfo>();
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

    public List<InetAddress> getAdresses(){
        l.lock();
        try {
            return new ArrayList<>(adresses);
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
        this.adresses = new ArrayList<InetAddress>();
    }

    
}
