import CONNECT.Connect;

public class QException extends Exception {

    public int status; //status code
    public int type; //exception type: 0 for 'syntax error' and 1 for 'rn time error'
    public String msg; //exception message
    public static final String[] ex = {"Syntax error ", "Run time error "};

    QException(int type, int status, String msg)  {
        this.type = (type >= 0 && type <= ex.length) ? type : 0;
        this.status = status;
        this.msg = msg;
        Interpreter.logger.info("系统异常， " + msg);
        //String str = String.valueOf(type) + String.valueOf(status)+msg;
     //   Connect.out_to_client(str);
    }

    @Override
    public String getMessage() {
        return ex[type] + status + ": " + msg;
    }

//    public void printMsg() {
//        System.out.println(ex[type] + status + ": " + msg);
//    }
    public void printMsg() throws Exception {
        try{
            String str = ex[type] + status + ": " + msg;
            System.out.println(str);
            Connect.out_to_client(str);
        }catch(Exception e){
            System.out.println("Error" + e);
        }

    }

}
