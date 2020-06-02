/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package caro;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Data {
    public static Connection con;
    
    public static void Connect() throws SQLException{
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://ADMIN\\HIEN:1433;databaseName=caro;user=hienlpv;password=hiendeptraiso1");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void Close(){
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
