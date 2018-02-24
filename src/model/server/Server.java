package model.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Server {
	public static ServerSocket ss = null;
	public static Thread t;
	private final static Map<String,String> users = initUsersMap();
	private final static Map <String,Socket> connections = new HashMap<>();
	private final static ArrayList<Group> groups = initGroupsList();;


	public static void main(String[] args) {

		try {
			ss = new ServerSocket(1995);
			System.out.println("Le serveur est à l'écoute du port "+ss.getLocalPort());
			initUsersMap();
			t = new Thread(new AcceptConnexion(ss));
			t.start();

		} catch (IOException e) {
			System.err.println("Le port "+ss.getLocalPort()+" est déjà utilisé !");
		}
	}

	private static synchronized Map<String,String> initUsersMap() {
		String fileName = "users.txt";
		
		Map<String, String> users = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String line;
			String [] ids;
			while ((line = br.readLine()) != null) {
				ids = line.split(" ");
				users.put(ids[0], ids[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return users;
	}

	private static synchronized ArrayList<Group> initGroupsList(){
		ArrayList<Group> groupsList = new ArrayList<>();
		String fileName = "groups.txt";
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String line;
			String [] groupDescription;
			while ((line = br.readLine()) != null) {
				groupDescription = line.split(" ");
				String topic = groupDescription[0];
				String operator = groupDescription[1];
				String mode = groupDescription[2];
				ArrayList<String> members = new ArrayList<>();
				for (int i=3; i<groupDescription.length; i++) {
					members.add(groupDescription[i]);
				}
				Group group = new Group (operator, mode, topic, members);
				groupsList.add(group);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return groupsList;
	}
	
	public synchronized static void addConnection(String login, Socket client) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(client);
		connections.put(login, client);
	}
	
	public synchronized static Set<String> getConnectedUsers() {
		return connections.keySet();
	}
	
	public synchronized static Set<String> getUsers() {
		return users.keySet();
	}
	
	public synchronized static Socket getClient(String login) {
		Objects.requireNonNull(login);
		return connections.get(login);
	}
	
	public synchronized static boolean isValidUser(String login, String pass) {
		System.out.println(getUsers());
		Objects.requireNonNull(pass);
		Objects.requireNonNull(login);
		return (pass.equals(users.get(login)));
	}
	
	public synchronized static Set<Socket> getClients () {
		return (Set<Socket>) connections.values();
	}
	
	public static boolean isClient(String usr) {
		if (users.keySet().contains(usr)) return true; 
		return false; 
	}
	
	public static boolean isGroup(String grp) {
		if (groups.contains(grp)) return true;
		return false; 
	}
	
	public static ArrayList<String> getGroupMembers(String grp){
		for (Group group : groups) {
			if (group.equals(grp)) return group.getMembers();
		}
		return null;
	}
	
	public static boolean addMember(String login, String mdp){
		if(users.containsKey(login)){
			return false;
		}
		else {
			users.put(login, mdp);
			rewriteAnnuaire();
		}
		return true;
	}
	
	public static void rewriteAnnuaire() {
		String fileName = "users.txt";
		try {
			FileWriter fw = new FileWriter (new File(fileName));
			for(String user : users.keySet()) {
				fw.write (user+" "+users.get(user));
		        fw.write ("\r\n");
			}
			
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
