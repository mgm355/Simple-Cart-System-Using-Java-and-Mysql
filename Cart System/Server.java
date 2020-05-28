package Server;
import java.net.*;
import java.sql.*;
import java.io.*;


public class Server{
   private ServerSocket serverSocket;
   int port = 7100;
   //Added below properties to my statement since I'm having timezone error in my connections due to it
   //&useLegacyDatetimeCode=false&serverTimezone=UTC
   static final String dbURL = "jdbc:mysql://localhost:3306/eece350?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=UTC" + "user=root & password=root";//connect to db schema db
   
   public Server() throws ClassNotFoundException {
   Class.forName("com.mysql.jdbc.Driver");
   }
   public void Start_Connections() {
 try {
  //create a server socket on a port 
  serverSocket = new ServerSocket(port);

 } catch (IOException e) {
  System.err.println("failed to create a socket");
  e.printStackTrace();
  System.exit(0);
 }
 // Entering the infinite loop
 while (true) { // we are waiting for the connection here
  try {
   // wait for a TCP handshake initialization
   Socket newConnection = serverSocket.accept();
   System.out.println("connection acccepted");


   ServerStart st = new ServerStart(newConnection);
   // Then, start the thread, and go back to waiting for another TCP connection
   // This also is not blocking
   new Thread(st).start();
  } catch (IOException ioe) {
   System.err.println("server accept failed");
  }
 }
}

   public static void main(String args[]) {

 Server server = null;
 try {
  //check whether we are connected to the drive or not 
  server = new Server();

 } catch (ClassNotFoundException e) {
  e.printStackTrace();
  System.exit(1);
 }
 server.Start_Connections();
}
   
   class ServerStart extends Thread {
 private Socket socket;
 private DataInputStream in;
 private DataOutputStream out;
 String respond;
 
