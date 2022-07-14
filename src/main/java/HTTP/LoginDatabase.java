package HTTP;

import java.sql.*;
import java.util.ArrayList;

public class LoginDatabase {
    private java.sql.Connection con;
    private final String name = "Connections";

    public void initialization() {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + name);
            PreparedStatement st = con.prepareStatement(
                    "CREATE TABLE if NOT EXISTS 'Connection' (" +
                            "'login' VARCHAR(20) PRIMARY KEY UNIQUE, " +
                            "'password' VARCHAR(20) NOT NUL)");

            st.executeUpdate();
        } catch (ClassNotFoundException e) {
            System.out.println("Не знайшли драйвер JDBC");
            e.printStackTrace();
            System.exit(0);

        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            e.printStackTrace();
        }
    }

    public void insertUser(String login, String password) throws SQLException {
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO Connection(login, password) " +
                        "VALUES (?, ?)");

        statement.setString(1, login);
        statement.setString(2, password);

        statement.executeUpdate();
        statement.close();
    }

    public Connection getUserByLogin(String login) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM Connection WHERE login = ?");
        ResultSet res = statement.executeQuery();

        Connection connection = new Connection(res.getString("login"),
                res.getString("password"));

        res.close();
        statement.close();
        return connection;
    }
    public String getPassword(String login) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM Connection WHERE login = ?");
        statement.setString(1, login);
        ResultSet res = statement.executeQuery();

        String password = res.getString("password");

        res.close();
        statement.close();
        return password;
    }
}
