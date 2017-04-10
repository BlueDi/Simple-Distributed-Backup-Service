package peer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	private static String peer_ap;
	private static String operation;
	private static String operand_1;
	private static String operand_2;

	/**
	 * This function sends the arguments to a function in the peer (selected by
	 * acess point). The appropriate peer is selected with
	 * registry.lookup(peer_ap).
	 */
	private static void callOperation() {
		String stateString;
		Registry registry;

		try {
			registry = LocateRegistry.getRegistry();
			PeerInterface stub = (PeerInterface) registry.lookup(peer_ap);
			if(operation.equals("STATE"))
			{
				stateString = stub.printState();
				System.out.println(stateString);
			}
			else stub.handleOperation(operation, operand_1, operand_2);
		} catch (RemoteException e) {
			System.err.println(
					"Error in Client.callOperation(). Reference could not be created or communication with the registry failed.");
		} catch (NotBoundException e) {
			System.err.println(
					"Error in Client.callOperation(). Registry tried to lookup but the name is not currently bounded.");
		}
	}

	/**
	 * Verifica se os argumentos passados são válidos. TODO: resto das
	 * verificações dos argumentos. Ainda só está feito para a operation.
	 * 
	 * @param args
	 *            Argumentos passados na consola
	 */
	private static boolean processInput(String[] args) {
		peer_ap = args[0];
		operation = args[1];
		operand_1 = args[2];
		operand_2 = args[3];

		return ("BACKUP".equals(operation) || "RESTORE".equals(operation) || "DELETE".equals(operation)
				|| "RECLAIM".equals(operation) || "STATE".equals(operation));
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java TestApp <peer_ap> <operation>");
			System.err.println("Usage: java TestApp <peer_ap> <operation> <opnd_1>");
			System.err.println("Usage: java TestApp <peer_ap> <operation> <opnd_1> <opnd_2>");
			System.exit(1);
		}
	
			
		if (processInput(args))
			callOperation();
	}

}
