package model.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class AcceptConnexion implements Runnable{

	private ServerSocket socketserver = null;
	private Socket socket = null;
	private Server server;
	private boolean serverConnection;

	public Thread t1;
	public AcceptConnexion(ServerSocket ss, Server server){
		socketserver = Objects.requireNonNull(ss);
		this.server = server;
		this.serverConnection = serverConnection;
	}

	public void run() {
		try {
			while(true){
				socket = socketserver.accept();
				System.out.println("Un client tente de se connecter ");
				t1 = new Thread(new Authentification(socket, server));
				t1.start();
			}
		} catch (IOException e) {
			System.err.println("Erreur serveur");
		}
	}
}