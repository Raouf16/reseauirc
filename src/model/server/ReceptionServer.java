package model.server;
//package model.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.net.*;


public class ReceptionServer implements Runnable {

	private Server server; 
	private BufferedReader in;
	private String message = null, login = null;

	public ReceptionServer(BufferedReader in, String login, Server server){
		this.server = server; 
		this.in = Objects.requireNonNull(in);
		this.login = Objects.requireNonNull(login);
	}

	public void sendMessage(Set<String> dest, String msg){
		// Ici, trois cas de figure : Un client connecté sur ce serveur, un cient non connecté mais qui 
		// appartient à ce serveur et un client qui n'appartient pas à ce serveuro	
		
		Socket client = null;// used to send messages to users and servers
		PrintWriter out = null;

		for (String usr : dest){
			try {
				if (Server.getUserServer(usr).equals(server.getName())) {
					// On envoie le message ici, peut importe si le client est connecté
					if (this.server.getConnectedUsers().contains(usr)){
						// Si le destinataire est connecté, on lui envoie le message
						client = this.server.getClient(usr);
						if (client != null) {
							out = new PrintWriter(client.getOutputStream());
							out.println(msg);
							out.flush();
						}
					}else {
						// Sinon, on met le message dans sa file d'attente 
						try {
							this.server.addWaitingMessage(usr, msg);
						}catch (IllegalArgumentException e){
							e.printStackTrace();
						}
					}
				}else {
					//client = this.server.getSocket();
					//out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
					out = this.server.getOutput();
					out.println("SERVER" + "__" + "UNICAST"+ "__" + login + " " + message);
					out.flush();
				}
			}catch (IOException e){
				e.printStackTrace();
				continue;
			}
		}	
	}

