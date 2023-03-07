package First;

import java.util.Calendar;
import java.sql.Date;


public class Order {
	private float cost;
	private int OID;
	private Date date;
	Artwork artwork = new Artwork();
	public Order(int OID,float cost, Date date, Artwork artwork) {
		this.OID = OID;
		this.cost = cost;
		this.artwork = artwork;
		this.date = date;
	}
	public Order(float cost, Artwork artwork) {
		this.cost = cost;
		this.artwork = artwork;
		date = new Date(Calendar.getInstance().getTime().getTime());  
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public float getCost() {
		return cost;
	}
	public void setPrice(float cost) {
		this.cost = cost;
	}
	public int getOID() {
		return OID;
	}
	public void setOID(int oID) {
		this.OID = oID;
	}
	public Artwork getArtwork() {
		return artwork;
	}
	public void setArtwork(Artwork artwork) {
		this.artwork = artwork;
	}
	@Override
	public String toString() {
		return "Order [price=" + cost + ", artwork=" + artwork + "]";
	}
	
	

}
