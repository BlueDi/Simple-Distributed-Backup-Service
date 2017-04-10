package peer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
	private static String peer_ap;
	private static String operation;
	private static String operand_1 = "";
	private static String operand_2 = "";

	/**
	 * This function sends the arguments to a function in the peer (selected by
	 * acess point). The appropriate peer is selected with
	 * registry.lookup(peer_ap).
	 */
	private static void callOperation() {
		Registry registry;

		try {
			registry = LocateRegistry.getRegistry();
			PeerInterface stub = (PeerInterface) registry.lookup(peer_ap);

			if ("STATE".equals(operation))
				System.out.println(stub.operationState());
			else
				stub.handleOperation(operation, operand_1, operand_2);
		} catch (RemoteException e) {
			System.err.println(
					"Error in Client.callOperation(). Reference could not be created or communication with the registry failed.");
		} catch (NotBoundException e) {
			System.err.println(
					"Error in Client.callOperation(). Registry tried to lookup but the name is not currently bounded.");
		}
	}

	/**
	 * Verifica se os argumentos passados são válidos.
	 * 
	 * @param args
	 *            Argumentos para o protocolo
	 */
	private static boolean processInput(String[] args) {
		peer_ap = args[0];
		operation = args[1];
		if (args.length >= 3)
			operand_1 = args[2];
		if (args.length >= 4)
			operand_2 = args[3];

		return ("BACKUP".equals(operation) || "RESTORE".equals(operation) || "DELETE".equals(operation)
				|| "REMOVED".equals(operation) || "STATE".equals(operation));
	}

	/**
	 * Testing Client Application
	 * 
	 * @param args
	 *            Argumentos passados na consola
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java TestApp <peer_ap> <operation>");
			System.err.println("Usage: java TestApp <peer_ap> <operation> <opnd_1>");
			System.err.println("Usage: java TestApp <peer_ap> <operation> <opnd_1> <opnd_2>");
		}

		else if (processInput(args))
			callOperation();
	}

}
