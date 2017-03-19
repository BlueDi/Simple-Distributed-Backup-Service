package peer;

import java.io.IOException;
import java.util.Scanner;

import handlers.MdbHandler;
public class Client {
//	private static MulticastChannel mc = null;
//	private static MulticastChannel mdb = null;
//	private static MulticastChannel mdr = null;
//	private static MulticastListener mcListener = null;
//	private static MulticastListener mdbListener = null;
//	private static MulticastListener mdrListener = null;
//	private static MdbHandler mdbHandler = null;
	
	//Peer Initiator
	private static int acess_point;
	
	/**
	 * Peer initiator function
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		Scanner input = new Scanner(System.in);
		if (args.length < 4) 
		{
			System.out.println("Usage: java PeerInitiator <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ");
		}
		else
		{
			processInput(args);
		}

	}

	public static void processInput(String[] args){
		try {
			acess_point = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {  
			System.out.println("Error parsing acess point");
		}
		String subProtocol = args[1];
		String operand1 = args[2];
		String operand2 = args[3];

		switch(subProtocol){
		case "BACKUP" : 
			operationBackup();
			break;
		case "RESTORE" : break;
		case "DELETE" : break;
		case "RECLAIM" : break;
		case "STATE" : break;
		default: System.out.println("Wrong State"); break;
		}
	}

	public static void operationBackup(){
		System.out.println("Backup this file");
	}
}
