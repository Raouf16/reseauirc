package model.client;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Connexion implements Runnable {

	private Socket socket = null;
	public static Thread t2;
	public static String login = null, pass = null, message1 = null, message2 = null, message3 = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Scanner sc = null;
	private boolean connect = false;

	public Connexion(Socket s){
		socket = s;
	}

	public void run() {

		try {

			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
			sc = new Scanner(System.in);
			boolean error = false;


			while(!connect ){
				System.out.println(in.readLine());
				login = sc.nextLine();
				out.println(login);
				out.flush();

				System.out.println(in.readLine());
				pass = sc.nextLine();
				out.println(pass);
				out.flush();
				String inLine = in.readLine();
				if(inLine.equals("connecte")){

					System.out.println("Je suis connecté "); 
					connect = true;
				}
				else if(inLine.equals("Vous avez atteint le nombre d'essais max!"))
				{
					connect = true;
					error = true;
				}
				else {
					System.err.println("Vos informations sont incorrectes "); 
				}
			}

			if (!error) {
				t2 = new Thread(new ChatClient(socket));
				t2.start();
			}
			else 
			{
				System.out.println("Au revoir");
				socket.close();
			}
			

		} catch (IOException e) {

			System.err.println("Le serveur ne répond plus ");
		}
	}

}
