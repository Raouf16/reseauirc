package model.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ChatServer implements Runnable {

	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String login = null;
	private Thread t3, t4;
	public final Map<String, Socket> clients = new HashMap<>();


	public ChatServer(Socket s, String log){
		socket = Objects.requireNonNull(s);
		login = Objects.requireNonNull(log);
	}
	public void run() {

		try {

			in = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8)); 
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"), true);
			Thread t3 = new Thread(new ReceptionServer(in,login));
			t3.start();
			Thread t4 = new Thread(new EmissionServer(out));
			t4.start();
		} catch (IOException e) {
			System.err.println(login +"s'est déconnecté ");
		}
	}
}
