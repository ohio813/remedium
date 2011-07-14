package system.mq;



/*
 * We will be testing the basic funcionality of HSQL, adding a new
 * datatase located on a given file, adding persistent data to this database
 * and then retrieving it from another method.
 *
 * You can change the db_file variable to pick another location, by default
 * we are using "storage/hsql_testDB"
 *
 * If any exception occurs, this test case will fail.
 *
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// HSQL imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Nuno Brito
 */
public class hsql_raw_test {

    static Connection conn;   //our connnection to the db - persist for life of program
    static String db_file = "storage/hsql_testDB";


    public hsql_raw_test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection("jdbc:hsqldb:"
                                           + db_file,    // filenames
                                           "SA",         // username
                                           "");          // password

    }

    @AfterClass
    public static void tearDownClass() throws Exception {

     Statement st = conn.createStatement();
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }

//use for SQL command SELECT
    public synchronized void query(String expression) throws SQLException {

        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        rs = st.executeQuery(expression);    // run the query

        // do something with the result set.
        dump(rs);
        st.close();    // NOTE!! if you close a statement the associated ResultSet is

    }

//use for SQL commands CREATE, DROP, INSERT and UPDATE
    public synchronized void update(String expression) throws SQLException {

        Statement st = null;

        st = conn.createStatement();    // statements

        int i = st.executeUpdate(expression);    // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }

        st.close();
    }    // void update()

    public static void dump(ResultSet rs) throws SQLException {

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column is indexed

                // with 1 not 0
                System.out.print(o.toString() + " ");
            }

            System.out.println(" ");
        }
    }              


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
   


    @Test
    public void testHSQL() throws SQLException{

            // drop a table
            this.update(
                "DROP TABLE IF EXISTS sample_table");
            // create a table
            this.update(
                "CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)");
            // add dummy data
            this.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)");
            this.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Toyota', 200)");
            this.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Honda', 300)");
            this.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('GM', 400)");

            // do a query
            this.query("SELECT * FROM sample_table WHERE num_col < 250");
    }



}