package model.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import model.server.Server;


public class ChatClient implements Runnable {

	private Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Scanner sc;
	private Thread t3, t4;

	public ChatClient(Socket s){
		socket = s;
	}

	public void run() {
		try {
			//pour l'envoie et la reception en UTF-8
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			sc = new Scanner(System.in);
			Thread t4 = new Thread(new EmissionClient(out));
			t4.start();
			Thread t3 = new Thread(new ReceptionClient(in));
			t3.start();



		} catch (IOException e) {
			System.err.println("Le serveur distant s'est déconnecté !");
		}
	}

}

