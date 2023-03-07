package First;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {
	
	int clientNo;
	TextArea ta = new TextArea();
	TextArea ta2 = new TextArea();
	Label clients = new Label("Number of Clients: " + clientNo);

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Panel p to hold the label and text field

		clients.setAlignment(Pos.CENTER);
		clients.setStyle("-fx-font-size:30");
		BorderPane mainPane = new BorderPane();
		ta.setWrapText(true);
		ta.setMaxWidth(450);
		// Text area to display contents
		ta.setEditable(false);
		mainPane.setTop(new ScrollPane(ta));
		mainPane.setBottom(new ScrollPane(ta2));
		
		VBox v = new VBox(10);
		v.setAlignment(Pos.CENTER);
		v.getChildren().addAll(clients,mainPane);

		// Create a scene and place it in the stage
		Scene scene = new Scene(v, 450,500);
		primaryStage.setTitle("Client"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

		new Thread(() -> {
			try {
				// Create a server socket
				ServerSocket serverSocket = new ServerSocket(9745);
				ta.appendText("Server started at " + new Date() + '\n');
				primaryStage.setOnCloseRequest(e->{
					try {
						serverSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
				while (true) {
					// Listen for a new connection request
					Socket socket = serverSocket.accept();

					// Increment clientNo
					clientNo++;

					Platform.runLater(() -> {
						// Display the client number
						ta.appendText("Starting thread for client " + clientNo + " at " + new Date() + '\n');

						// Find the client's host name, and IP address
						InetAddress inetAddress = socket.getInetAddress();
						clients.setText("Number of Clients: " + clientNo);
						ta2.appendText("Client " + clientNo + "'s host name is " + inetAddress.getHostName() + "\n");
						ta2.appendText("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + "\n");
					});
					new Thread(new HandleAClient(socket)).start();
				}
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}).start();
	}

	public static void main(String[] args) {
		launch(args);
	}
	class HandleAClient implements Runnable {
		private Socket socket; // A connected socket
		
		/** Construct a thread */
		public HandleAClient(Socket socket) {
			this.socket = socket;
		}
		
		/** Run a thread */
		public void run() {
			try {
				// Create data input and output streams
				ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream());
				
				// Read from input
				
				// Continuously serve the client
				while (true) {
					// Receive radius from the client
					Object object = inputFromClient.readObject();
					if(object.toString().equals("Close"))
					{
						clientNo--;
						Platform.runLater(() -> {
							clients.setText("Number of Online Clients: " + clientNo);					
						});	
					}
					else {
						Platform.runLater(() -> {
							ta.appendText(object.toString());
						});						
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

