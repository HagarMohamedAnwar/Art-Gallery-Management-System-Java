package First;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Main extends Application {

	ArrayList<Order> cart = new ArrayList<>();
	Gallery gallery = new Gallery("L'Avenir De L'Art", "Smart Village");
	Customer customer;
	Socket socket;

	private static Connection con;
	ObjectOutputStream toServer = null;
	int i = 0;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SQLException {
		// OPEN DATABASE CONNECTION
		try {
			String url = "jdbc:mysql://sql11.freesqldatabase.com:3306/sql11497350?useSSL=false";
			String user = "sql11497350";
			String password = "YvEGa2eDlB";
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			// Create a socket to connect to the server
			Socket socket = new Socket("localhost", 9745);

			// Create an output stream to send data to the server
			toServer = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			try {
				toServer.writeObject(ex);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		loginPage(primaryStage);
		primaryStage.show();
		primaryStage.setResizable(false);

		primaryStage.setOnCloseRequest(e -> {
			try {
				toServer.writeObject("Close");
				toServer.close();

			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, e1, "Closing", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

///////////////////////////////// LOGIN SCENE ///////////////////////////////////
	public void loginPage(Stage primaryStage) {
		// Labels
		Label lblPassword = new Label("Password");
		Label lblEmail = new Label("Email");

		// Text
		Text titleLogin = new Text("L'Avenir De L'Art Art Gallery");
		titleLogin.setFont(Font.font("Arial", FontWeight.NORMAL, 20));

		// Text Fields
		TextField txtEmail = new TextField();
		TextField txtPassword = new PasswordField();

		// Buttons
		Button btnLogin = new Button("Sign in");
		btnLogin.setOnAction(e -> {
			try {
				Boolean found = false;
				Statement stmt = con.createStatement();
				// Get all customers
				ResultSet rs = stmt.executeQuery("select * from Customer");

				// Loop to find the required customer with email and password given
				while (rs.next()) {
					String Email = rs.getString("Email");
					String pass = rs.getString("Password");
					if (Email.equals(txtEmail.getText()) && pass.equals(txtPassword.getText())) {
						// Found customer and stores it
						customer = new Customer(rs.getInt("CID"), rs.getString("Name"), gallery.getName(), Email, pass,
								rs.getString("Gender").charAt(0), rs.getString("Address"));
						found = true;
						break;
					}
				}
				if (found) {
					// show welcome message
					JOptionPane.showMessageDialog(null, "Login Successful", "Login", JOptionPane.INFORMATION_MESSAGE);
					cHomePage(primaryStage);
				} else {
					// if not found show message
					JOptionPane.showMessageDialog(null, "Invalid Credentials", "Login",
							JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject("Invalid Credentials\n");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			} catch (SQLException ex) {
				JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject(ex);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		Button btnSignUp = new Button("Sign up");
		btnSignUp.setOnAction(e -> {
			registerScene(primaryStage);
		});

		Button btnAdmin = new Button("Admin");
		btnAdmin.setOnAction(e -> {
			adminLoginScene(primaryStage);
		});

		// Panes and properties
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		pane.setPadding(new Insets(25, 25, 25, 25));

		pane.add(lblEmail, 0, 1);
		pane.add(txtEmail, 1, 1);
		pane.add(lblPassword, 0, 2);
		pane.add(txtPassword, 1, 2);

		HBox hbtn = new HBox(10);
		hbtn.setAlignment(Pos.CENTER);
		hbtn.getChildren().addAll(btnLogin, btnSignUp, btnAdmin);

		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().add(titleLogin);
		vbox.getChildren().add(pane);
		vbox.getChildren().add(hbtn);

		// Scene Properties
		Scene loginScene = new Scene(vbox, 400, 300);
		primaryStage.setScene(loginScene);
		primaryStage.setTitle("Login");
	}

/////////////////////////// ADMIN LOGIN SCENE ///////////////////////////////////
	private void adminLoginScene(Stage primaryStage) {

		// Text
		Text title = new Text("Admin Login");
		title.setStyle("-fx-font-size:30");

		// Labels
		Label lblPassword = new Label("Password");
		Label lblEmail = new Label("Email");

		// Text Fields
		TextField txtEmail = new TextField();
		TextField txtPassword = new PasswordField();

		// Buttons
		Button btnLogin = new Button("Sign in");
		btnLogin.setOnAction(e -> {
			try {
				Boolean found = false;
				Statement stmt = con.createStatement();
				// Get all admins
				ResultSet rs = stmt.executeQuery("select * from Admin");

				while (rs.next()) {
					String Email = rs.getString("Email");
					String pass = rs.getString("Password");
					// check if email and password are in database
					if (Email.equals(txtEmail.getText()) && pass.equals(txtPassword.getText())) {
						found = true;
						break;
					}
				}
				if (found) {
					JOptionPane.showMessageDialog(null, "Admin Login Successful", "Admin Login",
							JOptionPane.INFORMATION_MESSAGE);
					aHomePage(primaryStage);
				} else {
					JOptionPane.showMessageDialog(null, "Invalid Credentials", "Admin Login",
							JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject("Admin Invalid Credentials");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			} catch (SQLException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
				try {
					toServer.writeObject(ex);
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			}
		});

		Button btnCustomer = new Button("Customer");
		btnCustomer.setOnAction(e -> {
			loginPage(primaryStage);
		});

		// Panes and properties
		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setAlignment(Pos.CENTER);
		pane.setPadding(new Insets(25, 25, 25, 25));

		pane.add(lblEmail, 0, 1);
		pane.add(txtEmail, 1, 1);
		pane.add(lblPassword, 0, 2);
		pane.add(txtPassword, 1, 2);

		HBox hbtn = new HBox(10);
		hbtn.setAlignment(Pos.CENTER);
		hbtn.getChildren().addAll(btnLogin, btnCustomer);

		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().add(title);
		vbox.getChildren().add(pane);
		vbox.getChildren().add(hbtn);

		// Scene Properties
		Scene adminLoginScene = new Scene(vbox, 400, 300);
		primaryStage.setScene(adminLoginScene);
		primaryStage.setTitle("Admin Login");
	}

///////////////////////////// ADMIN HOMEPAGE ////////////////////////////////////
	private void aHomePage(Stage primaryStage) {

		// Texts
		Text title = new Text("Admin Homepage");
		title.setStyle("-fx-font-size:30");

		// Buttons
		Button btnArtworks = new Button("Artworks");
		btnArtworks.setOnAction(e -> {
			artwork(primaryStage);
		});

		Button btnArtists = new Button("Artists");
		btnArtists.setOnAction(e -> {
			artist(primaryStage);
		});

		Button btnLogout = new Button("Logout");
		btnLogout.setOnAction(e -> {
			adminLoginScene(primaryStage);
		});

		// Panes and properties
		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(title, btnArtworks, btnArtists, btnLogout);

		Scene scene = new Scene(vbox, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Admin Homepage");

	}

///////////////////////////// ARTIST HOMEPAGE ///////////////////////////////////
	private void artist(Stage primaryStage) {

		// Text
		Text title = new Text("Artists");
		title.setStyle("-fx-font-size:30");

		// Buttons
		Button btnAdd = new Button("Add Artist(s)");
		btnAdd.setOnAction(e -> {
			addArtist(primaryStage);
		});

		Button btnRemove = new Button("Remove Artist(s)");
		btnRemove.setOnAction(e -> {
			removeArtist(primaryStage);
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			aHomePage(primaryStage);
		});

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(title, btnAdd, btnRemove, btnBack);

		Scene scene = new Scene(vbox, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Admin Artists");
	}

///////////////////////////// REMOVE ARTIST /////////////////////////////////////
	private void removeArtist(Stage primaryStage) {
		// obeservablelist of ARTISTS, if something is added or removed from this list,
		// the update is also shown
		// to the combobox or table it is connected to;
		ObservableList<Artist> artists = FXCollections.observableArrayList();
		updateArtists(artists);

		ObservableList<Integer> artistIDs = FXCollections.observableArrayList();
		getArtistIDs(artists, artistIDs);

		// Text
		Text title = new Text("Remove Artist(s)");
		title.setStyle("-fx-font-size:30");

		// Labels
		Label lblArtist = new Label("Artist");

		// ComboBoxes
		ComboBox<Integer> cmbArtist = new ComboBox<>();

		cmbArtist.getItems().addAll(artistIDs);
		cmbArtist.setPromptText("Artist");

		// Buttons
		Button btnRemove = new Button("Remove");
		btnRemove.setOnAction(e -> {
			// if ComboBox doesn't have an artist selected show message
			if (cmbArtist.getValue() == null) {
				JOptionPane.showMessageDialog(null, "Choose artist to be deleted", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject("Choose artist to be deleted\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			} else {
				try {
					// delete artist from database
					PreparedStatement stmt = con.prepareStatement("Delete from Artist where AID = ?");
					stmt.setInt(1, cmbArtist.getValue());
					stmt.executeUpdate();
					JOptionPane.showMessageDialog(null, "Artist Deleted Successfuly", primaryStage.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);

					// update ComboBox artists numbers
					updateArtists(artists);
					cmbArtist.getItems().clear();
					getArtistIDs(artists, artistIDs);
					cmbArtist.getItems().addAll(artistIDs);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
			}
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			artist(primaryStage);
		});

		// TableView
		TableView<Artist> tv = new TableView<Artist>();

		// Table columns
		TableColumn<Artist, Integer> colArtistID = new TableColumn<>("Artist ID");
		colArtistID.setMinWidth(20);
		colArtistID.setCellValueFactory(new PropertyValueFactory<>("artistID"));

		TableColumn<Artist, String> colName = new TableColumn<>("Artist Name");
		colName.setMinWidth(20);
		colName.setCellValueFactory(new PropertyValueFactory<>("Name"));

		TableColumn<Artist, String> colArtStyle = new TableColumn<>("Art Style");
		colArtStyle.setMinWidth(20);
		colArtStyle.setCellValueFactory(new PropertyValueFactory<>("artStyle"));

		TableColumn<Artist, Date> colDOB = new TableColumn<>("Date Of Birth");
		colDOB.setMinWidth(20);
		colDOB.setCellValueFactory(new PropertyValueFactory<>("DOB"));

		// Add table columns to the table
		tv.getColumns().addAll(colArtistID, colName, colArtStyle, colDOB);

		tv.setItems(artists);
		// Panes

		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(25, 25, 25, 25));
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(lblArtist, cmbArtist, btnRemove, btnBack);

		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(25, 25, 25, 25));
		bp.setCenter(tv);
		bp.setBottom(hbox);

		Scene scene = new Scene(bp, 1000, 700);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Remove Artists");
	}

//////////////////////////// UPDATE ARTIST IDS COMBOBOX /////////////////////////
	private void updateArtists(ObservableList<Artist> artists) {
		artists.clear();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Artist");
			while (rs.next()) {
				artists.add(new Artist(rs.getInt("AID"), rs.getString("Name"), rs.getString("Art_Style"),
						rs.getDate("DOB")));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Artist Update ComboBox", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject("Artist Update CMB\n");
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}
	}

////////////////////////////// GET ARTIST IDS COMBOBOX //////////////////////////
	private void getArtistIDs(ObservableList<Artist> artists, ObservableList<Integer> artistIDs) {
		artistIDs.clear();
		for (Artist a : artists) {
			artistIDs.add(a.getArtistID());
		}
	}

/////////////////////////// ADD ARTIST //////////////////////////////////////////
	private void addArtist(Stage primaryStage) {
		// Obeservablelist for artist
		ObservableList<Artist> artist = FXCollections.observableArrayList();
		try {
			// retrive all artist from database
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Artist");
			while (rs.next()) {
				artist.add(new Artist(rs.getInt("AID"), rs.getString("Name"), rs.getString("Art_Style"),
						rs.getDate("DOB")));

			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}

		// Text
		Text title = new Text("Add Artist(s)");
		title.setStyle("-fx-font-size:30");

		// Labels
		Label lblAID = new Label("Artist ID");
		Label lblName = new Label("Name");
		Label lblArt_Style = new Label("Art Style");
		Label lblDOB = new Label("Date of Birth");

		// TextFields
		TextField txtAID = new TextField();
		TextField txtName = new TextField();
		TextField txtArt_Style = new TextField();

		// DatePicker
		DatePicker dp = new DatePicker();

		// ComboBoxes
		ObservableList<String> gallery = FXCollections.observableArrayList();

		// Fill ComboBoxes
		fillGallery(gallery);

		ComboBox<String> galleries = new ComboBox<>();
		galleries.getItems().addAll(gallery);
		galleries.setPromptText("Gallery");

		// Buttons
		Button btnAdd = new Button("Add");
		btnAdd.setOnAction(e -> {
			// check if all textboxes are filled and comboboxes are selected
			if (txtName.getText().isEmpty() || txtArt_Style.getText().isEmpty() || dp.getValue() == null) {
				JOptionPane.showMessageDialog(null, "Please fill all details", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject("Please fill all details\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			} else {
				try {

					// insert artist into the database
					PreparedStatement stmt = con
							.prepareStatement("insert into Artist (Name,Art_Style,DOB) values(?,?,?)");
					stmt.setString(1, txtName.getText());
					stmt.setString(2, txtArt_Style.getText());
					LocalDate ld = dp.getValue();
					Date date = Date.valueOf(ld);
					stmt.setDate(3, date);

					stmt.executeUpdate();
					// show that the artist is added successfuly
					JOptionPane.showMessageDialog(null, "Artwork Added Successfuly", primaryStage.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);

				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
			}
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			artist(primaryStage);
		});

		// TableView
		TableView<Artist> tv = new TableView<Artist>();

		TableColumn<Artist, Integer> colAID = new TableColumn<>("Artist ID");
		colAID.setMinWidth(20);
		colAID.setCellValueFactory(new PropertyValueFactory<>("artistID"));

		TableColumn<Artist, String> colName = new TableColumn<>("Name");
		colName.setMinWidth(20);
		colName.setCellValueFactory(new PropertyValueFactory<>("Name"));

		TableColumn<Artist, String> colartStyle = new TableColumn<>("Art Style");
		colartStyle.setMinWidth(20);
		colartStyle.setCellValueFactory(new PropertyValueFactory<>("artStyle"));

		TableColumn<Artist, Date> colDOB = new TableColumn<>("Date of Birth");
		colDOB.setMinWidth(20);
		colDOB.setCellValueFactory(new PropertyValueFactory<>("DOB"));

		tv.getColumns().addAll(colAID, colName, colartStyle, colDOB);

		tv.setItems(artist);

		// Panes
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setAlignment(Pos.CENTER);
		gp.setPadding(new Insets(25, 25, 25, 25));

		gp.add(lblName, 0, 0);
		gp.add(txtName, 1, 0);
		gp.add(lblArt_Style, 0, 1);
		gp.add(txtArt_Style, 1, 1);
		gp.add(lblDOB, 0, 2);
		gp.add(dp, 1, 2);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(gp, btnAdd, btnBack);

		BorderPane bp = new BorderPane();
		bp.setLeft(vbox);
		bp.setCenter(tv);

		Scene scene = new Scene(bp, 1000, 700);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Add Artist");

	}

//////////////////////////// ADMIN ARTWORKS SCENE ///////////////////////////////
	private void artwork(Stage primaryStage) {

		// Text
		Text title = new Text("Artworks");
		title.setStyle("-fx-font-size:30");

		// Buttons
		Button btnAdd = new Button("Add Artwork(s)");
		btnAdd.setOnAction(e -> {
			addArtwork(primaryStage);
		});

		Button btnRemove = new Button("Remove Artwork(s)");
		btnRemove.setOnAction(e -> {
			removeArtwork(primaryStage);
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			aHomePage(primaryStage);
		});

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(title, btnAdd, btnRemove, btnBack);

		Scene scene = new Scene(vbox, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Admin Artworks");
	}

/////////////////////////// ADMIN REMOVE ARTWORKS SCENE /////////////////////////
	private void removeArtwork(Stage primaryStage) {

		// obeservablelist of artworks, if something is added or removed from this list,
		// the update is also shown
		// to the combobox or table it is connected to;
		ObservableList<Artwork> artworks = FXCollections.observableArrayList();
		updateArtworks(artworks);

		ObservableList<String> artworkTitles = FXCollections.observableArrayList();
		getArtworksTitles(artworks, artworkTitles);

		// Text
		Text title = new Text("Remove Artwork(s)");
		title.setStyle("-fx-font-size:30");

		// Labels
		Label lblArtwork = new Label("Artwork");

		// ComboBoxes
		ComboBox<String> cmbArtwork = new ComboBox<>();

		cmbArtwork.getItems().addAll(artworkTitles);
		cmbArtwork.setPromptText("Artwork");

		// Buttons
		Button btnRemove = new Button("Remove");
		btnRemove.setOnAction(e -> {
			// if ComboBox doesn't have an artwork selected show message
			if (cmbArtwork.getValue().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Choose artwork to be deleted", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);

				try {
					toServer.writeObject("Choose artwork to be deleted\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			} else {
				try {
					// delete artwork from database
					PreparedStatement stmt = con.prepareStatement("Delete from Artwork where Title = ?");
					stmt.setString(1, cmbArtwork.getValue());
					stmt.executeUpdate();
					JOptionPane.showMessageDialog(null, "Artwork Delete Successfuly", primaryStage.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);
					// update ComboBox titles
					updateArtworks(artworks);
					cmbArtwork.getItems().clear();
					getArtworksTitles(artworks, artworkTitles);
					cmbArtwork.getItems().addAll(artworkTitles);
					cmbArtwork.setPromptText("Artwork");
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
			}
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			artwork(primaryStage);
		});

		// TableView
		TableView<Artwork> tv = new TableView<Artwork>();

		// Table columns
		TableColumn<Artwork, String> colTitle = new TableColumn<>("Title");
		colTitle.setMinWidth(20);
		colTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));

		TableColumn<Artwork, String> colGName = new TableColumn<>("Gallery Name");
		colGName.setMinWidth(20);
		colGName.setCellValueFactory(new PropertyValueFactory<>("GName"));

		TableColumn<Artwork, Integer> colArtist = new TableColumn<>("Artist ID");
		colArtist.setMinWidth(20);
		colArtist.setCellValueFactory(new PropertyValueFactory<>("artistID"));

		TableColumn<Artwork, Integer> colYear = new TableColumn<>("Year");
		colYear.setMinWidth(20);
		colYear.setCellValueFactory(new PropertyValueFactory<>("Year"));

		TableColumn<Artwork, Float> colPrice = new TableColumn<>("Price");
		colPrice.setMinWidth(20);
		colPrice.setCellValueFactory(new PropertyValueFactory<>("Price"));

		TableColumn<Artwork, Image> colImage = new TableColumn<>("Image");
		colImage.setMinWidth(20);
		colImage.setCellValueFactory(new PropertyValueFactory<>("Image"));

		// Add table columns to the table
		tv.getColumns().addAll(colTitle, colGName, colArtist, colYear, colPrice, colImage);

		tv.setItems(artworks);
		// Panes

		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(25, 25, 25, 25));
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(lblArtwork, cmbArtwork, btnRemove, btnBack);

		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(25, 25, 25, 25));
		bp.setCenter(tv);
		bp.setBottom(hbox);

		Scene scene = new Scene(bp, 1000, 700);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Remove Artworks");
	}

///////////////////////// UPDATE ARTWORKS LIST TO BE VIEWED /////////////////////
	private void updateArtworks(ObservableList<Artwork> artworks) {
		artworks.clear();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Artwork");
			while (rs.next()) {
				PreparedStatement stmt2 = con.prepareStatement("Select * from Artist where AID = ?");
				stmt2.setInt(1, rs.getInt("AID"));
				ResultSet rs2 = stmt2.executeQuery();
				if (rs2.next()) {
					Blob blob = rs.getBlob("Image");
					InputStream in = blob.getBinaryStream();
					BufferedImage image = ImageIO.read(in);
					Image i = SwingFXUtils.toFXImage(image, null);

					Artist artist = new Artist(rs2.getInt("AID"), rs2.getString("Name"), rs2.getString("Art_Style"),
							rs2.getDate("DOB"));
					artworks.add(new Artwork(rs.getString("Title"), rs.getString("GName"), rs.getInt("Year"),
							rs.getFloat("Price"), i, artist));

				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Artwork Update", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}
	}

////////////////////// GET ARTWORKS' TITLES IN A COMBOBOX ///////////////////////
	private void getArtworksTitles(ObservableList<Artwork> artworks, ObservableList<String> titles) {
		titles.clear();
		for (Artwork a : artworks) {
			titles.add(a.getTitle());
		}
	}

///////////////////////////// ARTWORKS EDIT SCENE ///////////////////////////////
	private void addArtwork(Stage primaryStage) {

		// Obeservablelist for artworks
		ObservableList<Artwork> artworks = FXCollections.observableArrayList();
		try {
			// retrive all artworks from database
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Artwork");
			while (rs.next()) {
				// add all artworks retrived to the observablelist and so tableview is updated
				PreparedStatement stmt2 = con.prepareStatement("Select * from Artist where AID = ?");
				stmt2.setInt(1, rs.getInt("AID"));
				ResultSet rs2 = stmt2.executeQuery();
				if (rs2.next()) {
					// get images
					Blob blob = rs.getBlob("Image");
					InputStream in = blob.getBinaryStream();
					BufferedImage image = ImageIO.read(in);
					Image i = SwingFXUtils.toFXImage(image, null);

					Artist artist = new Artist(rs2.getInt("AID"), rs2.getString("Name"), rs2.getString("Art_Style"),
							rs2.getDate("DOB"));
					// add artworks to the obervablelist
					artworks.add(new Artwork(rs.getString("Title"), rs.getString("GName"), rs.getInt("Year"),
							rs.getFloat("Price"), i, artist));

				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}

		// Text
		Text title = new Text("Add Artwork(s)");
		title.setStyle("-fx-font-size:30");

		// Labels
		Label lblTitle = new Label("Title");
		Label lblGallery = new Label("Gallery");
		Label lblYear = new Label("Year");
		Label lblPrice = new Label("Price");
		Label lblArtist = new Label("Artist");
		Label lblArtistName = new Label("Artist");
		Label lblImage = new Label("Image");
		Label lblImageName = new Label();
		lblImageName.setWrapText(true);
		lblImageName.setMaxWidth(50);

		// TextFields
		TextField txtTitle = new TextField();
		TextField txtYear = new TextField();
		TextField txtPrice = new TextField();

		// ComboBoxes
		ObservableList<String> gallery = FXCollections.observableArrayList();
		ObservableList<String> artist = FXCollections.observableArrayList();

		// Fill ComboBoxes
		fillGallery(gallery);
		fillArtist(artist);

		ComboBox<String> galleries = new ComboBox<>();
		galleries.getItems().addAll(gallery);
		galleries.setPromptText("Gallery");

		ComboBox<String> artists = new ComboBox<>();
		artists.getItems().addAll(artist);
		artists.setPromptText("Artist");

		artists.setOnAction(e -> {
			try {
				PreparedStatement stmt = con.prepareStatement("Select Name from Artist where AID = ?");
				stmt.setInt(1, Integer.parseInt(artists.getValue()));
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					lblArtistName.setText(rs.getString("Name"));
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject(ex);
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			}
		});

		// Buttons
		Button btnAdd = new Button("Add");
		btnAdd.setOnAction(e -> {
			// check if all textboxes are filled and comboboxes are selected
			if (txtTitle.getText().isEmpty() || txtYear.getText().isEmpty() || txtPrice.getText().isEmpty()
					|| galleries.getValue().isEmpty() || artists.getValue().isEmpty()
					|| lblImageName.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please fill all details", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject("Please fill all details\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			} else {
				try {

					// insert artworks into the database
					InputStream in = new FileInputStream(lblImageName.getText());
					PreparedStatement stmt = con.prepareStatement(
							"insert into Artwork (Title,GName,Year,Price,Image,AID) values(?,?,?,?,?,?)");
					stmt.setString(1, txtTitle.getText());
					stmt.setString(2, galleries.getValue());
					stmt.setInt(3, Integer.parseInt(txtYear.getText()));
					stmt.setFloat(4, Float.parseFloat(txtPrice.getText()));
					stmt.setBlob(5, in);
					stmt.setInt(6, Integer.parseInt(artists.getValue()));
					stmt.executeUpdate();

					// show that the artwork is added successfuly
					JOptionPane.showMessageDialog(null, "Artwork Added Successfuly", primaryStage.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);

					// get artist and add it to the artwork
					stmt = con.prepareStatement("select * from Artist where AID = ?");
					stmt.setInt(1, Integer.parseInt(artists.getValue()));
					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						Artist artistt = new Artist(rs.getInt("AID"), rs.getString("Name"), rs.getString("Art_Style"),
								rs.getDate("DOB"));
						artworks.add(new Artwork(txtTitle.getText(), galleries.getValue(),
								Integer.parseInt(txtYear.getText()), Float.parseFloat(txtPrice.getText()),
								new Image(lblImageName.getText()), artistt));
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
			}
		});

		Button btnBrowse = new Button("Browse");
		btnBrowse.setOnAction(e -> {
			File file = new FileChooser().showOpenDialog(primaryStage);
			Image img = new Image(file.getPath());
			lblImageName.setText(img.getUrl());
		});

		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			artwork(primaryStage);
		});

		// TableView
		TableView<Artwork> tv = new TableView<Artwork>();

		TableColumn<Artwork, String> colTitle = new TableColumn<>("Title");
		colTitle.setMinWidth(20);
		colTitle.setCellValueFactory(new PropertyValueFactory<>("Title"));

		TableColumn<Artwork, String> colGName = new TableColumn<>("Gallery Name");
		colGName.setMinWidth(20);
		colGName.setCellValueFactory(new PropertyValueFactory<>("GName"));

		TableColumn<Artwork, Integer> colArtist = new TableColumn<>("Artist ID");
		colArtist.setMinWidth(20);
		colArtist.setCellValueFactory(new PropertyValueFactory<>("artistID"));

		TableColumn<Artwork, Integer> colYear = new TableColumn<>("Year");
		colYear.setMinWidth(20);
		colYear.setCellValueFactory(new PropertyValueFactory<>("Year"));

		TableColumn<Artwork, Float> colPrice = new TableColumn<>("Price");
		colPrice.setMinWidth(20);
		colPrice.setCellValueFactory(new PropertyValueFactory<>("Price"));

		TableColumn<Artwork, Image> colImage = new TableColumn<>("Image");
		colImage.setMinWidth(20);
		colImage.setCellValueFactory(new PropertyValueFactory<>("Image"));

		tv.getColumns().addAll(colTitle, colGName, colArtist, colYear, colPrice, colImage);

		tv.setItems(artworks);
		// Panes
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setAlignment(Pos.CENTER);
		gp.setPadding(new Insets(25, 25, 25, 25));

		gp.add(lblTitle, 0, 0);
		gp.add(txtTitle, 1, 0);
		gp.add(lblGallery, 0, 1);
		gp.add(galleries, 1, 1);
		gp.add(lblYear, 0, 2);
		gp.add(txtYear, 1, 2);
		gp.add(lblPrice, 0, 3);
		gp.add(txtPrice, 1, 3);
		gp.add(lblArtist, 0, 4);
		gp.add(artists, 1, 4);
		gp.add(lblArtistName, 1, 5);

		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER);
		hbox.setPadding(new Insets(25, 25, 25, 25));
		hbox.getChildren().addAll(lblImage, lblImageName, btnBrowse);
		hbox.setMaxWidth(300);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(gp, hbox, btnAdd, btnBack);

		BorderPane bp = new BorderPane();
		bp.setLeft(vbox);
		bp.setCenter(tv);

		Scene scene = new Scene(bp, 1000, 700);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Add Artworks");
	}

//////////////////////////// FILL GALLERY COMBOBOX //////////////////////////////
	private void fillGallery(ObservableList<String> gallery) {
		try {
			PreparedStatement stmt = con.prepareStatement("Select * from Gallery");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				gallery.add(rs.getString("GName"));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Gallery ComboBox", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}
	}

//////////////////////////// FILL ARTIST COMBOBOX ///////////////////////////////
	private void fillArtist(ObservableList<String> artist) {
		try {
			PreparedStatement stmt = con.prepareStatement("Select * from Artist");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				artist.add(rs.getString("AID"));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Artist ComboBox", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}
	}

///////////////////////////////// CUSTOMER HOMEPAGE SCENE //////////////////////////////////////
	public void cHomePage(Stage primaryStage) {
		// Text
		Text titleCHomepage = new Text("Homepage\nWelcome!");
		titleCHomepage.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
		titleCHomepage.setTextAlignment(TextAlignment.CENTER);
		titleCHomepage.setStyle("-fx-font-size:30-");

		// Buttons
		Button btnShop = new Button("Shop");
		btnShop.setOnAction(e -> {
			try {
				shopScene(primaryStage);
			} catch (IOException | SQLException e1) {
				try {
					toServer.writeObject(e1);
				} catch (IOException exx) {
					exx.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, e1, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
			}
		});

		Button btnOrders = new Button("Order(s)");
		btnOrders.setOnAction(e -> {
			try {
				orderScene(primaryStage);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject(ex);
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			}
		});

		Button btnProfile = new Button("Profile");
		btnProfile.setOnAction(e -> {
			profileScene(primaryStage);
		});

		Button btnLogOut = new Button("Logout");
		btnLogOut.setOnAction(e -> {
			loginPage(primaryStage);
		});

		// Panes and properties
		VBox cHomepagePane = new VBox(10);
		cHomepagePane.setAlignment(Pos.CENTER);
		cHomepagePane.getChildren().addAll(titleCHomepage, btnShop, btnOrders, btnProfile, btnLogOut);

		// Scenes
		Scene cHomepage = new Scene(cHomepagePane, 400, 300);
		primaryStage.setScene(cHomepage);
	}

//////////////////////// Profile Scene ////////////////////////////////////////////
	private void profileScene(Stage primaryStage) {
		Label lblName = new Label("Name: "+ customer.getName());
		Label lblAddress = new Label("Address: "+ customer.getAddress());
		Label lblEmail = new Label("Email: "+ customer.getEmail());
		Label lblGender = new Label("Gender: "+ customer.getGender());
		
		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			cHomePage(primaryStage);
		});
		// Panes and properties
					BorderPane pane = new BorderPane();
					pane.setPadding(new Insets(25, 25, 25, 25));


					VBox profile = new VBox(10);
					profile.setAlignment(Pos.CENTER);
					Text title = new Text();
					profile.getChildren().addAll(title, lblName, lblAddress, lblEmail, lblGender, btnBack);
					pane.setCenter(profile);

					Scene scene = new Scene(pane, 450, 470);
					primaryStage.setScene(scene);
					primaryStage.setTitle("Profile");
	}

////////////////////////////////////// REGISTER SCENE //////////////////////////////////////////
	public void registerScene(Stage primaryStage) {
		// Labels
		Label lblName = new Label("Name:");
		Label lblAddress = new Label("Address:");
		Label lblEmail = new Label("Email:");
		Label lblGender = new Label("Gender:");
		Label lblPassword = new Label("Password:");
		Label lblPasswordConfirm = new Label("Confirm Password:");

		// Text
		Text titleRegister = new Text("Register");
		titleRegister.setFont(Font.font("Arial", FontWeight.NORMAL, 20));

		// TextFields
		TextField txtName = new TextField();
		TextField txtAddress = new TextField();
		TextField txtEmail = new TextField();
		TextField txtPassword = new PasswordField();
		TextField txtPasswordConfirm = new PasswordField();

		// Radio Buttons
		RadioButton male = new RadioButton("Male");
		RadioButton female = new RadioButton("Female");

		// Toggle Group
		ToggleGroup gender = new ToggleGroup();
		male.setToggleGroup(gender);
		female.setToggleGroup(gender);

		// Buttons
		Button btnSubmitRegister = new Button("Register");
		btnSubmitRegister.setOnAction(e -> {
			String gend = "";
			if (male.isSelected())
				gend = "M";
			else if (female.isSelected())
				gend = "F";
			if (checkRegister(txtName.getText(), txtEmail.getText(), txtPassword.getText(),
					txtPasswordConfirm.getText(), gend, txtAddress.getText())) {
				JOptionPane.showMessageDialog(null, "Registration Successful!", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				loginPage(primaryStage);
			}
		});

		Button btnBack = new Button("Already Have an Account?");
		btnBack.setOnAction(e -> {
			loginPage(primaryStage);
		});

		// Panes and properties
		GridPane pane1 = new GridPane();
		pane1.setHgap(10);
		pane1.setVgap(10);
		pane1.setAlignment(Pos.CENTER);
		pane1.setPadding(new Insets(25, 25, 25, 25));

		pane1.add(lblName, 0, 1);
		pane1.add(txtName, 1, 1);
		pane1.add(lblEmail, 0, 2);
		pane1.add(txtEmail, 1, 2);
		pane1.add(lblPassword, 0, 3);
		pane1.add(txtPassword, 1, 3);
		pane1.add(lblPasswordConfirm, 0, 4);
		pane1.add(txtPasswordConfirm, 1, 4);
		pane1.add(lblGender, 0, 5);
		pane1.add(male, 1, 5);
		pane1.add(female, 1, 6);
		pane1.add(lblAddress, 0, 7);
		pane1.add(txtAddress, 1, 7);

		HBox hbox1 = new HBox(10);
		hbox1.getChildren().addAll(btnSubmitRegister, btnBack);
		hbox1.setAlignment(Pos.CENTER);

		VBox vbox1 = new VBox();
		vbox1.setAlignment(Pos.CENTER);
		vbox1.getChildren().add(titleRegister);
		vbox1.getChildren().add(pane1);
		vbox1.getChildren().add(hbox1);

		Scene scene = new Scene(vbox1, 400, 400);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Register");
	}

////////////////////////////////////// SHOP SCENE //////////////////////////////////////////////
	public void shopScene(Stage primaryStage) throws SQLException, IOException {

		// ArrayList for artwork
		ArrayList<Artwork> li = new ArrayList<>();

		// Position at first Artwork
		i = 0;

		// Get Artwork and their artists informations
		try {
			Artist artist;
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from Artwork");
			// Get images and place them in artworks arraylist
			while (rs.next()) {
				PreparedStatement stmt2 = con.prepareStatement("select * from Artist where AID = ?");
				stmt2.setInt(1, rs.getInt("AID"));
				ResultSet rs2 = stmt2.executeQuery();

				// Get image from database
				Blob blob = rs.getBlob("Image");
				InputStream in = blob.getBinaryStream();
				BufferedImage image = ImageIO.read(in);
				Image i = SwingFXUtils.toFXImage(image, null);

				if (rs2.next()) {

					// get artwork's artist
					artist = new Artist(rs2.getInt("AID"), rs2.getString("Name"), rs2.getString("Art_Style"),
							rs2.getDate("DOB"));

					// add artwork to artwork arraylist
					li.add(new Artwork(rs.getString("Title"), gallery.getName(), rs.getInt("Year"),
							rs.getFloat("Price"), i, artist));
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Shop", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}

		// Labels
		Label lblTitle = new Label("Title: " + li.get(i).getTitle());
		Label lblPrice = new Label("Price: $" + li.get(i).getPrice());
		Label lblYear = new Label("Year: " + li.get(i).getYear());
		Label lblArtist = new Label("Artist: " + li.get(i).getArtist().getName());
		Label lblDOB = new Label("Date Of Birth: " + li.get(i).getArtist().getDOB());
		Label lblArtStyle = new Label("Art Style: " + li.get(i).getArtist().getArtStyle());

		// Text
		Text title = new Text("Shop");
		title.setStyle("-fx-font-size:30");

		// ImageView
		ImageView iv = new ImageView(li.get(i).getImage());
		iv.setFitWidth(170);
		iv.setFitHeight(170);

		// Buttons
		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			cHomePage(primaryStage);
		});

		Button btnAddToCart = new Button("Add to cart");
		btnAddToCart.setOnAction(e -> {
			cart.add(new Order(li.get(i).getPrice(), li.get(i)));
		});

		Button btnCart = new Button("View Cart");
		btnCart.setOnAction(e -> {
			if (cart.size() > 0)
				cartScene(primaryStage);
			else {
				JOptionPane.showMessageDialog(null, "Cart is empty!", primaryStage.getTitle(),
						JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject("Cart is Empty\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			}
		});

		Button btnNext = new Button(">");
		btnNext.setStyle("-fx-font-size:20");
		btnNext.setOnAction(e -> {
			i++;
			if (li.size() <= i) {
				i = 0;
			}
			iv.setImage(li.get(i).getImage());
			lblTitle.setText("Title: " + li.get(i).getTitle());
			lblPrice.setText("Price: $" + li.get(i).getPrice());
			lblYear.setText("Year: " + li.get(i).getYear());
			lblArtist.setText("Artist: " + li.get(i).getArtist().getName());
			lblDOB.setText("Date Of Birth: " + li.get(i).getArtist().getDOB());
			lblArtStyle.setText("Art Style: " + li.get(i).getArtist().getArtStyle());
		});

		Button btnPrev = new Button("<");
		btnPrev.setStyle("-fx-font-size:20");
		btnPrev.setOnAction(e -> {
			i--;
			if (i < 0) {
				i = li.size() - 1;
			}
			iv.setImage(li.get(i).getImage());
			lblTitle.setText("Title: " + li.get(i).getTitle());
			lblPrice.setText("Price: $" + li.get(i).getPrice());
			lblYear.setText("Year: " + li.get(i).getYear());
			lblArtist.setText("Artist: " + li.get(i).getArtist().getName());
			lblDOB.setText("Date Of Birth: " + li.get(i).getArtist().getDOB());
			lblArtStyle.setText("Art Style: " + li.get(i).getArtist().getArtStyle());
		});

		// Panes and properties
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(25, 25, 25, 25));

		pane.setRight(btnNext);
		pane.setLeft(btnPrev);
		BorderPane.setAlignment(btnNext, Pos.CENTER);
		BorderPane.setAlignment(btnPrev, Pos.CENTER);

		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(btnAddToCart, btnCart, btnBack);

		VBox shop = new VBox(10);
		shop.setAlignment(Pos.CENTER);

		shop.getChildren().addAll(title, iv, lblTitle, lblPrice, lblYear, lblArtist, lblDOB, lblArtStyle, hbox);
		pane.setCenter(shop);

		Scene scene = new Scene(pane, 450, 470);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Shop");
	}

//////////////////////////////////////ORDER SCENE //////////////////////////////////////////////
	public void orderScene(Stage primaryStage) throws SQLException, IOException {
		// ArrayList for order
		ArrayList<Order> li = new ArrayList<>();
		// Position at first order
		i = 0;
		// Get Artwork and their artists informations
		try {
			PreparedStatement stmt = con.prepareStatement("select * from Orderr where CID =?");
			stmt.setInt(1, customer.getCID());
			ResultSet rs = stmt.executeQuery();
			// Get images and place them in artworks arraylist
			while (rs.next()) {
				PreparedStatement stmt2 = con.prepareStatement("select * from Artwork where Title =?");
				stmt2.setString(1, rs.getString("Title"));
				ResultSet rs2 = stmt2.executeQuery();
				if (rs2.next()) {
					// get images
					Blob blob = rs2.getBlob("Image");
					InputStream in = blob.getBinaryStream();
					BufferedImage image = ImageIO.read(in);
					Image i = SwingFXUtils.toFXImage(image, null);

					Artwork artwork = new Artwork(rs2.getString("Title"), rs2.getString("GName"), rs2.getInt("Year"),
							rs2.getFloat("Price"), i);
					li.add(new Order(rs.getInt("OID"), rs.getFloat("cost"), rs.getDate("date"), artwork));
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "Order", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
		}
		// Labels
		Label lblOrderID = new Label("Order ID: " + li.get(i).getOID());
		Label lblCost = new Label("Cost: $" + li.get(i).getCost());
		Label lblDate = new Label("Date of Order: " + li.get(i).getDate());
		Label lblTitle = new Label("Title: " + li.get(i).getArtwork().getTitle());
		Label lblYear = new Label("Year: " + li.get(i).getArtwork().getYear());

		// Text
		Text title = new Text("Order");
		title.setStyle("-fx-font-size:30");

		// ImageView
		ImageView iv = new ImageView(li.get(i).getArtwork().getImage());
		iv.setFitWidth(170);
		iv.setFitHeight(170);

		// Buttons
		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {
			cHomePage(primaryStage);
		});

		Button btnNext = new Button(">");
		btnNext.setStyle("-fx-font-size:20");
		btnNext.setOnAction(e -> {
			i++;
			if (li.size() <= i) {
				i = 0;
			}
			iv.setImage(li.get(i).getArtwork().getImage());
			lblOrderID.setText("Order ID: " + li.get(i).getOID());
			lblCost.setText("Cost: $" + li.get(i).getCost());
			lblDate.setText("Date of Order: " + li.get(i).getDate());
			lblTitle.setText("Title: " + li.get(i).getArtwork().getTitle());
			lblYear.setText("Year: " + li.get(i).getArtwork().getYear());
		});

		Button btnPrev = new Button("<");
		btnPrev.setStyle("-fx-font-size:20");
		btnPrev.setOnAction(e -> {
			i--;
			if (i < 0) {
				i = li.size() - 1;
			}
			iv.setImage(li.get(i).getArtwork().getImage());
			lblOrderID.setText("Order ID: " + li.get(i).getOID());
			lblCost.setText("Cost: $" + li.get(i).getCost());
			lblDate.setText("Date of Order: " + li.get(i).getDate());
			lblTitle.setText("Title: " + li.get(i).getArtwork().getTitle());
			lblYear.setText("Year: " + li.get(i).getArtwork().getYear());
		});

// Panes and properties
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(25, 25, 25, 25));

		pane.setRight(btnNext);
		pane.setLeft(btnPrev);
		BorderPane.setAlignment(btnNext, Pos.CENTER);
		BorderPane.setAlignment(btnPrev, Pos.CENTER);

		VBox order = new VBox(10);
		order.setAlignment(Pos.CENTER);

		order.getChildren().addAll(title, iv, lblOrderID, lblCost, lblDate, lblTitle, lblYear, btnBack);
		pane.setCenter(order);

		Scene scene = new Scene(pane, 450, 470);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Orders");

	}

////////////////////////////////////// CART SCENE //////////////////////////////////////////////
	private void cartScene(Stage primaryStage) {

		// Show first order
		i = 0;

		// Labels
		Label lblTitle = new Label("Title: " + cart.get(i).getArtwork().getTitle());
		Label lblPrice = new Label("Price: $" + cart.get(i).getArtwork().getPrice());
		Label lblYear = new Label("Year: " + cart.get(i).getArtwork().getYear());
		Label lblArtist = new Label("Artist: " + cart.get(i).getArtwork().getArtist().getName());
		Label lblDOB = new Label("Date Of Birth: " + cart.get(i).getArtwork().getArtist().getDOB());
		Label lblArtStyle = new Label("Art Style: " + cart.get(i).getArtwork().getArtist().getArtStyle());

		// Text
		Text title = new Text("Cart");
		title.setStyle("-fx-font-size:30");

		// ImageView
		ImageView iv = new ImageView(cart.get(i).getArtwork().getImage());
		iv.setFitWidth(170);
		iv.setFitHeight(170);

		// ComboBox
		String[] paymentOptions = { "Cash", "Online" };
		ObservableList<String> payments = FXCollections.observableArrayList(paymentOptions);

		ComboBox<String> payment = new ComboBox<>();
		payment.getItems().addAll(payments);
		payment.setPromptText("Payment Method");

		// Buttons
		Button btnBack = new Button("Back");
		btnBack.setOnAction(e -> {

			// get back to shop scene
			try {
				shopScene(primaryStage);
			} catch (SQLException | IOException e1) {
				try {
					toServer.writeObject(e1);
				} catch (IOException exx) {
					exx.printStackTrace();
				}
				e1.printStackTrace();
			}
		});

		Button btnDelete = new Button("Remove From Cart");
		btnDelete.setOnAction(e -> {
			cart.remove(i);
		});

		Button btnNext = new Button(">");
		btnNext.setStyle("-fx-font-size:20");
		btnNext.setOnAction(e -> {
			i++;
			if (cart.size() <= i) {
				i = 0;
			}
			iv.setImage(cart.get(i).getArtwork().getImage());
			lblTitle.setText("Title: " + cart.get(i).getArtwork().getTitle());
			lblPrice.setText("Price: $" + cart.get(i).getArtwork().getPrice());
			lblYear.setText("Year: " + cart.get(i).getArtwork().getYear());
			lblArtist.setText("Artist: " + cart.get(i).getArtwork().getArtist().getName());
			lblDOB.setText("Date Of Birth: " + cart.get(i).getArtwork().getArtist().getDOB());
			lblArtStyle.setText("Art Style: " + cart.get(i).getArtwork().getArtist().getArtStyle());
		});

		Button btnPrev = new Button("<");
		btnPrev.setStyle("-fx-font-size:20");
		btnPrev.setOnAction(e -> {
			i--;
			if (i < 0) {
				i = cart.size() - 1;
			}
			iv.setImage(cart.get(i).getArtwork().getImage());
			lblTitle.setText("Title: " + cart.get(i).getArtwork().getTitle());
			lblPrice.setText("Price: $" + cart.get(i).getArtwork().getPrice());
			lblYear.setText("Year: " + cart.get(i).getArtwork().getYear());
			lblArtist.setText("Artist: " + cart.get(i).getArtwork().getArtist().getName());
			lblDOB.setText("Date Of Birth: " + cart.get(i).getArtwork().getArtist().getDOB());
			lblArtStyle.setText("Art Style: " + cart.get(i).getArtwork().getArtist().getArtStyle());
		});

		Button btnPlaceOrder = new Button("Place Order(s)");
		btnPlaceOrder.setOnAction(e -> {
			if (payment.getValue() != null) {
				try {
					for (Order o : cart) {
						PreparedStatement stmt = con
								.prepareStatement("insert into Orderr (CID,Title,Date,Cost,Payment) values(?,?,?,?,?)");
						stmt.setInt(1, customer.getCID());
						stmt.setString(2, o.getArtwork().getTitle());
						stmt.setDate(3, o.getDate());
						stmt.setFloat(4, o.getCost());
						stmt.setString(5, payment.getValue());
						stmt.execute();
					}
					// Empty Cart
					cart.clear();

					// open shop scene
					JOptionPane.showMessageDialog(null, "Order(s) Place", primaryStage.getTitle(),
							JOptionPane.INFORMATION_MESSAGE);
					shopScene(primaryStage);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex, primaryStage.getTitle(), JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}

			} else {
				JOptionPane.showMessageDialog(null, "Choose Payment Method", "Cart", JOptionPane.INFORMATION_MESSAGE);
				try {
					toServer.writeObject("Choose Payment Method\n");
				} catch (IOException exx) {
					exx.printStackTrace();
				}
			}
		});

		// Panes and properties
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(25, 25, 25, 25));
		pane.setRight(btnNext);
		pane.setLeft(btnPrev);
		BorderPane.setAlignment(btnNext, Pos.CENTER);
		BorderPane.setAlignment(btnPrev, Pos.CENTER);

		HBox hbox = new HBox(10);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(btnPlaceOrder, btnDelete, btnBack);

		VBox orders = new VBox(10);
		orders.setAlignment(Pos.CENTER);
		orders.getChildren().addAll(title, iv, lblTitle, lblPrice, lblYear, lblArtist, lblDOB, lblArtStyle, payment,
				hbox);

		pane.setCenter(orders);

		Scene scene = new Scene(pane, 450, 500);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Cart");
	}

////////////////////////////// CHECK REGISTERATION IF VALID /////////////////////////////////////
	public boolean checkRegister(String name, String email, String password, String passwordConfirm, String gender,
			String address) {

		try {
			String msg = "Please enter: ";
			if (name.equals("")) {
				msg += "Name ";
			}
			if (email.equals("")) {
				msg += "Email ";
			}
			if (password.equals("")) {
				msg += "Password ";
			}
			if (passwordConfirm.equals("")) {
				msg += "Password confirmation ";
			}
			if (gender == "") {
				msg += "Gender ";
			}
			if (address.equals("")) {
				msg += "Address";
			}
			if (!msg.equals("Please enter: ")) {
				JOptionPane.showMessageDialog(null, msg, "PopUp Dialog", JOptionPane.INFORMATION_MESSAGE);
				return false;
			} else if (!password.equals(passwordConfirm)) {
				JOptionPane.showMessageDialog(null, "Passwords mismatch", "PopUp Dialog",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			} else {
				checkPassword(password);
				try {
					String qu = "insert into Customer (Email,Password,Name,Address,Gender) values(?,?,?,?,?)";
					PreparedStatement stmt = con.prepareStatement(qu);
					stmt.setString(1, email);
					stmt.setString(2, password);
					stmt.setString(3, name);
					stmt.setString(4, address);
					stmt.setString(5, gender);
					stmt.executeUpdate();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex, "Registeration Failed", JOptionPane.INFORMATION_MESSAGE);
					try {
						toServer.writeObject(ex);
					} catch (IOException exx) {
						exx.printStackTrace();
					}
				}
				return true;
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex, "PopUp Dialog", JOptionPane.INFORMATION_MESSAGE);
			try {
				toServer.writeObject(ex);
			} catch (IOException exx) {
				exx.printStackTrace();
			}
			return false;
		}
	}

///////////////////////////// CHECK PASSWORD IF VALID //////////////////////////////////////////
	private void checkPassword(String password) throws myExceptions {
		if (password.length() < 7) {
			throw new myExceptions();
		}
	}
}
