package model.server;
//package model.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Server {
	private static final ArrayList<Server> onLineServers = new ArrayList<>();
	private Server previous;
	private Socket socket;
	private Socket clientSocket;
	private Server next;
	private String name; 
	private int port;
	private final static Map <String,Socket> connections = new HashMap<>();
	private final Map<String, ArrayList<String>> waitingLine;
	private AcceptConnexion acceptConnexion;
	private PrintWriter out;

	public static ServerSocket ss = null;
	public static Thread t;
	private final static Map<String,String[]> users = initUsersMap();
	private final static ArrayList<Group> groups = initGroupsList();;

	public Server(int port, String name){
		this.name = name;
		this.port = port;	
		try {
			this.ss = new ServerSocket(port);
			acceptConnexion = new AcceptConnexion(this.ss, this);
			t = new Thread(acceptConnexion);
			t.start();
			synchronized (onLineServers){
				if (onLineServers.size() > 1) {
					this.previous = onLineServers.get(onLineServers.size()-1);
					this.next = previous.getNext();
					this.previous.changeNext(this);
					
					// Définir dans quel ordre instancier les sockets 

					//Thread conn1 = new Thread(new AcceptConnexion(this.next.getServerSocket(), this.next, true));
					this.socket = new Socket(InetAddress.getLocalHost(), this.next.getPort());
					out =  new PrintWriter(this.socket.getOutputStream());
					out.println("server");
					out.flush();
					// On s'autentifie au serveur
					
					//Thread conn2 = new Thread(new AcceptConnexion(this.ss, this, true));
					this.previous.changeSocket(new Socket(InetAddress.getLocalHost(), this.port));
					PrintWriter pw = this.previous.getOutput();
					pw.println("server");
					pw.flush();
					
				}else if (onLineServers.size() == 1){
					this.previous = onLineServers.get(0);
					this.next = this.previous;
					this.previous.changeNext(this);
					
					// Définir dans quel ordre instancier les sockets 
					//Thread conn1 = new Thread(new AcceptConnexion(this.next.getServerSocket(), this.next, true));
					this.socket = new Socket(InetAddress.getLocalHost(), this.next.getPort());
					
					//Thread conn2 = new Thread(new AcceptConnexion(this.ss, this, true));
					this.previous.changeSocket(new Socket(InetAddress.getLocalHost(), this.port));
				}else{
					this.next = this;
				}
				onLineServers.add(this);
				System.out.println("Serveur " + name + " connect� sur le port " + port);
			}
		}catch (IOException e){
				e.printStackTrace();
		}

		this.waitingLine = initWaitingLine();
	}

	public PrintWriter getOutput() {
		return this.out;
	}
	
	public Socket accept(){
		try {
			return this.ss.accept();
		}catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}

	public ServerSocket getServerSocket(){
		return ss; 
	}

	public Socket getSocket (){
		return this.socket;
	}

	public void changeSocket(Socket s){
		this.socket = s;
		try {
			this.out = new PrintWriter(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPort(){
		return this.port;
	}

	public Server getNext(){
		return this.next;
	}

	public Server getPrevious(){
		return this.previous;
	}

	public String getName(){
		return this.name;
	}

	public void changePrevious (Server s){
		this.previous = previous;
	}

	public void changeNext (Server s){
		this.next = s;
	}

	public static String getUserServer(String usr){
		Objects.requireNonNull(users);
		return users.get(usr)[1];
	}

	public synchronized void addWaitingMessage(String usr, String msg)throws IllegalArgumentException{
		Objects.requireNonNull(usr);
		if (this.waitingLine.containsKey(usr)){
			this.waitingLine.get(usr).add(msg);
		}else {
				throw new IllegalArgumentException("Le client n'est pas reconnu par le seveur -- add");
		}		
	}

	public synchronized ArrayList<String> getWaitingMessages (String usr){
		Objects.requireNonNull(usr);
		if (this.waitingLine.keySet().contains(usr)) {
			return this.waitingLine.get(usr);
		}
		else throw new IllegalArgumentException("Le client n'est pas reconnu par le serveur -- get");
	}

	public synchronized void deleteWaitingMessages(String usr){
		Objects.requireNonNull(usr);
		if (this.waitingLine.keySet().contains(usr)) this.waitingLine.put(usr, new ArrayList<>());
	}

	private synchronized Map<String, ArrayList<String>> initWaitingLine(){
		/* Cette méthode construit une Map qui contiendra les messages en attente 
		pour chaque utilisateur lié à ce serveur */
		Objects.requireNonNull(this.name);
		Objects.requireNonNull(users);
		
		Map<String, ArrayList<String>> waitingLine = new HashMap();
		for (String usr : users.keySet()){
			if (users.get(usr)[1].equals(this.name)){
				waitingLine.put(usr, new ArrayList<>());
			}
		}
		return waitingLine; 
	}

	private static synchronized Map<String,String[]> initUsersMap() {
		String fileName = "users";
		
		Map<String, String[]> users = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String line;
			String [] ids;
			String key;
			String [] value;
			while ((line = br.readLine()) != null) {
				ids = line.split(" ");
				key = ids[0];
				value = new String[2];
				value [0] = ids[1];
				value [1] = ids[2];
				users.put(key, value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return users;
	}

	private static synchronized ArrayList<Group> initGroupsList(){
		ArrayList<Group> groupsList = new ArrayList<>();
		String fileName = "groups";
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String line;
			String [] groupDescription;
			while ((line = br.readLine()) != null) {
				groupDescription = line.split(" ");
				String topic = groupDescription[0];
				String operator = groupDescription[1];
				String mode = groupDescription[2];
				String password = groupDescription[3];
				ArrayList<String> members = new ArrayList<>();
				for (int i=4; i<groupDescription.length; i++) {
					members.add(groupDescription[i]);
				}
				Group group = new Group (operator, mode, topic, password, members);
				groupsList.add(group);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return groupsList;
	}
	
	public synchronized void addConnection(String login, Socket client) {
		Objects.requireNonNull(login);
		Objects.requireNonNull(client);
		connections.put(login, client);
	}
	
	public synchronized Set<String> getConnectedUsers() {
		return this.connections.keySet();
	}
	
	public synchronized static Set<String> getUsers() {
		return users.keySet();
	}
	
	public synchronized static Socket getClient(String login) {
		Objects.requireNonNull(login);
		return connections.get(login);
	}
	
	public synchronized static boolean isValidUser(String login, String pass) {
		//Objects.requireNonNull(pass);
		//Objects.requireNonNull(login);
		if (login.startsWith("serveur")) return true;
		else return (pass.equals(users.get(login)[0]));
	}
	
	public synchronized Set<Socket> getClients () {
		return (Set<Socket>) connections.values();
	}
	
	public static boolean isClient(String usr) {
		if (users.keySet().contains(usr)) return true; 
		return false; 
	}
	
	public static synchronized boolean isGroup(String grp) {
		if (groups.contains(grp)) return true;
		return false; 
	}
	
	public static synchronized ArrayList<String> getGroupMembers(String grp){
		for (Group group : groups) {
			if (group.equals(grp)) return group.getMembers();
		}
		return null;
	}
	
	public static ArrayList<Group> getGroups() {
		return groups;
	}

	public static boolean addMember(String login, String mdp){
		if(users.containsKey(login)){
			return false;
		}
		else {
			String [] tab = new String[2];
			tab[0] = mdp;
			tab[1] = "serveur1";
			users.put(login, tab);
			rewriteAnnuaire();
		}
		return true;
	}
	
	public static void rewriteAnnuaire() {
		String fileName = "users";
		try {
			FileWriter fw = new FileWriter (new File(fileName));
			for(String user : users.keySet()) {
				fw.write (user+" "+users.get(user)[0]+" "+users.get(user)[1]);
		        fw.write ("\r\n");
			}
			
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public static void changePWD(String login, String mdp) {
		String [] tab = new String[2];
		tab[0] = mdp;
		tab[1] = users.get(login)[1];
		users.put(login, tab);
		rewriteAnnuaire();
	}
	
	public static void changeLOGIN(String newlogin, String login) {
		String [] tab = new String[2];
		tab[0] = users.get(login)[0];
		tab[1] = users.get(login)[1];
		users.put(newlogin, tab);
		users.remove(login);
		rewriteAnnuaire();
	}
	// ############################# MAIN ###################################### //
}