	public void run() {

		PrintWriter out = null;
		boolean stop = false;

		while(true){
			try {
				message = in.readLine();
				if (message.equals("QUIT")) // le déconnecter
				{
					this.server.getClient(login).close();
					System.out.println(login+" a quitté la conversation");
				}
				else {
					String [] received = message.split("__");
					String cmd = received[0];
					message = received[1];
					String [] msgs = message.split(" ");
					String usr;
					Set<String> dest = new HashSet<>();
					Socket client = null;
					String group;
					String name;
					String pass;
					switch (cmd) {
						case "UNICAST" :
						// On va chercher le client/groupe destinataire du message
							message = received[1];
							msgs = message.split(" ");
							for (String d : msgs) {
								if (d.startsWith("@")) {
									usr = d.substring(1);
									if (Server.isClient(usr)) dest.add(usr);
									else if (Server.isGroup(usr)) dest.addAll(Server.getGroupMembers(usr));									break;
								}
							}

							// On envoie le message puis on l'affiche
							sendMessage (dest, login + message);
							System.out.println(login+" : " + " " + message);
							break;

						case "BROADCAST":
							message = received[1];
							msgs = message.split(" ");
							dest = Server.getUsers();
							sendMessage(dest, login + " " + message);
							System.out.println(login+" : "+message);							
							break;

						case "MULTICAST" :
							message = received[1];
							msgs = message.split(" ");
							for (String d : msgs) {
								if (d.startsWith("@")) {
									usr = d.substring(1);
									if (Server.isClient(usr)) dest.add(usr);
									else if (Server.isGroup(usr)) dest.addAll(Server.getGroupMembers(usr));
								}
							}
							// On envoie le message puis on l'affiche 
							sendMessage(dest, login + " " + message);
							System.out.println(login+" : "+message);
							break;	
						case "SERVER" :
							// Quand le message vient d'un serveur, c'est forcément un UNICAST.
							message = received[2];
							System.out.println("je suis " + this.server.getName() + "et je viens de recevoir un message de la part d'un serveur " + message);
							String [] m	= message.split(" ");
							dest.add(m[0]);
							message = m[1];
							sendMessage(dest, message);
							break;
						case "NAMES" : 
							// Afficher tous les utilisateurs									
							client = Server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							out.println(login+":"+"\nUtilisateurs : ");
							for (String s1 : Server.getUsers()) {
								out.println(s1+"");
							}
							out.println("\n Groupes : ");
							for (Group s2 : Server.getGroups()) {
								out.println(s2.getTopic());
							}
							out.flush();
							break;
						case "LIST" : 
							// Afficher tous les groupes sauf les privés si il est pas dedans
							String top = null;
							client = Server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							out.println("\nGroupes : ");
							for (Group s2 : Server.getGroups()) {
								if (s2.getMode() == "p") {
									for (String s : Server.getGroupMembers(s2.getTopic())) {
										if (s == login) {
											top = s2.getTopic();
											out.println(top);
										}
									}
									if (top == null) {
									out.println("prv ");
									}
								}
								else {
								out.println(s2.getTopic());
								}
							}
							out.flush();
							break;
						case  "OPER" : 
							group = msgs[0];
							name = msgs[1];
							pass = msgs[2];
							//if (pass.equals()) {
							//}
							//break;
						case "QUIT":
							Socket sock = Server.getClient(login); 
							if (message != null) {
								out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
								out.println(login+":"+message);
								out.flush();
							}
							if (sock != null) {
								sock.close();
								sock = null;
							}
							break;
						case "KICK":
							group = msgs[0];
							name = msgs[1];
							String msg = null; //c'est pas bon, faut recuperer tout le reste du tableau
							Group grp = null;
							for (Group s2 : Server.getGroups()) {
								if (s2.getTopic() == group) {
									grp = s2;
								}
							}
								if(grp !=null) {
									if (grp.getOperator() == login) {
										//Si y'a un message de kickage, on l'envoie 
										if (msgs[2] != null) { // Pas bon, faut recup toute le tableau
										msg = msgs[2];
										out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
										out.println(login+":"+message);
										out.flush();
										}
										grp.kick(name);
										// On le vire de l'arraylist Server ou non ?
									}
							}
							break;
						case "MODE" :
							// Mode des groupes, modes des utilisateurs pas gérés
							group = msgs[0];
							String temp = msgs[1];
							String opp = temp.substring(0, 0);
							String modes = temp.substring(1);
							String mode;
							Group grp2 = null;
							name = msgs[2];
							// Traiter opp = - ou + à chaque fois 
							for (int i = 0; i < modes.length(); i++) {
									mode = modes.substring(i, i);
									if(mode == "p"){ // drapeau de canal privé
										for (Group s3 : Server.getGroups()) {
											if (s3.getTopic() == group) {
												grp2 = s3;
											}
										}
									}
									if (mode == "o") { // donne/retire privileges doperateur
										if(opp == "-") {
										}	
										if (opp == "+") {		
										}
									}
									if (mode == "i") {// mode sur invitation
										if(opp == "-") {
										}	
										if (opp == "+") {		
										}
									}
									if (mode == "") {
										if(opp == "-") {
										}	
										if (opp == "+") {		
										}
									}
									
									if (mode == "s" || mode == "b" || mode == "v" || mode == "k" || mode == "l" || mode == "m" || mode == "n" || mode == "t") {
											// reconnaissance mais pas de traitement effectué dans notre projet
									}
							}
							break;
						case "PASS" :
							if(msgs.length != 1 ) {
								out.println("Erreur : La commande PASS prend qu'un et un seul paramètre");
								out.flush();
							}
							else {
								String mdp = msgs[0];
								Server.changePWD(login, mdp);
							}
							break;
						case "NICK" :
							if(msgs.length != 1 ) {
								out.println("Erreur : La commande NICK prend qu'un et un seul paramètre (pas de compteur de distance)");
								out.flush();
							}
							else if(Server.getUsers().contains(login)) {
								String newLogin = msgs[0];
								Server.changeLOGIN(newLogin, login);
							}
							else {
								Server.addMember(login, "");
							}
							break;
							
									
							
								
						
							/*
							 password
							 nick
							 user
							 server
							 oper
							 //quit 
							 squit
							 join
							 part
							 mode
							 //names
							 //list
							 invite
							 //kick
							 //privmsg
							 //notice
							 ping
							 pong
							 */	
						}
				}
				} catch (IOException e) {
				e.printStackTrace();
				}
		}
	}
}