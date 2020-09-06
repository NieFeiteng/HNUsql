import CATALOGMANAGER.Attribute;
import CATALOGMANAGER.NumType;
import CATALOGMANAGER.Table;
import RECORDMANAGER.Condition;
import RECORDMANAGER.TableRow;

import java.util.Vector;

public class DBManager {
    String adName;	//字段名称
    String adTableName;	//管理员表名
    String password;//管理员密码
    //构造函数
    public DBManager(String adName, String password) throws Exception {
        this.adName=adName;
        this.adTableName="ADMINISTRATOR_"+adName;
        this.password=password;
    }

    //建立新管理员
    public boolean Ad_creatAd() throws Exception {
        //建立表ADMINISTRATOR_adNam
        Vector<Attribute> attrVec = new Vector<>();
        Attribute attribute;
        String attrType="char";
        attribute = new Attribute("TableName", NumType.valueOf(attrType.toUpperCase()), 20, true);
        attrVec.add(attribute);
        Table table = new Table(adTableName,"TableName", attrVec);
        boolean flag= API.create_table(adTableName, table);
        if(!flag){
            System.out.println("管理员表" + adTableName+"创建失败！");
        }
        else{
            System.out.println("管理员表" + adTableName+"创建成功！");

            //加入PASSWORD表
            TableRow tableRow = new TableRow();
            tableRow.add_attribute_value(adName);
            tableRow.add_attribute_value(password);
            tableRow.add_attribute_value("1");
            flag= API.insert_row("PASSWORD", tableRow);
            if(flag){
                System.out.println("管理员" + adName+"创建成功！");
            }
            else{
                System.out.println("管理员" + adName+"创建失败！");
            }
        }

        return  flag;
    }

    //添加管理员所有的表，在管理员create table后调用
    public boolean Ad_AddTable(String tableName) throws Exception {
        //将表加入到ADMINISTRATOR_adNam中
        TableRow tableRow = new TableRow();
        tableRow.add_attribute_value(tableName);
        boolean flag= API.insert_row(adTableName, tableRow);
        if(flag){
            System.out.println("管理员"+ adName+"增加管理的表"+tableName+"成功！");
        }
        else{
            System.out.println("管理员"+ adName+"增加管理的表"+tableName+"失败！");
        }
        return flag;
    }

    //删除管理员所有的表，在管理员drop table后调用
    public boolean Ad_DropTable(String tableName) throws Exception {
        //从ADMINISTRATOR_adNam删除这个表
        int num=0;
        Vector<Condition> conditions= new Vector<>();;
        Condition tconditionl= new Condition("TableName", "=", tableName);
        conditions.add(tconditionl);
        num = API.delete_row(adTableName, conditions);
        System.out.println(num + " row(s) are deleted from "+ tableName);
        if(num==0){
            return false;
        }
        else{
            return true;
        }
    }

    //创建普通用户
    public boolean Ad_CreatUser(String userName, String password) throws Exception {
        String userTableName="USER_"+userName;
        Vector<Attribute> attrVec = new Vector<>();
        Attribute attribute1,attribute2;
        String attrType="char";
        attribute1 = new Attribute("TableName", NumType.valueOf(attrType.toUpperCase()), 20, true);
        attribute2 = new Attribute("Authority", NumType.valueOf(attrType.toUpperCase()), 4, false);
        attrVec.add(attribute1);
        attrVec.add(attribute2);
        Table table = new Table(userTableName,"TableName", attrVec);
        boolean flag= API.create_table(userTableName, table);
        if(flag){
            System.out.println("普通用户表" + userTableName+"创建成功！");
            TableRow tableRow = new TableRow();
            tableRow.add_attribute_value(userName);
            tableRow.add_attribute_value(password);
            tableRow.add_attribute_value("0");
            flag= API.insert_row("PASSWORD", tableRow);
            if(flag){
                System.out.println("普通用户" + userName+"创建成功！");
            }
            else{
                System.out.println("普通用户" + userName+"创建失败！可能有重复用户！");
            }
        }
        else{
            System.out.println("普通用户表" + userTableName+"创建失败！");
        }

        return flag;
    }

    //删除普通用户
    public boolean Ad_DropUser(String userName) throws Exception {
        String userTableName="USER_"+userName;
        boolean flag= API.drop_table(userTableName);
        if(flag){
            System.out.println("普通用户表" + userTableName+"删除成功！");

            int num=0;
            Vector<Condition> conditions= new Vector<>();;
            Condition tconditionl= new Condition("Name", "=", userName);
            conditions.add(tconditionl);
            num = API.delete_row("PASSWORD", conditions);
            System.out.println(num + " row(s) are deleted from PASSWORD");
            if(num==0){
                flag=false;
            }
            else{
                flag=true;
            }
        }
        else {
            System.out.println("普通用户表" + userTableName+"删除失败！");
        }
        return flag;
    }