 public ServerStart(Socket socket) {
  this.socket=socket;
 }
 ///////////////////////////////////establishing the connection////////////////////////////////////
 public void run() {
  try {
   in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
   out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
  }catch(IOException e){
   System.out.println(e.getMessage());
  }
  byte[] ba = new byte[20000];
  boolean TCP_connection= true;
  
  while(TCP_connection) {
   ba = new byte[20000];
   String Client_message = "";
   try {
   //read the messages from the client 
   in.read(ba, 0, 200);
   Client_message = new String(ba);
   //if the client sends 
   if (Client_message.equals("Quit")) {
    TCP_connection = false;
   } else {
    String split[] = Client_message.split("~");
    System.out.println(Client_message);
    String functionType = split[0];
    String response = "";
    System.out.println(split[0]);
    //see what dialog did the client use in the GUI
    if (functionType.equals("loginstore")) {
     response = LogInStore(Client_message);
    } else if (functionType.equals("register")) {
     response = SignUp(Client_message);
    } else if (functionType.equals("BuyItems")) {
     response = BuyItems(Client_message);
     // response = "success";
    } else if (functionType.equals("ShowItems")) {
     response = ShowItems(split[1]);
     // response = "success";
    } else if (functionType.equals("invoice")) {
     response = Invoice(Client_message);
    } else if (functionType.equals("checkout")) {
     response = CheckOut();
    }
    PrintWriter dataout = new PrintWriter(socket.getOutputStream(), true);
    response = response +"Finish!";
    System.out.println(response);
    dataout.println(response);
    out.write(response.getBytes(),0,response.length());//write a response to the client after the operation
    out.write("$\n".getBytes(), 0, 1);//to  go to a new line 
   }
   }catch (IOException e) {
    System.out.println("in exception state");
    System.out.println(e);
    TCP_connection = false;
   }
  }
  try {
   System.out.println("closing socket");
   in.close();
   out.close();
   //closing the buffers and then close the socket
   socket.close();
  } catch (IOException e) {
  
  }
 }
 private String CheckOut() {
  // TODO Auto-generated method stub
  return null;
 }
 private String Invoice(String client_message) {
  // TODO Auto-generated method stub
  return null;
 }
 private String BuyItems(String client_message) {
  String inter[]=client_message.split("~");
  System.out.println("Buy Items");
  return "success";
 }
 private String ShowItems(String par) {
  System.out.println("Show Items");
  Connection conn = null;
    try {
    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eece350?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=UTC","root","root");
    System.out.println("connected to database");
    Statement stmt = conn.createStatement();
    //set the query to get the availability and name of each item
    String query="SELECT quantity, name, price FROM eece350."+par;
   //excute the query
   ResultSet rs = stmt.executeQuery(query);
   System.out.println("excution query!");
   
   //this will loop over items and set the client message as example: "1~0" if item1 has items available and item2 is empty
   String available_items="";
   String items_list = "";
   String items_price = "";
   System.out.println(query);
   //Note check for  order of items i=here
   while (rs.next()) {
     System.out.println(rs.getString("name"));
     System.out.println(rs.getString("quantity"));
     items_list+=rs.getString("name")+"~";
     items_price+=rs.getFloat("price")+"~";
    if(rs.getInt("quantity")>0)
    {
        available_items+=rs.getInt("quantity")+"~";
    }else
    {
        available_items+="0~";
    }
   }
   String reply_items = available_items +"~~~"+ items_list+"~~~"+items_price;
   System.out.println(reply_items); //return in the format of 1~1~Finish! ==> meaning item1 has flag 1 and item2 has flag2
   return reply_items;
   
  }catch(SQLException e) {
   System.out.println(e.getMessage());
  }finally {
   if (conn != null) {
    try {
     conn.close();
    } catch (SQLException e) {
    }
   }
  }
  return "Couldn't Retrieve Items from Database";
 }
///////////////////////////////////sign up////////////////////////////////////////////////////
 private String SignUp(String client_message) {
  System.out.println("register state");
  Connection conn = null;
                        String result="";
                        String inter[]=client_message.split("~");
                        String username=inter[1];
                        String firstname=inter[2];
                        String lastname= inter[3];
                        String birth_date=inter[4];
                        String gender = inter[5];
                        String address =inter[6];
                        String password=inter[7];
  try {
   conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eece350?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=UTC","root","root");
   System.out.println("connected to database");
   Statement stmt = conn.createStatement();
   //set the query to get the row that correspond to the logged in user
                                String second_query="SELECT userName " + "FROM eece350.users " + "WHERE username = " + "'" + username + "'";
                                ResultSet check_username= stmt.executeQuery(second_query);
                                if (check_username.next()){//there exist a row 
    return "fail";
   }      
                                String query = "INSERT INTO eece350.users VALUES ( " + "'" + username + "'"+","+
     "'"+firstname+"'"+","+"'"+lastname+"'"+","+"'"+birth_date+"'"+","+
                                                "'"+gender+"'"+","+"'"+address+"'"+","+"'"+password+"'"+")";
                                stmt.executeUpdate(query);
                                System.out.println("new register!");
   //excute the query
   ResultSet rs = stmt.executeQuery(query);
   String databaseUserName="";
   String databasePassword="";
   System.out.println("excution query!");
   while (rs.next()) {
    databaseUserName= rs.getString("userName");
    System.out.println(databaseUserName);
    databasePassword=rs.getString("password");
    System.out.println(databasePassword);
   }
  }catch(SQLException e) {
   System.out.println(e.getMessage());
  }finally {
   if (conn != null) {
    try {
     conn.close();
    } catch (SQLException e) {
    }
   }
  }
  return "success";
 }
//////////////////////////////////////////////////////log in ////////////////////////////////////////////////
 private String LogInStore(String client_message) {
  System.out.println("login state");
  System.out.println(client_message);
  Connection conn = null;
  String result=" " ;
  String inter[]=client_message.split("~");
  String userName=inter[1];
  String password=inter[2];
  System.out.println(inter[1]);
  System.out.println(inter[2]);
  try {
   conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/eece350?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=UTC","root","root");
   System.out.println("connected to database");
   Statement stmt = conn.createStatement();
   //set the query to get the row that correspond to the logged in user
   String query = "SELECT userName,password " + "FROM eece350.users " + "WHERE userName= " + "'" + userName + "'"+"AND password="+
     "'"+password+"'";
   //excute the query
   ResultSet rs = stmt.executeQuery(query);
   String databaseUserName="";
   String databasePassword="";
   System.out.println("excution query!");
   while (rs.next()) {
    System.out.println("DB Username:");
    databaseUserName= rs.getString("userName");
    System.out.println(databaseUserName);
    System.out.println("DB pass:");
    databasePassword=rs.getString("password");
    System.out.println(databasePassword);
   }
   if(userName.equals(databaseUserName) && password.equals(databasePassword)) {
    System.out.println("welcome "+inter[1]+" "+inter[2]);
    result="success";
   }else {
    return "failed!";
   }
  }catch(SQLException e) {
   System.out.println(e.getMessage());
  }
  return result;
 }
 
}
}
