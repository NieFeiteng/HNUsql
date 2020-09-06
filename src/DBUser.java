import RECORDMANAGER.Condition;
import RECORDMANAGER.TableRow;

import java.util.Vector;

public class DBUser {
    String userName;	//普通用户名
    String userTableName;	//用户表名
    String password;//用户密码

    //构造函数
    public DBUser(String userName, String passwoed){
        this.userName=userName;
        this.userTableName="USER_"+userName;
        this.password=password;
    }

    //查看是否有相应的权限
    public boolean Add_Authority(String tableName) throws Exception {
        String userTableName="USER_"+userName;
        //selec
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        //System.out.println(ret.size());
        //System.out.println(ret.get(0).get_attribute_size());
        String tAuthority;
        if(ret.size()>0){
            tAuthority = ret.get(0).get_attribute_value(0);
        }
        else{
            tAuthority="0000";
        }
        //System.out.println(tAuthority);
        if(tAuthority.substring(0,1).equals("1")){
            System.out.println("普通用户" + userName+"有Add权");
            return true;
        }
        else {
            System.out.println("普通用户" + userName+"无Add权");
            return  false;
        }
    }
    public boolean Delete_Authority(String tableName) throws Exception {
        String userTableName="USER_"+userName;
        //selec
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        String tAuthority;
        if(ret.size()>0){
            tAuthority = ret.get(0).get_attribute_value(0);
        }
        else{
            tAuthority="0000";
        }
        if(tAuthority.substring(1,2).equals("1")){
            System.out.println("普通用户" + userName+"有Delete权");
            return true;
        }
        else {
            System.out.println("普通用户" + userName+"无Delete权");
            return  false;
        }

    }
    public boolean Update_Authority(String tableName) throws Exception {

        String userTableName="USER_"+userName;
        //selec
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        String tAuthority;
        if(ret.size()>0){
            tAuthority = ret.get(0).get_attribute_value(0);
        }
        else{
            tAuthority="0000";
        }
        if(tAuthority.substring(2,3).equals("1")){
            System.out.println("普通用户" + userName+"有Update权");
            return true;
        }
        else {
            System.out.println("普通用户" + userName+"无Update权");
            return  false;
        }
    }
    public boolean Select_Authority(String tableName) throws Exception {

        String userTableName="USER_"+userName;
        //selec
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        String tAuthority;
        if(ret.size()>0){
            tAuthority = ret.get(0).get_attribute_value(0);
        }
        else{
            tAuthority="0000";
        }
        if(tAuthority.substring(3,4).equals("1")){
            System.out.println("普通用户" + userName+"有Select权");
            return true;
        }
        else {
            System.out.println("普通用户" + userName+"无Select权");
            return  false;
        }
    }
}