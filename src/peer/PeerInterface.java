package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
	void handleOperation(String operation, String filePath, String replicationDegree) throws RemoteException;
}
