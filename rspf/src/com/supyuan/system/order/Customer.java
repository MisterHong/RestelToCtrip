package com.supyuan.system.order;

/**
 * 消费者
 * @author Administrator
 *
 */
public class Customer {

	private String GivenName;
	private String MiddleName;
	private String Surname;
	private String AgeQualifyingCode;
	private String Gender;
	private String Age;
	private String PhoneNumber;
	private String Email;
	private boolean isContact;
	
	public String getGivenName() {
		return GivenName;
	}
	public void setGivenName(String givenName) {
		GivenName = givenName;
	}
	public String getMiddleName() {
		return MiddleName;
	}
	public void setMiddleName(String middleName) {
		MiddleName = middleName;
	}
	public String getSurname() {
		return Surname;
	}
	public void setSurname(String surname) {
		Surname = surname;
	}
	public String getAgeQualifyingCode() {
		return AgeQualifyingCode;
	}
	public void setAgeQualifyingCode(String ageQualifyingCode) {
		AgeQualifyingCode = ageQualifyingCode;
	}
	public String getGender() {
		return Gender;
	}
	public void setGender(String gender) {
		Gender = gender;
	}
	public String getAge() {
		return Age;
	}
	public void setAge(String age) {
		Age = age;
	}
	public String getPhoneNumber() {
		return PhoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		PhoneNumber = phoneNumber;
	}
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
	public boolean isContact() {
		return isContact;
	}
	public void setContact(boolean isContact) {
		this.isContact = isContact;
	}
	
}
