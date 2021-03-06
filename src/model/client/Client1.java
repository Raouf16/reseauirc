package model.client;

import java.io.*;
import java.net.*;

public class Client1 {

	public static Socket socket = null;
	public static Thread t1;

	public static void main(String[] args) {

		try {

			System.out.println("Demande de connexion");
			socket = new Socket(InetAddress.getLocalHost(),19958);
			System.out.println("Connexion établie avec le serveur, authentification :"); // Si le message s'affiche c'est que je suis connecté
			t1 = new Thread(new Connexion(socket));
			t1.start();

		} catch (UnknownHostException e) {
			System.err.println("Impossible de se connecter à l'adresse "+socket.getLocalAddress());
		} catch (IOException e) {
			System.err.println("Aucun serveur à l'écoute du port "+19958);
		}
	}
}