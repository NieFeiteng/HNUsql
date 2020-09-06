package CLIENT;


public class  TCPclient {
    public static void main(String[] args) throws Exception {

        String userName = "";
        //登录
        while(true){
            System.out.print("Enter your username:");
            userName = Connect.read_line_from_client();
            System.out.print("Enter your passWord:");
            String paasWord = Connect.read_line_from_client();

            Connect.out_to_server(userName);
            Connect.out_to_server(paasWord);

            String result  = Connect.read_line_from_server();//直接返回字符串

            if(!result.contains("Wrong user name or password") ) break;
        }

        String readLine;
        readLine = Connect.read_line_from_server(); //读取welcome

        while (true){

            readLine = Connect.read_line_from_server();//返回数据包含“HNUsql-->”表示应该继续从client输入
            while(!readLine.contains("HNUsql-->")){
                readLine = Connect.read_line_from_server();
            }

            readLine = Connect.read_line_from_client();//读到； 结束
            Connect.out_to_server(readLine);

            if(readLine.contains("quit;"))break;

            while(!readLine.contains(";")){
                // System.out.print("HNUsql-->");
                readLine = Connect.read_line_from_server();
                readLine = Connect.read_line_from_client();
                Connect.out_to_server(readLine);
            }
        }
        Connect.close_client();
    }
}
