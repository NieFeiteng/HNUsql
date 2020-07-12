package CONNECT;

//服务器程序
import java.net.*;
import java.io.*;

public class Server{

    ServerSocket server;
    Socket socket;
    InputStreamReader sysIn ;
    BufferedReader sysBuf ;//从服务器命令行输入数据

    InputStreamReader socIn ;
    BufferedReader socBuf;//从client获取数据

    PrintWriter socOut;


    public void initial_server()throws Exception{
        try{
            //建立套接字
            server = new ServerSocket(4700);
            //监听
            socket = server.accept();
            //建立连接
            sysIn = new InputStreamReader(System.in);
            sysBuf = new BufferedReader(sysIn);

            socIn = new InputStreamReader(socket.getInputStream());
            socBuf = new BufferedReader(socIn);

            socOut = new PrintWriter(socket.getOutputStream());
        }catch(Exception e){
            System.out.println("Error" + e);
        }

    }

    public BufferedReader get_sysbuf(){
        return sysBuf;
    }

    public Server() throws Exception {
        try{
            initial_server();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public  String get_from_client()throws Exception{
        String readL="";
        try {
            readL = socBuf.readLine();
            System.out.println("Client:" + readL);
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return readL;
    }

    public  String get_from_server()throws Exception{
        String readL="";
        try {
            readL = sysBuf.readLine();
            System.out.println(readL);
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return readL;
    }

    public void out_to_client(String str)throws Exception{
        try {
            socOut.println(str);
            socOut.flush();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public void end_server() throws IOException {
        try{
            socOut.close();
            socIn.close();
            server.close();
            socket.close();
        }catch(Exception e){
            System.out.println("Error" + e);
        }

    }

}