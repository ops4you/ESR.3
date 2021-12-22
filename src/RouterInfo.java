import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RouterInfo {
    private List<ClientInfo> clients;
    final Lock l = new ReentrantLock();
    
    public RouterInfo(){
        clients = new ArrayList<ClientInfo>();
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
}
