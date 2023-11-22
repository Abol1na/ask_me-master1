import java.sql.Connection;

public class Main {
    public static void main(String[] args){
DbFunctions db=new DbFunctions();

Connection conn =db.connection_to_db("tutdb","postgres","h1H1h2H2");
db.createTable(conn,"employee");
    }

}
