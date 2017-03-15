package handlers;

import java.util.Queue;

public class MdbHandler extends Thread {
	private Queue<String> msgQueue;

	public MdbHandler(Queue<String> msgQueue) {
		this.msgQueue = msgQueue;
	}

	@Override
	public void run() {
		while (!isInterrupted())
			if (!msgQueue.isEmpty()) {
				String[] msg = msgQueue.poll().split("\\s",6);
				System.out.println("Received: ");
				for(int i = 0; i<msg.length; i++)
					System.out.print(msg[i] + "; ");
				System.out.println();
			}
	}
}
