package model.client;
//package model.client;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

//import model.server.Server;


public class EmissionClient implements Runnable {

	private PrintWriter out;
	private String login = null, message = null;
	private Scanner sc = null;

	public EmissionClient(PrintWriter out) {
		this.out = Objects.requireNonNull(out);

	}

	public boolean isACommand(String message) {
		String command = Arrays.asList(message.split(" ")).get(0);
		String commandList [] = {"NICK", "USER", "OPER", "MODE", "JOIN", "TOPIC", "INVITE", "KICK"};
		return Arrays.asList(commandList).contains(command);
	}
	
	public void run() {

		sc = new Scanner(System.in);

		while(true){
			System.out.println("Votre message :");
			message = sc.nextLine();
			ArrayList<String> dest = new ArrayList<>();
			if (message.equals("quit") || message.equals("QUIT")) {
				out.println("QUIT");
				out.flush();
			}
			else {
				
				/*for (String user : Server.getUsers())
				{
					for (String msg : Arrays.asList(message.split(" "))) {
						if (msg.contains("@"+user)) dest.add(user);
					}
				}*/
				
				for (String msg : Arrays.asList(message.split(" "))) {
					if (msg.startsWith("@")) dest.add(msg.substring(1));
				}
				
				String cmd = "";
				if (dest.size() == 1) cmd = "UNICAST";
				else if (dest.size() > 1) cmd = "MULTICAST";
				else {
					if (isACommand(message)) {
						cmd = Arrays.asList(message.split(" ")).get(0);
					}else {
						cmd = "BROADCAST";
					}
				}
				out.println(cmd+"__"+message);
				out.flush();
			}
		}
	}
}
