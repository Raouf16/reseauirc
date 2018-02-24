package model.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;


public class ReceptionClient implements Runnable {

	private BufferedReader in;
	private String message = null;

	public ReceptionClient(BufferedReader in){

		this.in = Objects.requireNonNull(in);
	}
	
	public void run() {

		while(true){
			try {
				message = in.readLine();
				System.out.println(message);

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

}
