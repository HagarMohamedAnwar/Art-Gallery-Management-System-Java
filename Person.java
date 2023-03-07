package First;
public class Person {
	private String name, email, password, address;
	private char gender;
	public Person(String name, String email, String password, char gender, String address) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.gender = gender;
		this.address = address;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public char getGender() {
		return gender;
	}
	public void setGender(char gender) {
		this.gender = gender;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "Person [name=" + name + ", email=" + email + ", password=" + password + ", address=" + address
				+ ", gender=" + gender + "]";
	}	
}
