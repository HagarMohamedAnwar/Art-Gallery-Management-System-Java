package First;

import java.io.Serializable;
import java.util.Date;

public class Artist implements Serializable{
	private int artistID;
	private String name, artStyle;
	private Date dob;
	public Artist(int artistID, String name, String artStyle, Date dob) {
		this.name = name;
		this.artStyle = artStyle;
		this.dob = dob;
		this.artistID = artistID;
	}
	public int getArtistID() {
		return artistID;
	}
	public void setArtistID(int artistID) {
		this.artistID = artistID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArtStyle() {
		return artStyle;
	}
	public void setArtStyle(String artStyle) {
		this.artStyle = artStyle;
	}
	public Date getDOB() {
		return dob;
	}
	public void setDOB(Date dob) {
		this.dob = dob;
	}
	@Override
	public String toString() {
		return "Artist [name=" + name + ", artStyle=" + artStyle + ", dob=" + dob + "]";
	}
	
	

}
