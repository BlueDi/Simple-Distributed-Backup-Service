package peer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class Client {
	private static String peer_ap;
	private static String operation;
	private static String operand_1;
	private static String operand_2;

	/**
	 * Peer initiator function
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		if (args.length < 4) {
			System.out.println("Usage: java TestApp <peer_ap> <operation> <opnd_1> <opnd_2> ");
			return;
		}
		
		if(processInput(args))
			callBackup();
		
	}

	/**
	 * Verifica se os argumentos passados são válidos.
	 * TODO: resto das verificações dos argumentos. Ainda só está feito para a operation.
	 * @param args Argumentos passados na consola
	 */
	private static boolean processInput(String[] args){
		peer_ap = args[0];
		operation = args[1];
		operand_1 = args[2];
		operand_2 = args[3];

		return ("BACKUP".equals(operation)
				|| "RESTORE".equals(operation) 
				|| "DELETE".equals(operation) 
				|| "RECLAIM".equals(operation) 
				|| "STATE".equals(operation));
	}

	private static void execute(){
		//Peer.execute(peer_ap, operation, operand_1, operand_2);
		try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            PeerInterface stub = (PeerInterface) registry.lookup("PeerInterface");
            String response = stub.sayHello();
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}
	private static void callBackup()
	{
		try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            PeerInterface stub = (PeerInterface) registry.lookup("PeerInterface");
            stub.execute(peer_ap, operation, operand_1, operand_2);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}
	
}
