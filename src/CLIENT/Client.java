package CLIENT;

//客户端
import java.net.*;
import java.io.*;

public class Client{


    Socket socket;
    InputStreamReader sysIn ;
    BufferedReader sysBuf ;//从本地命令行输入数据

    InputStreamReader socIn ;
    BufferedReader socBuf;//从服务器获取数据

    PrintWriter socOut;


    public void initial_client()throws Exception{
        try{
            //建立套接字

            //监听
            socket =  new Socket("127.0.0.1",4700);
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

    public Client() throws Exception {
        try{
            initial_client();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public  String get_from_server()throws Exception{
        String readL="";
        try {
            readL = socBuf.readLine();
            if(readL.contains("HNUsql-->"))
                System.out.print(readL);
            else if(!readL.isEmpty())
                System.out.println(readL);

        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return readL;
    }

    public  String get_from_client()throws Exception{
        String readL="";
        try {
            readL = sysBuf.readLine();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return readL;
    }

    public void out_to_server(String str)throws Exception{
        try {
            socOut.println(str);
            socOut.flush();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public void end_client() throws IOException {
        try{
            socOut.close();
            socIn.close();
            socket.close();
        }catch(Exception e){
            System.out.println("Error" + e);
        }

    }


}