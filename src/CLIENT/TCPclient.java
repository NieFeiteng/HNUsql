package CLIENT;


public class TCPclient {
    public static void main(String[] args) throws Exception {

        String readLine;
        readLine = Connect.read_line_from_server(); //读取welcome

        while (true){

            readLine = Connect.read_line_from_server();//返回数据包含“HNUsql-->”代表一条SQL语句执行完毕
            while(!readLine.contains("HNUsql-->")){
                readLine = Connect.read_line_from_server();
            }

            readLine = Connect.read_line_from_client();//读到； 结束
            Connect.out_to_server(readLine);

            if(readLine.contains("quit;"))break;

            while(!readLine.contains(";")){
                System.out.print("HNUsql-->");
                readLine = Connect.read_line_from_client();
                Connect.out_to_server(readLine);
            }
        }
        Connect.close_client();
    }
}
