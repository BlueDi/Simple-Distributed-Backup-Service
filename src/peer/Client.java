package peer;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class Client {
	private static String peer_ap;
	private static String operation;
	private static String operand_1;
	private static String operand_2;
	public static void main(String[] args) throws IOException{
		if (args.length < 4) {
			System.out.println("Usage: java TestApp <peer_ap> <operation> <opnd_1> <opnd_2> ");
			return;
		}
		
		if(processInput(args))
			callOperation();
		
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
	//This function sends the arguments to a function in the peer (selected by acess point)
	private static void callOperation()
	{
		try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            //This is how we select the appropriate peer
            PeerInterface stub = (PeerInterface) registry.lookup(peer_ap);
            stub.handleOperation(operation, operand_1, operand_2);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}
	
}
