package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import model.client.EmissionClient;
import model.server.Server;

import java.util.ArrayList;
import java.util.Arrays;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name; 
  private String password;
  private String login;
  private String pwd;
  private String confirmpwd;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  public static final int MAX_TRY = 3;
  private static ArrayList<String> ListUser = new ArrayList<>();

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 1995;
    this.name = "nickname";
    this.password = "password";
    this.login = "login";
    this.pwd = "password";
    this.confirmpwd = "confirm password";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(false);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Module du fil de discussion
    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Module de la liste des utilisateurs
    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Field message user input
    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    // button send
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(575, 410, 100, 35);
    
    // button Confirm 
    final JButton jsbtncon = new JButton("Confirm");
    jsbtncon.setFont(font);
    jsbtncon.setBounds(575, 380, 100, 35);

    // button Disconnect
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(25, 410, 130, 35);

 
    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // Click on send button
    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JPasswordField jtfPassword = new JPasswordField(this.password);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JTextField jtflogin = new JTextField(this.login);
    JLabel jlfpwd = new JLabel("Password");
    JLabel jlfconfirmpwd = new JLabel("Confirm Password");
    final JPasswordField jtfpwd = new JPasswordField(this.pwd);
    final JPasswordField jtfconfirmpwd = new JPasswordField(this.confirmpwd);
    
    final JButton jcbtn = new JButton("Connect");
    final JButton jcbtnlog = new JButton("Inscription");

    // check if those field are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfPassword, jtfport, jtfAddr, jcbtn));
    jtfPassword.getDocument().addDocumentListener(new TextListener(jtfName, jtfPassword, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfPassword, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfPassword, jtfport, jtfAddr, jcbtn));

    // position des Modules
    jcbtn.setFont(font);
    jcbtnlog.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfPassword.setBounds(375, 425, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 30);
    jcbtnlog.setBounds(575, 415, 100, 30);
    jtflogin.setBounds(25, 380, 135, 40);
    jtfpwd.setBounds(200, 380, 135, 40);
    jtfconfirmpwd.setBounds(375, 380, 135, 40);
    jlfpwd.setBounds(204, 410, 135, 40);
    jlfconfirmpwd.setBounds(379, 410, 135, 40);
    
    // couleur par defaut des Modules fil de discussion et liste des utilisateurs
    jtextFilDiscu.setBackground(Color.cyan);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    // ajout des éléments
    jfr.add(jcbtn);
    jfr.add(jcbtnlog);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfPassword);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.setVisible(true);


    // info sur le Chat
    appendToPane(jtextFilDiscu, "<h2><center>Welcome to ZLYR instant messaging</center></h2>"
        +"<ul>"
        +"<h3>Voici quelques commandes utiles : </h3>"	
        +"<li><b>NICK</b> suivi du nickname afin de créer un nouvel utilisateur ou changer son username</li>"
        +"<li><b>PASS</b> suivi du mdp pour changer son mot de passe</li>"
        +"<li><b>;)</b> quelques smileys sont implémentés</li>"
        +"<li><b>flèche du haut</b> pour reprendre le dernier message tapé</li>"
        +"</ul><br/>");

    // On connect
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          name = jtfName.getText();
          password = String.valueOf(jtfPassword.getPassword());
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);
          ClientGui.ListUser.add(name);
          jtextListUsers.setText(null);
          for (String user : ListUser) {
              appendToPane(jtextListUsers, "@" + user);
          }
          appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          
          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);
          
          //authentification
          if(Server.isValidUser(name, password)) {
        	  // create new Read Thread
              read = new Read();
              read.start();
              jfr.remove(jtfName);
              jfr.remove(jtfPassword);
              jfr.remove(jtfport);
              jfr.remove(jtfAddr);
              jfr.remove(jcbtn);
              jfr.remove(jcbtnlog);
              jfr.add(jsbtn);
              jfr.add(jtextInputChatSP);
              jfr.add(jsbtndeco);
              jfr.revalidate();
              jfr.repaint();
              jtextFilDiscu.setBackground(Color.WHITE);
              jtextListUsers.setBackground(Color.WHITE);
              output.println(name);
              output.println(password);
          }
          else {
		        appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
		        appendToPane(jtextFilDiscu, "<span>Please enter correct nickname & password</span>");
		        jfr.revalidate();
		        jfr.repaint();
          }
          
        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });
    
    // ON INSCRIP 
    jcbtnlog.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent ae) {
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.remove(jcbtnlog);
          jfr.remove(jtfPassword);
  
          jfr.add(jtflogin);
          jfr.add(jtfpwd);
          jfr.add(jtfconfirmpwd);
          jfr.add(jlfpwd);
          jfr.add(jlfconfirmpwd);
          jfr.add(jsbtncon);
          jfr.revalidate();
          jfr.repaint();
        }
      });
    
    // ON CONFIRM
    jsbtncon.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent ae) {
        	
        	 if(String.valueOf(jtfpwd.getPassword()).equals(String.valueOf(jtfconfirmpwd.getPassword()))) {
        		 
        		 if(Server.addMember(jtflogin.getText(), String.valueOf(jtfpwd.getPassword()))) {
        			 JOptionPane.showMessageDialog(jfr, "Inscription effectué ! vous pouvez vous connecter");
                	 jfr.remove(jtflogin);
                     jfr.remove(jtfpwd);
                     jfr.remove(jtfconfirmpwd);
                     jfr.remove(jlfpwd);
                     jfr.remove(jlfconfirmpwd);
                     jfr.remove(jsbtncon);
          
                     jfr.add(jtfName);
                     jfr.add(jtfport);
                     jfr.add(jtfAddr);
                     jfr.add(jcbtn);
                     jfr.add(jtfPassword);
                     jfr.revalidate();
                 	 jfr.repaint();
        		 }
        		 else {
        			 JOptionPane.showMessageDialog(jfr, "User déjà existant, choisir un autre");
                	 jfr.revalidate();
                 	 jfr.repaint();
        		 }
        	 }
        	 else {
        		 JOptionPane.showMessageDialog(jfr, "Les mots de passe ne correspondent pas, veuillez vous réinscrire");
            	 jfr.revalidate();
             	 jfr.repaint();
        	 }
        }
      });
    
    // on deco
    jsbtndeco.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.add(jcbtnlog);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        ClientGui.ListUser.remove(name);
        jtextListUsers.setText(null);
        for (String user : ListUser) {
            appendToPane(jtextListUsers, "@" + user);
        }
        output.close();
      }
    });

  }

  // check if if all field are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JTextField jtf4;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JTextField jtf4, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jtf4 = jtf4;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  // envoi des messages
  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
  	ArrayList<String> dest = new ArrayList<>();
	if (message.equals("quit") || message.equals("QUIT")) {
		output.println("QUIT");
		output.flush();
	}
	else {
		for (String msg : Arrays.asList(message.split(" "))) {
			if (msg.startsWith("@")) dest.add(msg.substring(1));
		}
		
		String cmd = "";
		if (dest.size() == 1) cmd = "UNICAST";
		else if (dest.size() > 1) cmd = "MULTICAST";
		else {
			if (EmissionClient.isACommand(message)) {
				cmd = Arrays.asList(message.split(" ")).get(0);
			}else {
				cmd = "BROADCAST";
			}
		}
		output.println(cmd+"__"+message);
		output.flush();
	}
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
              appendToPane(jtextFilDiscu, message);
           }
          
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // send html to pane
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  
}
