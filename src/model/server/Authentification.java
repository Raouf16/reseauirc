package model.server;


import java.net.*;
import java.util.Objects;
import java.io.*;

public class Authentification implements Runnable {

	private Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private String login = "zero", pass =  null;
	public boolean authentifier = false;
	public boolean running = true;
	public Thread t2;
	private Server server;
	private boolean serverConnection;

	public static final int MAX_TRY = 3;


	public Authentification(Socket s, Server server){
		socket = Objects.requireNonNull(s);
		this.server = server;
	}
	public void run() {
		try {
			int count = 0;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			while(!authentifier && running){
				while(count < MAX_TRY) {
					out.println("Entrez votre login :");
					out.flush();
					login = in.readLine();
					if (login == null){
						running = false;
						System.out.println("Le client a quitt�, connexion interrompue -- aut");
						count = MAX_TRY;
					}
					else if (login.equals("server")) {
						authentifier = true;
						break;
					}else {
						out.println("Entrez votre mot de passe :");
						out.flush();
						pass = in.readLine();
						System.out.println(pass);
						if (pass == null){ 
							running = false;
							System.out.println("Le client a quitt�, connexion interrompue");
						}
						else if(!isConnected(login) && isValid(login, pass)){
							out.println("connecte");
							System.out.println(login +" vient de se connecter ");
							out.flush();
							authentifier = true;
							break;
						}else {
							count++;
							out.println("erreur"); 
							out.flush(); 
						}
					}	
				}
				if (count == MAX_TRY && !authentifier) 
				{
					out.println("Vous avez atteint le nombre d'essais max!");
					out.flush();
					socket.close();
				}
			}
			if (authentifier) {
				server.addConnection(login,socket);
				System.out.println("connection �tablie");
				t2 = new Thread(new ChatServer(socket,login, server));
				t2.start();
			}
		} catch (IOException e) {

			System.err.println(login+" ne r�pond pas !");
		}
	}
	
	private boolean isConnected(String login) {
		Objects.requireNonNull(login);
		return this.server.getConnectedUsers().contains(login);
	}

	private boolean isValid(String login, String pass) 
	{
		return (this.server.isValidUser(login, pass) && Server.getUserServer(login).equals(this.server.getName()));
	}
}
