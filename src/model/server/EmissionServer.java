package model.server;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Scanner;


public class EmissionServer implements Runnable {

	private PrintWriter out;
	private String message = null;
	private Scanner sc = null;

	public EmissionServer(PrintWriter out) {
		this.out = Objects.requireNonNull(out);
	}


	public void run() {

		sc = new Scanner(System.in);

		while(true){
			System.out.println("Votre message :");
			message = sc.nextLine();
			out.println(message);
			out.flush();
		}
	}
}
