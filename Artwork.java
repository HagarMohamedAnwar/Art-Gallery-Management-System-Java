package First;

import javafx.scene.image.Image;

public class Artwork {
	private String title;
	private int year;
	private float price;
	private Artist artist;
	private Image image;
	private String GName;
	private int artistID;
	
	public Artwork(String title, String GName,int year, float price, Image image, Artist artist) {
		this.title = title;
		this.year = year;
		this.price = price;
		this.image = image;
		this.artist = artist;
		this.GName = GName;
		artistID = artist.getArtistID();
	}
	public Artwork(String title, String GName, int year, float price, Image image) {
		this.title = title;
		this.year = year;
		this.price = price;
		this.image = image;
		this.GName = GName;
	}
	public String getGName() {
		return GName;
	}
	public void setGName(String gName) {
		GName = gName;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public Artwork() {
		// TODO Auto-generated constructor stub
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public Artist getArtist() {
		return artist;
	}
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	@Override
	public String toString() {
		return "Artwork [title=" + title + ", year=" + year + ", price=" + price + ", artist=" + artist + "]";
	}
	public int getArtistID() {
		return artistID;
	}
	public void setArtistID(int artistID) {
		this.artistID = artistID;
	}
	
	
}
