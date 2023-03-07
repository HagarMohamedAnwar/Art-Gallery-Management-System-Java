package First;

public class Admin extends Person {
	private int adminID;
	private String GName;
	
	public Admin(String name, String GName, String email, String password, char gender, String address) {
		super(name, email, password, gender, address);
		this.GName = GName;
	}
	public Admin(int adminID, String name, String GName, String email, String password, char gender, String address) {
		super(name, email, password, gender, address);
		this.adminID = adminID;
		this.GName = GName;
	}
	public String getGName() {
		return GName;
	}
	public void setGName(String gName) {
		GName = gName;
	}
	public int getAdminID() {
		return adminID;
	}
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}
	@Override
	public String toString() {
		return super.toString() + " Admin IDs: " + adminID;
	}
	
	
	
}