    //对普通用户授权  +判断manager和user是否拥有此表!
    private boolean Grantinit(String userName,String tableName) throws Exception {
        String userTableName="USER_"+userName;
        boolean flag;
        TableRow tableRow = new TableRow();
        tableRow.add_attribute_value(tableName);
        tableRow.add_attribute_value("0000");
        flag= API.insert_row(userTableName, tableRow);
        return flag;
    }

    public boolean Grant(String userName, String tableName, int authority) throws Exception {
        String userTableName="USER_"+userName;
        boolean flag;
        //selec1 ad
        Vector<String> attrNames2= new Vector<>();
        attrNames2.add("TableName");
        Vector<Condition> conditions2= new Vector<>();
        Condition tconditionl2= new Condition("TableName", "=", tableName);
        conditions2.add(tconditionl2);
        Vector<TableRow> ret2 = API.select(adTableName, attrNames2, conditions2);
        int num2 = ret2.size();
        if(num2>0){
            flag=true;
        }
        else{
            System.out.println("管理员"+adName+"没有表"+tableName+"的权限！");
            return false;
        }

        //selec2 user
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        String tAuthority;
        if(ret.size()>0){
            tAuthority= ret.get(0).get_attribute_value(0);
        }
        else{
            Grantinit(userName,tableName);
            tAuthority="0000";
        }

        switch (authority) {
            case 1:
                tAuthority= "1"+tAuthority.substring(1);
                break;
            case 2:
                tAuthority= tAuthority.substring(0,1)+"1"+tAuthority.substring(2);
                break;
            case 3:
                tAuthority= tAuthority.substring(0,2)+"1"+tAuthority.substring(3);
                break;
            case 4:
                tAuthority= tAuthority.substring(0,3)+"1";
                break;
            default:
                System.out.println("权限编码输入错误！");
                return false;
        }

        int num=0;
        Vector<Condition> conditions= new Vector<>();
        Condition tconditionl= new Condition("TableName", "=", tableName);
        conditions.add(tconditionl);
        num = API.delete_row(userTableName, conditions);
        System.out.println(num + " row(s) are deleted from "+userTableName);
        if(num==0){
            flag=false;
        }
        else{
            TableRow tableRow = new TableRow();
            tableRow.add_attribute_value(tableName);
            tableRow.add_attribute_value(tAuthority);
            //System.out.println(tAuthority);
            flag= API.insert_row(userTableName, tableRow);
            if(flag){
                System.out.println("用户" + userName+"授权成功！");
            }
            else{
                System.out.println("用户" + userName+"授权失败！");
            }

        }

        return flag;
    }

    //收回权限
    public boolean Revoke(String userName, String tableName, int authority) throws Exception {
        String userTableName="USER_"+userName;
        boolean flag;
        //select1 ad
        Vector<String> attrNames2= new Vector<>();
        attrNames2.add("TableName");
        Vector<Condition> conditions2= new Vector<>();
        Condition tconditionl2= new Condition("TableName", "=", tableName);
        conditions2.add(tconditionl2);
        Vector<TableRow> ret2 = API.select(adTableName, attrNames2, conditions2);
        int num1 = ret2.size();
        if(num1>0){
            flag=true;
        }
        else{
            System.out.println("管理员"+adName+"没有表"+tableName+"的权限！");
            return false;
        }

        //selec2 user
        Vector<String> attrNames= new Vector<>();
        attrNames.add("Authority");
        Vector<Condition> conditions1= new Vector<>();
        Condition tconditionl1= new Condition("TableName", "=", tableName);
        conditions1.add(tconditionl1);
        Vector<TableRow> ret = API.select(userTableName, attrNames, conditions1);
        int num2=ret.size();
        String tAuthority ;
        if(num2>0){
            tAuthority = ret.get(0).get_attribute_value(0);
        }
        else{
            System.out.println("用户" + userName+"收回权限成功！");
            return true;
        }
        switch (authority) {
            case 1:
                tAuthority= "0"+tAuthority.substring(1);
                break;
            case 2:
                tAuthority= tAuthority.substring(0,1)+"0"+tAuthority.substring(2);
                break;
            case 3:
                tAuthority= tAuthority.substring(0,2)+"0"+tAuthority.substring(3);
                break;
            case 4:
                tAuthority= tAuthority.substring(0,3)+"0";
                break;
            default:
                System.out.println("权限编码输入错误！");
                return false;
        }

        int num=0;
        Vector<Condition> conditions= new Vector<>();
        Condition tconditionl= new Condition("TableName", "=", tableName);
        conditions.add(tconditionl);
        num = API.delete_row(userTableName, conditions);
        System.out.println(num + " row(s) are deleted from "+userTableName);
        if(num==0){
            flag=false;
        }
        else{
            TableRow tableRow = new TableRow();
            tableRow.add_attribute_value(tableName);
            tableRow.add_attribute_value(tAuthority);
            flag= API.insert_row(userTableName, tableRow);
            //System.out.println(tAuthority);
            if(flag){
                System.out.println("用户" + userName+"收回权限成功！");
            }
            else{
                System.out.println("用户" + userName+"收回权限失败！");
            }

        }

        return flag;
    }

}
