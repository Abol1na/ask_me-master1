import javax.sound.midi.Soundbank;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbFunctions {
    public Connection connection_to_db(String dbname, String user, String pass) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432" + dbname, user, pass);
            if (conn != null) {
                System.out.println("connection established");
            } else {
                System.out.println("connection failed");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return conn;
    }
    public void createTable(Connection conn,String table_name){
        Statement statement;
        try{
String query="create table"+table_name+"(empid SERIAL,name varchar(200),addres varchar(200),primary key(empid));";
        statement=conn.createStatement();
        statement.executeUpdate(query);
            System.out.println("Table Created ");
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
