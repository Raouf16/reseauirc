package model.server;

import java.util.ArrayList;

public class Group {
	private String operator;
	private String mode;
	private String topic; 
	private final ArrayList<String> members;
	
	public Group (String operator, String mode, String topic) {
		this.operator = operator; 
		this.mode = mode;
		this.topic = topic;
		this.members = new ArrayList<>();
		this.members.add(operator);
	}
	
	public Group (String operator, String mode, String topic,  ArrayList<String> members) {
		this.operator = operator; 
		this.mode = mode;
		this.topic = topic;
		this.members = members;
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
	
	public boolean equals(Object grp) {
		return ((String) grp).equals(topic); 
	}
}