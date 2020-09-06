package CLIENT;


import java.io.IOException;

public class Connect {
    static Client client;

    static {
        try {
            client = new Client();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void out_to_server(String str) throws Exception {
        try {
            client.out_to_server(str);
        }catch(Exception e){
            System.out.println("Error" + e);
        }
    }

    public static String read_line_from_server() throws Exception {
        String str = "";
        try {
            str = client.get_from_server();

        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return str;
    }


    public static String read_line_from_client() throws Exception {//从服务器命令行读取数据
        String str = "";
        try {
            str = client.get_from_client();
        }catch(Exception e){
            System.out.println("Error" + e);
        }
        return str;
    }

    public static  void close_client() throws IOException {
        try{
            client.end_client();
            System.out.println("Bye!");
        }
        catch(Exception e){
            System.out.println("Error" + e);
        }
    }
}
