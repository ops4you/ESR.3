import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientInfo {
    private List<InetAddress> adress;
    final Lock l = new ReentrantLock();
    
    public ClientInfo(){
        adress = new ArrayList<InetAddress>();
    }

    public List<InetAddress> getClients(){
        l.lock();
        try {
            return new ArrayList<>(adress);
        } finally {
            l.unlock();
        }
    }

    public void addClient(InetAddress adr){
        l.lock();
        try {
            adress.add(adr);  
        } finally{
            l.unlock();
        }
    }

    public void rmClient(InetAddress adr){
        l.lock();
        try {
            adress.remove(adr);  
        } finally{
            l.unlock();
        }
    }
}
