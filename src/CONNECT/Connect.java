package CONNECT;

import java.io.BufferedReader;
import java.io.IOException;

public class Connect {
    static Server ser;

    static {
        try {
            ser = new Server();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String userName;
    private static String passWord;


    public static void out_to_client(String str) throws Exception {
        try {
            ser.out_to_client(str);
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }
    public static String read_line_from_client() throws Exception {
        String str = "";
        try {
            str = ser.get_from_client();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return str;
    }


    public static String read_line_from_server() throws Exception {//从服务器命令行读取数据
        String str = "";
        try {
            str = ser.get_from_server();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return str;
    }

    public static BufferedReader get_sysbuf(){
        return ser.get_sysbuf();
    }

    public static  void close_server() throws IOException {
        try{
            ser.end_server();
        }
        catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public static String  get_username(){
        return userName;
    }

    public static String get_password(){
        return passWord;
    }

    public static void change_username(String user){
        userName = user;
    }

    public static void change_password(String password){
        passWord = password;
        
    }

}
