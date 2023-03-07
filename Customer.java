package First;

public class Customer  extends Person {
	private int CID;
	private String GName;
	
	public Customer(int CID,String name, String GName, String email, String password, char gender, String address) {
		super(name, email, password, gender,address);
		this.CID = CID;
		this.GName = GName;
	}
	public Customer(String name, String GName, String email, String password, char gender, String address) {
		super(name, email, password, gender,address);
		this.GName = GName;
	}
	public String getGName() {
		return GName;
	}
	public void setGName(String gName) {
		GName = gName;
	}
	public int getCID() {
		return CID;
	}
	public void setCID(int cID) {
		this.CID = cID;
	}
	@Override
	public String toString() {
		return super.toString() + " Customer IDs: " + CID;
	}

}
