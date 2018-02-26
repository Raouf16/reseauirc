package model.server;
//package model.server;

import java.util.ArrayList;

public class Group {
	private String operator;
	private String mode;
	private String topic; 
	private String password;
	private final ArrayList<String> members;
	
	public Group (String operator, String mode, String topic, String password) {
		this.operator = operator; 
		this.mode = mode;
		this.topic = topic;
		this.members = new ArrayList<>();
		this.members.add(operator);
		this.password = password;
	}
	
	public Group (String operator, String mode, String topic, String password, ArrayList<String> members) {
		this.operator = operator; 
		this.mode = mode;
		this.topic = topic;
		this.members = members;
		this.password = password;
	}
	
	public void user(String user) {
		this.members.add(user);
	}
	
	public String kick(String user) {
		if (user.equals(operator) ) {
			return "operator";
		}else {
			members.remove(user);
			return "deleted";
		}
	}
	
	public String getOperator () {
		return operator; 
	}
	
	public String getMode() {
		return topic; 
	}
	
	public void mode(String newMode) {
		this.mode = newMode;
	}
	
	public ArrayList<String> getMembers(){
		return members;
	}
	
	public void oper(String oper) {
		operator = oper; 
	}
	
	public void join (String client) {
		user(client);
	}
	
	public void topic(String newTopic) {
		topic = newTopic; 
	}
	
	public void setOper(String oper) {
		operator = oper; 
	}
	
	public void joinGroup(String client) {
		user(client);
	}
	
	public void setTopic(String newTopic) {
		topic = newTopic; 
	}
	

	
	private String getPassword() {
		return password;
	}
	
	public String getTopic() {
		return topic; 
	}
	
	public boolean equals(Object grp) {
		return ((String) grp).equals(topic); 
	}
}