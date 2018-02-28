package model.client;


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

	public static boolean isACommand(String message) {
		String command = Arrays.asList(message.split(" ")).get(0);
		String commandList [] = {"PASSWORD", "PASS", "NICK", "SERVER", "OPER", "QUIT", "SQUIT", "JOIN", "PART", "MODE", "TOPIC", "NAMES", "LIST", "INVITE", "KICK", "PRVMSG", "NOTICE", "VERSION", "STATS", "LINKS", "TIME", "CONNECT", "TRACE", "ADMIN", "INFO", "WHO", "WHOIS", "WHOWAS", "KILL", "PING", "PONG", "ERROR" , "REHASH", "RESTART", "SUMMON", "USER", "OPERWALL", "USERHOST", "ISON"};
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
