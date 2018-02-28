package model.server;

import java.util.ArrayList;

public class Group {
	private ArrayList<String> operators;
	private String mode;
	private String topic; 
	private String password;
	private String drapeauTopic;
	private int maxUsers;
	private final ArrayList<String> members;
	
	public Group (ArrayList<String> operators, String mode, String topic, String password, ArrayList<String> members) {
		this.operators = operators;
		this.members = members; 
		this.mode = mode;
		this.topic = topic;
		this.password = password;
		this.maxUsers = 9999; //Default value
	}
	
	public void addMember(String user) {
		if (maxUsers == 9999){
		this.members.add(user);
	}
		else {
			if (members.size() < maxUsers) {
				this.members.add(user);
			}
		}
	}
	
	public void removeMember(String user) {
		members.remove(user);
	}
	
	public String kick(String user) {
		if (operators.contains(user)) {
			return "operator";
		}else {
			members.remove(user);
			return "deleted";
		}
	}
	
	public ArrayList<String> getOperators () {
		return operators; 
	}
	
	public String getMode() {
		return mode; 
	}
	
	public ArrayList<String> getMembers(){
		return members;
	}
	
	public void addOperator(String oper) {
		operators.add(oper); 
	}
	
	public void removeOperator(String oper) {
		operators.remove(oper);
	}
	
	public String getTopic(){
		return this.topic; 
	}

	public void setTopic(String login, String newTopic) {
		if (drapeauTopic.equals("t")) {
			if (operators.contains(login)){
				topic = newTopic;
			}
		}else {
			topic = newTopic;
		}
	}
	
	public void setDrapeauTopic(String drapeau) {
		this.drapeauTopic = drapeau;
	}
	
	public String getPassword(String pass) {
		if (pass.equals("sErVeR")) {
			return password;
		}
		else {
			return null;
		}
	}
	
	public boolean equals(Object grp) {
		return ((String) grp).equals(topic); 
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}
}