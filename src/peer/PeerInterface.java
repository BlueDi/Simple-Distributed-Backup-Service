package peer;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface PeerInterface extends Remote{
	 String sayHello() throws RemoteException;
	 void execute(String peer_ap, String operation, String filePath, String replicationDegree) throws RemoteException;
}
