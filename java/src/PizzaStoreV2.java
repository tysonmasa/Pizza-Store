/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
//Added
import java.sql.Timestamp;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   //Edit made by Hugo Centeno so that certain methods work (waitin for aproval)
   public static String authorisedUser = null;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");
         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            //String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
               //Added by Hugo Centeno
               String permissionsQuery = "SELECT role FROM Users WHERE login = '"+authorisedUser+"'";
               List<List<String>> outputpermissionQuery = esql.executeQueryAndReturnResult(permissionsQuery);
               String permission = outputpermissionQuery.get(0).get(0);
              boolean usermenu = true;
              while(usermenu) {
                  if(permission.trim().equals("customer")){
                     System.out.println("MAIN MENU");
                     System.out.println("---------");   
                     System.out.println("1. View Profile");
                     System.out.println("2. Update Profile");
                     System.out.println("3. View Menu");
                     System.out.println("4. Place Order"); //make sure user specifies which store
                     System.out.println("5. View Full Order ID History");
                     System.out.println("6. View Past 5 Order IDs");
                     System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                     System.out.println("8. View Stores"); 
                     System.out.println(".........................");
                     System.out.println("20. Log out");
                  }else if(permission.trim().equals("driver")){
                     System.out.println("MAIN MENU");
                     System.out.println("---------");   
                     System.out.println("1. View Profile");
                     System.out.println("2. Update Profile");
                     System.out.println("3. View Menu");
                     System.out.println("4. Place Order"); //make sure user specifies which store
                     System.out.println("5. View Full Order ID History");
                     System.out.println("6. View Past 5 Order IDs");
                     System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                     System.out.println("8. View Stores"); 
                     //**the following functionalities should only be able to be used by drivers & managers**
                     System.out.println("9. Update Order Status");   
                      System.out.println(".........................");
                     System.out.println("20. Log out");                                      
                  }else{
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. View Profile");
                  System.out.println("2. Update Profile");
                  System.out.println("3. View Menu");
                  System.out.println("4. Place Order"); //make sure user specifies which store
                  System.out.println("5. View Full Order ID History");
                  System.out.println("6. View Past 5 Order IDs");
                  System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                  System.out.println("8. View Stores"); 

                  //**the following functionalities should only be able to be used by drivers & managers**
                  System.out.println("9. Update Order Status");

                  //**the following functionalities should ony be able to be used by managers**
                  System.out.println("10. Update Menu");
                  System.out.println("11. Update User");

                  System.out.println(".........................");
                  System.out.println("20. Log out");                    
                  }
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   /* case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break; */
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      try {
		   // User input for login
         //Check that the login is valid 
         boolean invalidLogin = true;
         String login = "";
         while(invalidLogin){
            System.out.print("\tEnter login: ");
            login = in.readLine();
            if(login.length() > 50 || login.length() == 0){
               System.out.print("\tInvalid login!\n");
            }else{
               invalidLogin = false;
            }
         }
        
        // User input for password
         boolean invalidPassword = true;
         String password = "";
         while(invalidPassword){
            System.out.println("---------");
            System.out.print("\tEnter password: ");
            password = in.readLine();
            if(password.length() > 30 || password.length() == 0){
               System.out.print("\tInvalid password!\n");
            }else{
               invalidPassword = false;
            }
         }

        // User input for phone number
        boolean invalidPhoneNumber = true;
        String phoneNumber = "";
         while(invalidPhoneNumber){
            System.out.print("\tEnter phone number: ");
            phoneNumber = in.readLine();
            if(phoneNumber.length() > 20 || phoneNumber.length() == 0 || !phoneNumber.matches("\\d+")){
               System.out.print("\tInvalid phone number!\n");
            }else{
               invalidPhoneNumber = false;
            }
         }
         
        //Construct the query
        String query = "INSERT INTO Users (login, password, phoneNum, role) VALUES ('" 
                        + login + "', '" 
                        + password + "', '" 
                        + phoneNumber + "', 'manager')";
		   
		   esql.executeQuery(query);
	   }catch(Exception e){
	         System.err.println (e.getMessage());
	   }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try{
            boolean invalidCombination = true;
            while(invalidCombination){
               //Ask for login
            System.out.println("---------");
            System.out.print("\tEnter login: ");
            String login = in.readLine();
            //Ask for password
            System.out.print("\tEnter password: ");
            String password = in.readLine();
            String query = "SELECT password, login FROM Users WHERE login = '" + login + "' AND password = '" + password + "'";
            //Check if the user exists and the password is matching
            if(esql.executeQuery(query) == 0){
               System.out.println("Invalid combination for login and password!");
            }else{
               invalidCombination = false;
               System.out.println("\nWelcome again " + login);
               return login;
               }
            }
            
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
      return null;
   }
   //end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql) {
      try{
         String query = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
         List<List<String>> output = esql.executeQueryAndReturnResult(query);
         System.out.println(output.get(0).get(0));
         if(output.get(0).get(0).trim().equals("manager")){
         query = "SELECT password, favoriteItems, phoneNum, role FROM Users WHERE login = '" + authorisedUser + "'";
         output = esql.executeQueryAndReturnResult(query);
            // Iterate through the result set
            for (List<String> row : output) {
               // Extract the values for password, favorite items, and phone number
               String password = row.get(0);  // First column: password
               String favoriteItems = row.get(1);  // Second column: favorite items
               String phoneNum = row.get(2);  // Third column: phone number
               String role = row.get(3); 

               // Print each piece of information on a new line
               System.out.println("---------");
               System.out.println("INFORMATION");
               System.out.println("User: " + authorisedUser);
               System.out.println("Password: " + password);
               System.out.println("Favorite Item: " + favoriteItems);
               System.out.println("Phone Number: " + phoneNum);
               //Managers can view all the users information
               System.out.println("---------");
               System.out.println("You have more privileges as you are: " + role);
               boolean invalidInput = true;
               while(invalidInput){
                  System.out.println("Select the user whose information you want to see");
                  String user = in.readLine();
                  query = "SELECT password, favoriteItems, phoneNum, role, login FROM Users WHERE login = '" + user + "'";
                  if(esql.executeQuery(query) == 0){
                     System.out.println("---------");
                     System.out.println("Non existent user");
                  }else{
                  output = esql.executeQueryAndReturnResult(query);
               // Iterate through the result set
                  for (List<String> row1 : output) {
                  // Extract the values for password, favorite items, and phone number
                     password = row1.get(0);  // First column: password
                     favoriteItems = row1.get(1);  // Second column: favorite items
                     phoneNum = row1.get(2);  // Third column: phone number
                     role = row1.get(3);
                     String login = row1.get(4); 

                     // Print each piece of information on a new line
                     System.out.println("---------");
                     System.out.println("INFORMATION");
                     System.out.println("User: " + login);
                     System.out.println("Password: " + password);
                     System.out.println("Favorite Item: " + favoriteItems);
                     System.out.println("Phone Number: " + phoneNum);
                     System.out.println("Role: " + role);
                     invalidInput = false;
                     }                     
                  }
               }  
                  /* output = esql.executeQueryAndReturnResult(query);
               // Iterate through the result set
                  for (List<String> row1 : output) {
                  // Extract the values for password, favorite items, and phone number
                     password = row1.get(0);  // First column: password
                     favoriteItems = row1.get(1);  // Second column: favorite items
                     phoneNum = row1.get(2);  // Third column: phone number
                     role = row1.get(3);
                     String login = row1.get(4); 

                     // Print each piece of information on a new line
                     System.out.println("---------");
                     System.out.println("INFORMATION");
                     System.out.println("User: " + login);
                     System.out.println("Password: " + password);
                     System.out.println("Favorite Item: " + favoriteItems);
                     System.out.println("Phone Number: " + phoneNum);
                     System.out.println("Role: " + role);
                  } */
               

               /* System.out.println("Select the user whose information you want to see");
               String user = in.readLine();
               query = "SELECT password, favoriteItems, phoneNum, role, login FROM Users WHERE login = '" + user + "'";
               output = esql.executeQueryAndReturnResult(query);
            // Iterate through the result set
            for (List<String> row1 : output) {
               // Extract the values for password, favorite items, and phone number
               password = row1.get(0);  // First column: password
               favoriteItems = row1.get(1);  // Second column: favorite items
               phoneNum = row1.get(2);  // Third column: phone number
               role = row1.get(3);
               String login = row1.get(4); 

               // Print each piece of information on a new line
               System.out.println("---------");
               System.out.println("INFORMATION");
               System.out.println("User: " + login);
               System.out.println("Password: " + password);
               System.out.println("Favorite Item: " + favoriteItems);
               System.out.println("Phone Number: " + phoneNum);
               System.out.println("Role: " + role);
               } */
               //System.out.println();  // Adds a blank line for separation between records
               }
                       
         }else{
         query = "SELECT password, favoriteItems, phoneNum, role FROM Users WHERE login = '" + authorisedUser + "'";
         output = esql.executeQueryAndReturnResult(query);
            // Iterate through the result set
            for (List<String> row : output) {
               // Extract the values for password, favorite items, and phone number
               String password = row.get(0);  // First column: password
               String favoriteItems = row.get(1);  // Second column: favorite items
               String phoneNum = row.get(2);  // Third column: phone number
               String role = row.get(3); 

               // Print each piece of information on a new line
               System.out.println("---------");
               System.out.println("INFORMATION");
               System.out.println("User: " + authorisedUser);
               System.out.println("Password: " + password);
               System.out.println("Favorite Item: " + favoriteItems);
               System.out.println("Phone Number: " + phoneNum);
               if(!role.trim().equals("customer")){
                  System.out.println("Role: " + role);
               }
               System.out.println();  // Adds a blank line for separation between records
            }           
         }
         }
         



/*          String query = "SELECT password, favoriteItems, phoneNum, role FROM Users WHERE login = '" + authorisedUser + "'";
         List<List<String>> output = esql.executeQueryAndReturnResult(query);
            // Iterate through the result set
            for (List<String> row : output) {
               // Extract the values for password, favorite items, and phone number
               String password = row.get(0);  // First column: password
               String favoriteItems = row.get(1);  // Second column: favorite items
               String phoneNum = row.get(2);  // Third column: phone number
               String role = row.get(3); 

               // Print each piece of information on a new line
               System.out.println("---------");
               System.out.println("INFORMATION");
               System.out.println("User: " + authorisedUser);
               System.out.println("Password: " + password);
               System.out.println("Favorite Item: " + favoriteItems);
               System.out.println("Phone Number: " + phoneNum);
               if(!role.trim().equals("customer")){
                  System.out.println("Role: " + role);
               }
               System.out.println();  // Adds a blank line for separation between records
            } */
      catch(Exception e){
         System.err.println(e.getMessage());
      }   
      
   }
   public static void updateProfile(PizzaStore esql) {
      try{
            //Get the role. Depending on it less/more options
            String query = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
            List<List<String>> output = esql.executeQueryAndReturnResult(query);
            String role = output.get(0).get(0);
/*             System.out.println("---------");
            System.out.println("What would you like to update?"); */
            boolean invalidInput = true;
            while(invalidInput){
               if(!role.trim().equals("manager")){
                  System.out.println("---------");
                  System.out.println("What would you like to update?");
                  System.out.println("1. Add new favorite item");
                  System.out.println("2. Change phone number");
                  System.out.println("3. Change password");
                  System.out.println("4. Go back");
                  int input = readChoice();
                  if(input == 1){
                     System.out.println("---------");
                     System.out.println("Insert new item name");
                     String choice2 = in.readLine();
                     String query2 = "UPDATE Users SET favoriteItems = '"+choice2+"' WHERE login = '" + authorisedUser + "'";
                     esql.executeUpdate(query2);
                     System.out.println("Your favorite item has correctly been updated to " + choice2);
                  }else if(input == 2){
                     System.out.println("---------");
                     System.out.println("Insert new phone number");
                     String choice2 = in.readLine();
                     String query2 = "UPDATE Users SET phoneNum = '"+choice2+"' WHERE login = '" + authorisedUser + "'";
                     esql.executeUpdate(query2);
                     System.out.println("Your phone number has correctly been updated to " + choice2);
                  }else if(input == 3){
                     System.out.println("---------");
                     System.out.println("Insert new password");
                     String choice2 = in.readLine();
                     String query2 = "UPDATE Users SET password = '"+choice2+"' WHERE login = '" + authorisedUser + "'";
                     esql.executeUpdate(query2);
                     System.out.println("Your password has correctly been updated to " + choice2);
                  }else if(input == 4){
                     invalidInput = false;
                  }else{

                  }
               }else{ //Managers can update others information.
                  System.out.println("---------");
                  System.out.println("Manager mode");
                  invalidInput = true;
                  while(invalidInput){
                     System.out.println("Insert the user whose information you want to change:");
                     String user = in.readLine();
                     String query2 = "SELECT * FROM Users WHERE login = '"+user+"'";
                     if(esql.executeQuery(query2) == 0){
                        System.out.println("---------");
                        System.out.println("Non existent user");
                     }else{
                        boolean invalidInput2 = true;
                        while(invalidInput2){
                           System.out.println("---------");
                           System.out.println("1. Edit login");
                           System.out.println("2. Edit role");
                           System.out.println("3. Go back");
                           int input = readChoice();
                           if(input == 1){
                              System.out.println("Insert new login");
                              String newLogin = in.readLine();
                              query = "UPDATE Users SET login = '"+newLogin+"' WHERE login = '"+user+"'";
                              esql.executeUpdate(query);
                              invalidInput2 = false;
                              invalidInput = false;
                           }else if(input == 2){
                              System.out.println("Insert new role");
                              String newRole = in.readLine();
                              query = "UPDATE Users SET role = '"+newRole+"' WHERE login = '"+user+"'";
                              esql.executeUpdate(query2);
                              invalidInput2 = false;
                              invalidInput = false;
                           }else if(input == 3){
                              invalidInput2 = false;
                           }else{
                              System.out.println("Invalid choice");
                           }
                        }                       
                  }
               
                  }
               }
            }

      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewMenu(PizzaStore esql) {
      try{
      boolean invalidInput = true;
      while(invalidInput){
         System.out.println("---------");
         System.out.println("1. Search on the Menu");
         System.out.println("2. Filter search based on a price");
         System.out.println("3. Sort menu based on the price");
         System.out.println("4. Go back");
         int input = readChoice();
         if(input == 1){
            String query = "SELECT DISTINCT typeOfItem FROM Items";
            List<List<String>> items = null;
            
               items = esql.executeQueryAndReturnResult(query);
               // Iterate through the result
               System.out.println("---------");
               System.out.println("Choose among:");
               for (List<String> row : items) {
                  for (String item : row) {
                        System.out.println(item);  // Print the item
                  }
               }
             
            
            System.out.println("---------");
            System.out.println("Please enter your choice");
            String choice2 = in.readLine();
            String query2 = "SELECT itemName, price FROM Items WHERE typeOfItem = ' "+choice2+"'";
            
            //Execute query
            List<List<String>> selecteditems = esql.executeQueryAndReturnResult(query2);
            System.out.println("---------");
            System.out.println("Items available:");
            for (List<String> row : selecteditems) {
               String itemName = row.get(0); 
               String price = row.get(1);
               System.out.println(itemName + " - $" + price);
            }
         //invalidInput = false;
         }else if(input == 2){
            System.out.println("---------");
            System.out.println("Input your limit price");
            String choice2 = in.readLine();
            String query2 = "SELECT itemName, price FROM Items WHERE price <= " + choice2;
            //Execute query
            List<List<String>> selecteditems = esql.executeQueryAndReturnResult(query2);
            System.out.println("---------");
            System.out.println("Items available within the selected price limit:");
            for (List<String> row : selecteditems) {
               String itemName = row.get(0); 
               String price = row.get(1);
               System.out.println(itemName + " - $" + price);
            }
            //invalidInput = false;
         }else if(input == 3){
            boolean invalidSorting = true;
            while(invalidSorting){
            System.out.println("---------");
            System.out.println("1. Sort from highest to lowest");
            System.out.println("2. Sort from lowest to highest");
            System.out.println("3. Go back");
            input = Integer.parseInt(in.readLine());
               if(input == 1){
                  String query2 = "SELECT itemName, price FROM Items ORDER BY price DESC";
                  List<List<String>> selecteditems = esql.executeQueryAndReturnResult(query2);
                  System.out.println("---------");
                  System.out.println("Items from highest to lowest price:");
                  for (List<String> row : selecteditems) {
                     String itemName = row.get(0); 
                     String price = row.get(1);
                     System.out.println(itemName + " - $" + price);
                  }
               }else if(input == 2){
                  String query2 = "SELECT itemName, price FROM Items ORDER BY price ASC";
                  List<List<String>> selecteditems = esql.executeQueryAndReturnResult(query2);
                  System.out.println("---------");
                  System.out.println("Items from lowest to highest price:");
                  for (List<String> row : selecteditems) {
                     String itemName = row.get(0); 
                     String price = row.get(1);
                     System.out.println(itemName + " - $" + price);
                  }
               }else if(input == 3){
                  invalidSorting = false;
               }else{
                  System.out.println("Invalid input!");
               }
            }
            //invalidInput = false;
         }else if(input == 4){
            //break;
            invalidInput = false;
         }else{
            System.out.println("Invalid option, please choose again!");
         }
      }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }   
   
   }
   public static void placeOrder(PizzaStore esql) {
      try{
         boolean invalidInput = true;
            System.out.println("---------");
            System.out.println("From what store would you like to order? \n The following are available");
            String query2 = "SELECT storeID, address FROM Store";
            List<List<String>> selectedstores = esql.executeQueryAndReturnResult(query2);
            System.out.println("---------");
            System.out.println("Displaying its IDs and addresses");
            int max = 0;
            int storeIDtoInt = 0;
            for (List<String> row : selectedstores) {
               String storeID = row.get(0); 
               String address = row.get(1);
               storeIDtoInt = Integer.parseInt(storeID);
               //To have one last option: exit the current screen
               if (storeIDtoInt > max){
                  max = storeIDtoInt;
               }
               System.out.println(storeID + " - " + address);
            }
            max += 1;
            System.out.println(max + ". Go back");         
         while(invalidInput){
            int storeIDChoice = readChoice();
            //Handling possible inputs. Consider invalid ones as well.
            if(storeIDChoice == max){
               invalidInput = false;
            }else if(storeIDChoice >= 0 && storeIDChoice < max){
               query2 = "SELECT address FROM Store WHERE storeID = '"+storeIDChoice+"'";
               selectedstores = esql.executeQueryAndReturnResult(query2);
               System.out.println("---------");
               System.out.println("You have selected the store located at "+selectedstores.get(0).get(0));
               boolean invalidItem = true;
               //Create array to store order information
               ArrayList<ArrayList<String>> OrderArrayOfArrays = new ArrayList<>();
               int i = 0;
               while(invalidItem){
                  System.out.println("---------");
                  System.out.println("Select what item you want to order");
                  String choice2 = in.readLine();
                  query2 = "SELECT price FROM Items WHERE itemName = '"+choice2+"'";
                  if(esql.executeQuery(query2) == 0){//That item does not exist in the Menu.
                     System.out.println("---------");
                     System.out.println("Sorry, that item is not part of the menu.");
                  }else{
                     System.out.println("---------");
                     System.out.println("How many "+choice2);
                     int quantity = readChoice();
                     //Execute query
                     System.out.println(query2);
                     List<List<String>> selectedprice = esql.executeQueryAndReturnResult(query2);
                     String selectedpriceString = selectedprice.get(0).get(0);
                     double price = Double.parseDouble(selectedpriceString);
                     double finalPrice = price * quantity;
                     //System.out.println(finalPrice);
                     System.out.println("---------");
                     System.out.println("You are ordering " + quantity + " " + choice2 + " for " + finalPrice+" $");

                     //Update the Orders Array with the new info.
                     OrderArrayOfArrays.add(new ArrayList<String>());
                     ArrayList<String> firstRow = OrderArrayOfArrays.get(i);
                     //Casting to String
                     String strfinalPrice = String.valueOf(finalPrice);
                     String strquantity = Integer.toString(quantity);
                     //Adding to the Array of arrays. The structure of it is [[Quantity, Item, Final Price]]
                     firstRow.add(strquantity);
                     firstRow.add(choice2);
                     firstRow.add(strfinalPrice);
                     //System.out.println(OrderArrayOfArrays);
                     System.out.println("---------");
                     System.out.println("Keep on ordering?");
                     System.out.println("1. YES");
                     System.out.println("2. NO");
                     int choice = readChoice();
                     if(choice == 1){
                        i += 1;
                     }else if(choice == 2){
                        System.out.println("---------");
                        System.out.println("Order summary:");
                        finalPrice = 0;
                        for (ArrayList<String> order : OrderArrayOfArrays) {
                        String quantitySummary = order.get(0);  // Get the first element (quantity)
                        String itemSummary = order.get(1); // Get the second element (item)
                        String totalPriceSummary = order.get(2); // Get the third element (total price)
                        //Compute final price
                        double doubletotalPriceSummary = Double.parseDouble(totalPriceSummary);
                        finalPrice += doubletotalPriceSummary;
                        // Print the details
                        System.out.println("Quantity: " + quantitySummary + ", Item: " + itemSummary + ", Price: " + totalPriceSummary);
                        }
                        finalPrice = Math.round(finalPrice * 100.0) / 100.0;
                        System.out.println("Checkout: " + finalPrice + "$");
                        invalidItem = false;
                        //Insert order into FoodOrder table
                        Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());  // Current timestamp

                        query2 = "INSERT INTO FoodOrder (login, storeID, totalPrice, orderTimestamp) VALUES ('" 
                                 + authorisedUser + "', '" 
                                 + storeIDChoice + "', '" 
                                 + finalPrice + "', '" 
                                 + orderTimestamp.toString() + "')";
                        System.out.println(query2);
                        //Get the orderID
                         /* String query = "SELECT orderID FROM FoodOrder WHERE login = '" + authorisedUser + "' " +
                                 "AND storeID = '" + storeIDChoice + "' " +
                                 "AND totalPrice = " + finalPrice + " " + 
                                 "AND orderTimestamp = '" + orderTimestamp.toString() + "'"; */
                        String query = "SELECT * FROM FoodOrder WHERE login = '"+authorisedUser.trim()+"'";
                        List<List<String>> selectedOrderID = esql.executeQueryAndReturnResult(query);
                        //String StringselectedOrderID = selectedOrderID.get(0).get(0);
                        System.out.println("---------");
                        System.out.println(selectedOrderID);


/*                         for (ArrayList<String> order : OrderArrayOfArrays) {
                        String quantitySummary = order.get(0);  // Get the first element (quantity)
                        String itemSummary = order.get(1); // Get the second element (item)
                        String query3 = "INSERT INTO ItemsInOrder (quantity, itemName, orderID) VALUES (" +
                                          quantitySummary + ", '" + itemSummary + "', '" + selectedOrderID + "')";
                        //INSERT INTO ItemsInOrder
                        esql.executeQuery(query3);
                        System.out.println(query3);

                        }

                        
                        //Execute query
                        //INSERT INTO FoodOrder
		                  esql.executeQuery(query2);
                       
                        //Check correct insertion (check ID)

                        query2 = "SELECT * FROM FoodOrder";
                        List<List<String>> allfoodorders = esql.executeQueryAndReturnResult(query2);
                        System.out.println(allfoodorders);  */


                     }else{
                        System.out.println("Invalid choice!");
                     }
                     
                  }
                  
               }
              
               


               invalidInput = false;
            }else{
               System.out.println("Invalid option!");
            }      
         }            
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   

   //ADDED
   public static void viewAllOrders(PizzaStore esql, String login) {//see orderID history
      try{
         //System.out.println("as user " + login);
         String rolequery = 
         "SELECT * " +
         "FROM Users " +
         "WHERE role = 'customer' and login = '" + login + "'";
         int isCustomer = esql.executeQuery(rolequery);

         String query = "";
         //System.out.println("iscustomer = " + isCustomer);
         if (isCustomer != 0) {
            query = 
            "SELECT orderID " +
            "FROM FoodOrder F " +
            "WHERE login = '" + login + "'";
         }
         else {
            query = 
            "SELECT orderID " +
            "FROM FoodOrder F " +
            "WHERE login = ";
            System.out.print("\tlogin name: ");
            String input = "'" + in.readLine() + "'";
            query += input;
         }
         
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount != 0) {
            System.out.println ("Total orders: " + rowCount);
         }
         else {System.out.println("No Orders");}

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   //ADDED
 public static void viewRecentOrders(PizzaStore esql, String login) {//see recent 5 orderids
      try {
         //System.out.println("as user " + login);
         String rolequery = 
         "SELECT * " +
         "FROM Users " +
         "WHERE role = 'customer' and login = '" + login + "'";
         int isCustomer = esql.executeQuery(rolequery);

         //get first 5 order id's
         String query = "";
         //System.out.println("iscustomer = " + isCustomer);
         if (isCustomer != 0) {
            query = 
            "SELECT orderID " +
            "FROM FoodOrder F " +
            "WHERE login = '" + login + "' " +
            "limit 5";
         }
         else {
            query = 
            "SELECT orderID " +
            "FROM FoodOrder F " +
            "WHERE login = ";
            System.out.print("\tlogin name: ");
            String input = "'" + in.readLine() + "' " +
            "limit 5";
            query += input;
         }
         
         //execute query
         int rowCount = esql.executeQueryAndPrintResult(query);

         if (rowCount != 0) {
            System.out.println ("Total orders: " + rowCount);
         }
         else {System.out.println("No Orders");}

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
//ADDED
 public static void viewOrderInfo(PizzaStore esql, String login) {
      try{
         //System.out.println("as user " + login); //check if customer
         String rolequery = 
         "SELECT * " +
         "FROM Users " +
         "WHERE role = 'customer' and login = '" + login + "'";
         int isCustomer = esql.executeQuery(rolequery);

         String query = "";
         //System.out.println("iscustomer = " + isCustomer);
         
         if (isCustomer != 0) {//customer check their own orders 
            System.out.print("\tID of your order: ");
            String orderId = in.readLine();
            String orderQuery = //list order details
            "SELECT orderTimestamp, totalPrice, orderStatus " +
            "FROM FoodOrder F " +
            "WHERE orderID = '" + orderId + "' " + "and login = '" + login + "'";

            int rowCount = esql.executeQueryAndPrintResult(orderQuery);
            //System.out.println(": " + rowCount);
            
            if (rowCount != 0) { //customer is checking their own order
               String itemsQuery = //list items in the order
               "SELECT itemName, quantity " +
               "FROM ItemsInOrder I " +
               "WHERE orderID = '" + orderId + "'";

               int itemsCount = esql.executeQueryAndPrintResult(itemsQuery);
               System.out.println ("total items(s): " + itemsCount);
            }
            else {System.out.println("No matching order");}
            
         }
         else {// manager/driver check any orderid
            System.out.print("\tID of order: ");
            String orderId = in.readLine();
            String orderQuery = //list order details
            "SELECT orderTimestamp, totalPrice, orderStatus " +
            "FROM FoodOrder F " +
            "WHERE orderID = '" + orderId + "'";

            String itemsQuery = //list items in order
            "SELECT itemName, quantity " +
            "FROM ItemsInOrder I " +
            "WHERE orderID = '" + orderId + "'";

            int rowCount = esql.executeQueryAndPrintResult(orderQuery);
            //System.out.println("total row(s): " + rowCount);
            if (rowCount != 0) {
               int itemsCount = esql.executeQueryAndPrintResult(itemsQuery);
               System.out.println("total items(s): " + itemsCount);
            }
            else {System.out.println("No matching order");}            
         }
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void viewStores(PizzaStore esql) {
      try {
         String query = 
         "SELECT address, city, state, storeID, reviewScore, isOpen " +
         "FROM Store";
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Number of stores: " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   public static void updateOrderStatus(PizzaStore esql) {
//TO DO (easy)
   }
   public static void updateMenu(PizzaStore esql) {
      try{
         String query = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
         List<List<String>> output = esql.executeQueryAndReturnResult(query);
         System.out.println(output.get(0).get(0));
         if(output.get(0).get(0).trim().equals("manager")){
            boolean validInput = true;
            while(validInput){
               System.out.println("---------");
               System.out.println("What item do you want to update?");
               String choice2 = in.readLine();
               //Check the item exists
               query = "SELECT * FROM Items WHERE itemName = '"+choice2+"'";
               if(esql.executeQuery(query) == 0){
                  System.out.println("That item does not exist");
               }else{
                System.out.println("---------");
               System.out.println("What do you want to update from "+choice2+"");
               System.out.println("1. Name");
               System.out.println("2. Ingredients");
               System.out.println("3. Category");
               System.out.println("4. Price");
               System.out.println("5. Description");
               System.out.println("6. Add item");
               System.out.println("7. Go back");
               int input = readChoice();
               if(input == 1){
                  System.out.println("Choose new name for "+choice2);
                  String newName = in.readLine();
                  query = "UPDATE Items SET itemName = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  String query2 = "UPDATE ItemsInOrder SET itemName = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  //Aparently the old name is not updated, but the new name added as well.
                  esql.executeQuery(query);
                  esql.executeQuery(query2);
                  validInput = false;
               }else if(input == 2){
                  System.out.println("Choose new ingredients for "+choice2);
                  String newName = in.readLine();
                  query = "UPDATE Items SET ingredients = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  String query2 = "UPDATE ItemsInOrder SET ingredients = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  //Aparently the old name is not updated, but the new name added as well.
                  esql.executeQuery(query);
                  esql.executeQuery(query2);
                  validInput = false;
               }else if(input == 3){
                  System.out.println("Choose new category for "+choice2);
                  String newName = in.readLine();
                  query = "UPDATE Items SET typeOfItem = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  String query2 = "UPDATE ItemsInOrder SET typeOfItem = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  //Aparently the old name is not updated, but the new name added as well.
                  esql.executeQuery(query);
                  esql.executeQuery(query2);
                  validInput = false;
               }else if(input == 4){
                  System.out.println("Choose new price for "+choice2);
                  String newName = in.readLine();
                  query = "UPDATE Items SET price = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  String query2 = "UPDATE ItemsInOrder SET price = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  //Aparently the old name is not updated, but the new name added as well.
                  esql.executeQuery(query);
                  esql.executeQuery(query2);
                  validInput = false;
               }else if(input == 5){
                  System.out.println("Choose new description for "+choice2);
                  String newName = in.readLine();
                  query = "UPDATE Items SET description = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  String query2 = "UPDATE ItemsInOrder SET description = '"+newName+"' WHERE itemName = '"+choice2+"'";
                  //Aparently the old name is not updated, but the new name added as well.
                  esql.executeQuery(query);
                  esql.executeQuery(query2);
                  validInput = false;
               }else if(input == 6){
                  System.out.println("Choose an item name");
                  String newName = in.readLine();
                  System.out.println("Choose the ingredients");
                  String newIngredients = in.readLine();
                  System.out.println("Choose the type of item");
                  String newTypeOfItem = in.readLine();
                  System.out.println("Choose the price");
                  String newPrice = in.readLine();
                  System.out.println("Choose the description");
                  String newDescription = in.readLine();

                  // Constructing the SQL query
                  query = "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) VALUES ('" 
                              + newName + "', '" 
                              + newIngredients + "', '" 
                              + newTypeOfItem + "', '" 
                              + newPrice + "', '" 
                              + newDescription + "')";

               }else if(input == 7){
                  validInput = false;
               }else{
                  System.out.println("Invalid option!");
               }                 
               }
            }
         }else{
            //A non manager tries to enter
            System.out.println("Unauthorised");
         }
      }catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void updateUser(PizzaStore esql) {}


}//end PizzaStore

