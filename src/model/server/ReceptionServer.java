package model.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


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
		
		 // smiley
	      msg = msg.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
	      msg = msg.replace(":D", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
	      msg = msg.replace(":d", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
	      msg = msg.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
	      msg = msg.replace("-_-", "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
	      msg = msg.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
	      msg = msg.replace(":P", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
	      msg = msg.replace(":p", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
	      msg = msg.replace(":o", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
	      msg = msg.replace(":O", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
	      
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
					if (this.server.getNext() != null){
						out = this.server.getOutput();
						out.println("SERVER" + "__" + "UNICAST"+ "__" + usr + " " + msg);
						out.flush();
					}
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

		while(! stop){
			try {
				message = in.readLine();
				if (message == null) // le déconnecter
				{
					this.server.disconnectUser(login);
					System.out.println("Le client a quitté, connexion interrompue -- reception");
					stop = true;
				}
				else if (message.equals("QUIT")){
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
							dest = new HashSet<>();
							msgs = message.split(" ");
							int nbMsgs=0;
							// J'enleve les destinataires d'abord
							for (String d : msgs) {
								if (d.startsWith("@")) {
									nbMsgs ++;
								}
							}
							
							// je récupère le vrai message
							message = "";
							for (int i=nbMsgs; i<msgs.length; i++){
								message += " " + msgs[i];
							}

							for (String d : msgs) {
								if (d.startsWith("@")) {
									usr = d.substring(1);
									if (Server.isClient(usr)) {
										dest = new HashSet<>();
										dest.add(usr);
										sendMessage (dest, login + " à vous : " + message);
									}
									else if (Server.isGroup(usr)){
										dest = new HashSet<>();
										dest.addAll(Server.getGroupMembers(usr));
										sendMessage (dest, login + " au groupe " + usr + " : " + message);
									} 
								}
							}							
							System.out.println(login+" : " + " " + message);
							break;

						case "BROADCAST":
							dest = new HashSet<>();
							message = received[1];
							msgs = message.split(" ");
							dest = Server.getUsers();
							sendMessage(dest, login + " : " + message);
							System.out.println(login + " : " + message);							
							break;

						case "MULTICAST" :
							message = received[1];
							dest = new HashSet<>();
							msgs = message.split(" ");
							nbMsgs=0;
							// J'enleve les destinataires d'abord
							for (String d : msgs) {
								if (d.startsWith("@")) {
									nbMsgs ++;
								}
							}
							
							// je récupère le vrai message
							message = "";
							for (int i=nbMsgs; i<msgs.length; i++){
								message += " " + msgs[i];
							}
							for (String d : msgs) {
								if (d.startsWith("@")) {
									usr = d.substring(1);
									if (Server.isClient(usr)){
										dest.add(usr);
										sendMessage(dest, login + " à vous : " + message);
									} 
									else if (Server.isGroup(usr)){
										dest.addAll(Server.getGroupMembers(usr));
										sendMessage(dest, login + " au groupe " + usr + " : " + message);
									}
								}
							}
							// On envoie le message puis on l'affiche 
							System.out.println(login+" : "+message);
							break;	
						case "SERVER" :
							// Quand le message vient d'un serveur, c'est forcément un UNICAST.
							//System.out.println("je reçoit le message d'un serveur");
							message = received[2];
							String [] m	= message.split(" ");
							dest = new HashSet<>();
							dest.add(m[0]);
							message = m[1];
							for (int i=2; i<m.length; i++){
								message += " " + m[i];
							}
							sendMessage(dest, message);
							break;
						case "NAMES" : 
							// Afficher tous les utilisateurs									
							client = this.server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							out.println(login+":"+"\nUtilisateurs : ");
							for (String s1 : Server.getUsers()) {
								out.println(s1+"");
							}
							out.println("\n Groupes : ");
							//for (Group s2 : Server.getGroups()) {
							//	out.println(s2.getTopic());
							//}
							out.println("votre message : ");
							out.flush();
							break;
						case "LIST" :
							// Afficher tous les groupes sauf les privés si il est pas dedans
							String top = null;
							client = this.server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							out.println("\nGroupes : ");
							for (Group s2 : Server.getGroups()) {
								if (s2.getMode() == "s") {
									//Canal secret, pas de traitement
								}
								else {
									if (s2.getMode() == "p") {
										for (String s : Server.getGroupMembers(s2.getTopic())) {
											if (s.equals(login)) {
												top = s2.getTopic();
												out.println(top);
											}
										}
									if (top == null) {
									out.println("prv ");
									}
									else {
								out.println(s2.getTopic());
									}
									}
								}
							}
							out.flush();
							break;
						case  "OPER" : 
							name = msgs[0];
							pass = msgs[1];
							client = this.server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							
							if (this.server.getOperators().contains(name)) {
								out.println("Vous êtes déjà opérateur");
								out.flush();
							}
							else {
								if (this.server.checkPassword(pass)) {
									this.server.addOperator(name);
									out.println(name + "est maintenant opérateur du serveur");
									out.flush();
								}
							}
							Server.updateAnnuaires();
							break;
						case "QUIT":
							Socket sock = this.server.getClient(login); 
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
									if (grp.getOperators().contains(login)) {
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
							Server.updateAnnuaires();
							break;
						case "MODE" :
							// Mode des groupes
							// Modes des utilisateurs pas gérés
							group = msgs[0];
							String temp = msgs[1];
							String opp = temp.substring(0, 0);
							String modes = temp.substring(1);
							String mode;
							Group grp2 = null;
							name = msgs[2];
							client = this.server.getClient(login);
							out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
							// Traiter opp = - ou + à chaque fois
							if (opp.equals("+")) {
								for (int i = 0; i < modes.length(); i++) {
									mode = modes.substring(i, i);
									for (Group s3 : Server.getGroups()) {
										if (s3.getTopic() == group) {
											grp2 = s3;
										}
									}
									if (grp2 != null) {
										if (mode == "p") {// drapeau de canal privé
											grp2.setMode("p");
										}
										if (mode == "o") { // donne/retire privileges doperateur
											if (grp2.getOperators().contains(login)){
												grp2.addOperator(name);
											}
										}
										if (mode == "i") {// mode sur invitation
											grp2.setMode("i");
										}
										if (mode == "t") {
											grp2.setDrapeauTopic("t");
										}
										if (mode == "l") {
											if (grp2.getOperators().contains(login)){
												grp2.setMaxUsers(Integer.parseInt(name));
											}
										}
										
										if (mode == "s" || mode == "b" || mode == "v" || mode == "k" || mode == "l" || mode == "m" || mode == "n") {
											// reconnaissance mais pas de traitement effectué dans notre projet
											out.println("Vous ne pouvez pas utiliser cette commande dans le cadre de ce projet");
										}
									}
								}
							}
							else {
								if (opp == "-") {
									for (int i = 0; i < modes.length(); i++) {
										mode = modes.substring(i, i);
										for (Group s3 : Server.getGroups()) {
											if (s3.getTopic() == group) {
												grp2 = s3;
											}
										}
										if (grp2 != null) {
											if (mode == "p") {// drapeau de canal privé
												grp2.setMode(null);
											}
											if (mode == "o") { // donne/retire privileges doperateur
												if (grp2.getOperators().contains(login)){
													grp2.removeOperator(name);
												}
											}
											if (mode == "i") {// mode sur invitation
												grp2.setMode(null);
											}
											if (mode == "t") {// drapeau de sujet du canal 
												grp2.setDrapeauTopic(null);
											}
											if (mode == "l") {//nbr max de connexions
												if (grp2.getOperators().contains(login)){
													grp2.setMaxUsers(9999);
												}
											}
											
											if (mode == "s" || mode == "b" || mode == "v" || mode == "k" || mode == "l" || mode == "m" || mode == "n") {
													// reconnaissance mais pas de traitement effectué dans notre projet
											}
										}
									}
								}
							}
							Server.updateAnnuaires();
							break;
						case "INVITE" : 
							String nameInvite = msgs[0];
							String canalInvite = msgs[1];
							Group grp3 = null;
							name = msgs[2];
							for (Group s4 : Server.getGroups()) {
								if (s4.getTopic() == canalInvite) {
									grp3 = s4;
								}
							}
							if (grp3 != null) {
								if (grp3.getMode().equals("i")) {
									if (grp3.getOperators().contains(login)) {
										grp3.addMember(name);
									}
								}else {
									grp3.addMember(name);
								}
							}
							Server.updateAnnuaires();
							break; 
						case "PART": 
						// Quitter un groupe
							msgs = message.split(",");
							Group grp6 = null;
							for(int i=0;i<msgs.length;i++) {
								String msgTemp = msgs[i];
								for (Group s4 : Server.getGroups()) {
									if (s4.getTopic() == msgTemp) {
										grp6 = s4;
									}
								}
								if (grp6 != null) {
									if (grp6.getMembers().contains(login)) {
									grp6.removeMember(login);
									}
								}
							}
							Server.updateAnnuaires();
							break;
						case "PASS" :
							if(msgs.length != 2 ) {
								//Impossible 
							}
							else {
								String mdp = msgs[1];
								Server.changePWD(login, mdp);
							}
							break;
						case "NICK" :
							if(msgs.length != 2 ) {
								out.println("Erreur : La commande NICK prend qu'un et un seul paramètre (pas de compteur de distance)");
								out.flush();
							}
							else if(Server.getUsers().contains(login)) {
								String newLogin = msgs[1];
								Server.changeLOGIN(newLogin, login);
							}
							else {
								Server.addMember(login, "");
							}
							break;
							
							/*
							 //password
							 //nick
							 user
							 //server
							 //oper
							 //quit 
							 squit
							 join
							 //part
							 //mode
							 //names
							 //list
							 //invite
							 //kick
							 //privmsg
							 //notice
							 kill
							 ping
							 pong
							 */	
							default :
								client = this.server.getClient(login);
								out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF8"), true);
								out.println("Desolé ! La commande "+ cmd + " n'est pas gérée dans le cadre de ce projet");
						}
					}
				}catch (IOException e) {
				e.printStackTrace();
				}
		}
	}
}