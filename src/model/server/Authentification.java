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
	public Thread t2;

	public static final int MAX_TRY = 3;


	public Authentification(Socket s){
		socket = Objects.requireNonNull(s);
	}
	public void run() {

		try {
			int count = 0;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());

			while(!authentifier){
				while(count < MAX_TRY) {
					out.println("Entrez votre login :");
					out.flush();
					login = in.readLine();

					out.println("Entrez votre mot de passe :");
					out.flush();
					pass = in.readLine();

					if(!isConnected(login) && isValid(login, pass)){

						out.println("connecte");
						System.out.println(login +" vient de se connecter ");
						out.flush();
						authentifier = true;
						break;
					}
					else {
						count++;
						out.println("erreur"); 
						out.flush(); 
					}
				}
				if (count == MAX_TRY && !authentifier) 
				{
					//TODO envoyer message au server pour le supprimer
					out.println("Vous avez atteint le nombre d'essais max!");
					out.flush();
					socket.close();
				}
			}
			Server.addConnection(login,socket);
			t2 = new Thread(new ChatServer(socket,login));
			t2.start();

		} catch (IOException e) {

			System.err.println(login+" ne répond pas !");
		}
	}
	
	private static boolean isConnected(String login) {
		Objects.requireNonNull(login);
		return Server.getConnectedUsers().contains(login);
	}

	private static boolean isValid(String login, String pass) 
	{
		return Server.isValidUser(login, pass);
	}
}
