package com.deloitte.domain;

public class Person {
	
	private String firstName;
	private String secondName;
	private Long contactNum;
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public Long getContactNum() {
		return contactNum;
	}
	public void setContactNum(Long contactNum) {
		this.contactNum = contactNum;
	}
	public Person(String firstName, String secondName, Long contactNum) {
		super();
		this.firstName = firstName;
		this.secondName = secondName;
		this.contactNum = contactNum;
	}
	@Override
	public String toString() {
		return "Person [firstName=" + firstName + ", secondName=" + secondName
				+ ", contactNum=" + contactNum + "]";
	}
	
	
	
	
}
