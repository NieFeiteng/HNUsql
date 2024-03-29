import CATALOGMANAGER.Attribute;
import CATALOGMANAGER.CatalogManager;
import CATALOGMANAGER.NumType;
import CATALOGMANAGER.Table;
import CONNECT.Connect;
import INDEXMANAGER.Index;
import INDEXMANAGER.IndexManager;
import RECORDMANAGER.Condition;
import RECORDMANAGER.RecordManager;
import RECORDMANAGER.TableRow;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Interpreter {

//----zirui----//

    public static Logger logger = Logger.getLogger(String.valueOf(Interpreter.class));

    static {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler("HUNSql.log");
            fileHandler.setLevel(Level.INFO);
            //fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    SimpleDateFormat logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(record.getMillis());
                    return record.getLevel()
                            + " "
                            + logTime.format(cal.getTime())
                            + " "
                            + record.getSourceClassName().substring(
                            record.getSourceClassName().lastIndexOf(".") + 1,
                            record.getSourceClassName().length())
                            + "."
                            + record.getSourceMethodName()
                            + "() : "
                            + record.getMessage() + "\n";
                }
            });
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //----zirui----//

    //    private static boolean nestLock = false; //not permit to use nesting sql file execution
    private static int execFile = 0;
    public static boolean rtype;
    public static String rusername;
    public static String rpassword;
    public static DBManager rmanager;
    public static DBUser ruser;

    public static void main(String[] args) {
        try {
            API.initial();
            boolean type;
            while (true) {
                Connect.change_username(Connect.read_line_from_client());
                Connect.change_password(Connect.read_line_from_client());

                /******检查登录是否正确*******/

                String username = Connect.get_username();
                String password = Connect.get_password();

                //查PASSWORD表
                Vector<String> rattrNames = new Vector<>();
                rattrNames.add("Type");
                Vector<Condition> rconditions = new Vector<>();
                Condition rconditionl1 = new Condition("Name", "=", username);
                Condition rconditionl2 = new Condition("Password", "=", password);
                rconditions.add(rconditionl1);
                rconditions.add(rconditionl2);
                Vector<TableRow> rret = API.select("PASSWORD", rattrNames, rconditions);
                int rnum = rret.size();
                if (rnum > 0) {
                    if (rret.get(0).get_attribute_value(0).equals("1")) {
                        type = true;
                    } else {
                        type = false;
                    }

                    String result = "ok";
                    Connect.out_to_client(result);
                    System.out.println("Welcome to HNUsql~ " + Connect.get_username());
                    Connect.out_to_client("Welcome to HNUsql~ " + Connect.get_username());
                    break;
                } else {
                    String result = "Wrong user name or password";
                    Connect.out_to_client(result);

                }
            }
            interpret(Connect.get_sysbuf(), type, Connect.get_username(), Connect.get_username());

        } catch (IOException e) {
            System.out.println("101 Run time error : IO exception occurs");
        } catch (Exception e) {
            System.out.println("Default error: " + e.getMessage());
        }

    }

    private static void interpret(BufferedReader reader, boolean type, String username, String password) throws Exception {
        rtype = type;
        rusername = username;
        rpassword = password;
        if (rtype) {
            rmanager = new DBManager(username, password);
        } else {
            ruser = new DBUser(username, password);
        }

        String restState = ""; //rest statement after ';' in last line

        while (true) { //read for each statement

            int index;
            String line;
            StringBuilder statement = new StringBuilder();
            if (restState.contains(";")) { // resetLine contains whole statement
                index = restState.indexOf(";");
                statement.append(restState.substring(0, index));
                restState = restState.substring(index + 1);
            } else {
                statement.append(restState); //add rest line
                statement.append(" ");
                if (execFile == 0) {//不断读取命令行输入的SQL语句
                    System.out.print("HNUsql-->");
                    Connect.out_to_client("HNUsql-->");
                }
//                System.out.print("-->");
                while (true) {  //read whole statement until ';'

                    if (execFile != 0) line = reader.readLine();  //改用connect中的接口从客户端读取数据
                    else line = Connect.read_line_from_client();//从客户端读取数据

                    if (line == null) { //read the file tail
                        if (execFile != 0) reader.close();
                        return;
                    } else if (line.contains(";")) { //last line
                        index = line.indexOf(";");
                        statement.append(line.substring(0, index));
                        restState = line.substring(index + 1); //set reset statement
                        break;
                    } else {
                        statement.append(line);
                        statement.append(" ");
                        if (execFile == 0) {
                            System.out.print("HNUsql-->");
                            Connect.out_to_client("HNUsql-->");
                        }
//                        System.out.print("-->"); //next line
                    }
                }
            }

            //after get the whole statement
            String result = statement.toString().trim().replaceAll("\\s+", " ");
            String[] tokens = result.split(" ");

//            Date date=new Date();
//            System.out.print(result +" | " +date.toString() + " | ");

            try {
                if (tokens.length == 1 && tokens[0].equals(""))
                    throw new QException(0, 200, "No statement specified");
                switch (tokens[0]) { //match keyword
                    case "create":
                        if (tokens.length == 1)
                            throw new QException(0, 201, "Can't find create object");
                        switch (tokens[1]) {
                            case "table":
                                parse_create_table(result);
                                break;
                            case "user":
                                parse_create_user(result);
                                break;
                            case "index":
                                parse_create_index(result);
                                break;
                            default:
                                throw new QException(0, 202, "Can't identify " + tokens[1]);
                        }
                        break;
                    case "drop":
                        if (tokens.length == 1)
                            throw new QException(0, 203, "Can't find drop object");
                        switch (tokens[1]) {
                            case "table":
                                parse_drop_table(result);
                                break;
                            case "user":
                                parse_drop_user(result);
                                break;
                            case "index":
                                parse_drop_index(result);
                                break;
                            default:
                                throw new QException(0, 204, "Can't identify " + tokens[1]);
                        }
                        break;
                    case "select":
                        parse_select(result);
                        break;
                    case "insert":
                        parse_insert(result);
                        break;
                    case "delete":
                        parse_delete(result);
                        break;
                    case "quit":
                        parse_quit(result, reader);
                        break;
                    case "execfile":
                        parse_sql_file(result);
                        break;
                    case "show":
                        parse_show(result);
                        break;
                    case "update":
                        parse_update(result);
                        break;
                    case "grant":
                        parse_grant(result);
                        break;
                    case "revoke":
                        parse_revoke(result);
                        break;
                    default:
                        throw new QException(0, 205, "Can't identify " + tokens[0]);
                }
            } catch (QException e) {
                Connect.out_to_client(e.status + " " + QException.ex[e.type] + ": " + e.msg);
                System.out.println(e.status + " " + QException.ex[e.type] + ": " + e.msg);
            } catch (Exception e) {
                Connect.out_to_client("Default error: " + e.getMessage());
                System.out.println("Default error: " + e.getMessage());
            }
        }

    }

    private static void parse_show(String statement) throws Exception {
        String type = Utils.substring(statement, "show ", "").trim();
        if (type.equals("tables")) {
            CatalogManager.show_table();
        } else if (type.equals("indexes")) {
            CatalogManager.show_index();
        } else throw new QException(0, 323, "Can not find valid key word after 'show'!");
    }

    private static void parse_create_table(String statement) throws Exception {
        if (!rtype) {
            System.out.println("普通用户没有创建表的权利！");
            Connect.out_to_client("-->普通用户没有创建表的权利！");
            return;
        }

        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.replaceAll(" *, *", ",");
        statement = statement.trim();
        statement = statement.replaceAll("^create table", "").trim(); //skip create table keyword

        int startIndex, endIndex;
        if (statement.equals("")) //no statement after create table
            throw new QException(0, 401, "Must specify a table name");

        endIndex = statement.indexOf(" ");
        if (endIndex == -1)  //no statement after create table xxx
            throw new QException(0, 402, "Can't find attribute definition");

        String tableName = statement.substring(0, endIndex); //get table name
        startIndex = endIndex + 1; //start index of '('
        if (!statement.substring(startIndex).matches("^\\(.*\\)$"))  //check brackets
            throw new QException(0, 403, "Can't not find the definition brackets in table " + tableName);

        int length;
        String[] attrParas, attrsDefine;
        String attrName, attrType, attrLength = "", primaryName = "";
        boolean attrUnique;
        Attribute attribute;
        Vector<Attribute> attrVec = new Vector<>();

        attrsDefine = statement.substring(startIndex + 1).split(","); //get each attribute definition
        for (int i = 0; i < attrsDefine.length; i++) { //for each attribute
            if (i == attrsDefine.length - 1) { //last line
                attrParas = attrsDefine[i].trim().substring(0, attrsDefine[i].length() - 1).split(" "); //remove last ')'
            } else {
                attrParas = attrsDefine[i].trim().split(" ");
            } //split each attribute in parameters: name, type,（length) (unique)

            if (attrParas[0].equals("")) { //empty
                throw new QException(0, 404, "Empty attribute in table " + tableName);
            } else if (attrParas[0].equals("primary")) { //primary key definition
                if (attrParas.length != 3 || !attrParas[1].equals("key"))  //not as primary key xxxx
                    throw new QException(0, 405, "Error definition of primary key in table " + tableName);
                if (!attrParas[2].matches("^\\(.*\\)$"))  //not as primary key (xxxx)
                    throw new QException(0, 406, "Error definition of primary key in table " + tableName);
                if (!primaryName.equals("")) //already set primary key
                    throw new QException(0, 407, "Redefinition of primary key in table " + tableName);

                primaryName = attrParas[2].substring(1, attrParas[2].length() - 1); //set primary key
            } else { //ordinary definition
                if (attrParas.length == 1)  //only attribute name
                    throw new QException(0, 408, "Incomplete definition in attribute " + attrParas[0]);
                attrName = attrParas[0]; //get attribute name
                attrType = attrParas[1]; //get attribute type
                for (int j = 0; j < attrVec.size(); j++) { //check whether name redefines
                    if (attrName.equals(attrVec.get(j).attributeName))
                        throw new QException(0, 409, "Redefinition in attribute " + attrParas[0]);
                }
                if (attrType.equals("int") || attrType.equals("float")) { //check type
                    endIndex = 2; //expected end index
                } else if (attrType.equals("char")) {
                    if (attrParas.length == 2)  //no char length
                        throw new QException(0, 410, "ust specify char length in " + attrParas[0]);
                    if (!attrParas[2].matches("^\\(.*\\)$"))  //not in char (x) form
                        throw new QException(0, 411, "Wrong definition of char length in " + attrParas[0]);

                    attrLength = attrParas[2].substring(1, attrParas[2].length() - 1); //get length
                    try {
                        length = Integer.parseInt(attrLength); //check the length
                    } catch (NumberFormatException e) {
                        throw new QException(0, 412, "The char length in " + attrParas[0] + " dosen't match a int type or overflow");
                    }
                    if (length < 1 || length > 255)
                        throw new QException(0, 413, "The char length in " + attrParas[0] + " must be in [1,255] ");
                    endIndex = 3; //expected end index
                } else { //unmatched type
                    throw new QException(0, 414, "Error attribute type " + attrType + " in " + attrParas[0]);
                }

                if (attrParas.length == endIndex) { //check unique constraint
                    attrUnique = false;
                } else if (attrParas.length == endIndex + 1 && attrParas[endIndex].equals("unique")) {  //unique
                    attrUnique = true;
                } else { //wrong definition
                    throw new QException(0, 415, "Error constraint definition in " + attrParas[0]);
                }

                if (attrType.equals("char")) { //generate attribute
                    attribute = new Attribute(attrName, NumType.valueOf(attrType.toUpperCase()), Integer.parseInt(attrLength), attrUnique);
                } else {
                    attribute = new Attribute(attrName, NumType.valueOf(attrType.toUpperCase()), attrUnique);
                }
                attrVec.add(attribute);
            }
        }

        if (primaryName.equals(""))  //check whether set the primary key
            throw new QException(0, 416, "Not specified primary key in table " + tableName);

        Table table = new Table(tableName, primaryName, attrVec); // create table
        API.create_table(tableName, table);

        rmanager.Ad_AddTable(tableName);

        System.out.println("-->Create table " + tableName + " successfully");
        Connect.out_to_client("-->Create table " + tableName + " successfully");
    }

    private static void parse_drop_table(String statement) throws Exception {
        if (!rtype) {
            System.out.println("普通用户没有删除表的权利！");
            Connect.out_to_client("-->普通用户没有删除表的权利！");
            return;
        }

        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new QException(0, 601, "Not specify table name");
        if (tokens.length != 3)
            throw new QException(0, 602, "Extra parameters in drop table");

        String tableName = tokens[2]; //get table name
        API.drop_table(tableName);

        rmanager.Ad_DropTable(tableName);

        System.out.println("-->Drop table " + tableName + " successfully");
        Connect.out_to_client("-->Drop table " + tableName + " successfully");
    }

    private static void parse_create_index(String statement) throws Exception {
        statement = statement.replaceAll("\\s+", " ");
        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.trim();

        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new QException(0, 701, "Not specify index name");

        String indexName = tokens[2]; //get index name
        if (tokens.length == 3 || !tokens[3].equals("on"))
            throw new QException(0, 702, "Must add keyword 'on' after index name " + indexName);
        if (tokens.length == 4)
            throw new QException(0, 703, "Not specify table name");

        String tableName = tokens[4]; //get table name
        if (tokens.length == 5)
            throw new QException(0, 704, "Not specify attribute name in table " + tableName);

        String attrName = tokens[5];
        if (!attrName.matches("^\\(.*\\)$"))  //not as (xxx) form
            throw new QException(0, 705, "Error in specifiy attribute name " + attrName);

        attrName = attrName.substring(1, attrName.length() - 1); //extract attribute name
        if (tokens.length != 6)
            throw new QException(0, 706, "Extra parameters in create index");
        if (!CatalogManager.is_unique(tableName, attrName))
            throw new QException(1, 707, "Not a unique attribute");

        Index index = new Index(indexName, tableName, attrName);
        API.create_index(index);
        System.out.println("-->Create index " + indexName + " successfully");
        Connect.out_to_client("-->Create index \" + indexName + \" successfully");
    }

    private static void parse_drop_index(String statement) throws Exception {
        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new QException(0, 801, "Not specify index name");
        if (tokens.length != 3)
            throw new QException(0, 802, "Extra parameters in drop index");

        String indexName = tokens[2]; //get table name
        API.drop_index(indexName);
        System.out.println("-->Drop index " + indexName + " successfully");
        Connect.out_to_client("-->Drop index \" + indexName + \" successfully");
    }

    private static void parse_select(String statement) throws Exception {
        try{
        //select ... from ... where ...
        String attrStr = Utils.substring(statement, "select ", " from");//取出各个部分的具体内容，如表名
        String tabStr = Utils.substring(statement, "from ", " where");
        String conStr = Utils.substring(statement, "where ", "");

        Vector<Condition> conditions;
        Vector<String> attrNames;
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        if (attrStr.equals(""))
            throw new QException(0, 250, "Can not find key word 'from' or lack of blank before from!");
        if (attrStr.trim().equals("*")) {
            //select all attributes
            if (tabStr.equals("")) {  // select * from [];
                tabStr = Utils.substring(statement, "from ", "");

                if (!rtype) {
                    if (!ruser.Select_Authority(tabStr)) {
                        System.out.println("用户" + rusername + "没有select此表的权利！");
                        Connect.out_to_client("-->" + "用户" + rusername + "没有select此表的权利！");

                        return;
                    }
                }

                Vector<TableRow> ret = API.select(tabStr, new Vector<>(), new Vector<>());
                endTime = System.currentTimeMillis();
                Utils.print_rows(ret, tabStr);
            } else { //select * from [] where [];

                if (!rtype) {
                    if (!ruser.Select_Authority(tabStr)) {
                        System.out.println("用户" + rusername + "没有select此表的权利！");
                        Connect.out_to_client("-->" + "用户" + rusername + "没有select此表的权利！");

                        return;
                    }
                }

                String[] conSet = conStr.split(" *and *");
                //get condition vector
                conditions = Utils.create_conditon(conSet);
                Vector<TableRow> ret = API.select(tabStr, new Vector<>(), conditions);
                endTime = System.currentTimeMillis();
                Utils.print_rows(ret, tabStr);
            }
        } else {
            attrNames = Utils.convert(attrStr.split(" *, *")); //get attributes list
            if (tabStr.equals("")) {  //select [attr] from [];
                tabStr = Utils.substring(statement, "from ", "");

                if (!rtype) {
                    if (!ruser.Select_Authority(tabStr)) {
                        System.out.println("用户" + rusername + "没有select此表的权利！");
                        Connect.out_to_client("-->" + "用户" + rusername + "没有select此表的权利！");

                        return;
                    }
                }

                Vector<TableRow> ret = API.select(tabStr, attrNames, new Vector<>());
                endTime = System.currentTimeMillis();
                Utils.print_rows(ret, tabStr);
            } else { //select [attr] from [table] where

                if (!rtype) {
                    if (!ruser.Select_Authority(tabStr)) {
                        System.out.println("用户" + rusername + "没有select此表的权利！");
                        Connect.out_to_client("-->" + "用户" + rusername + "没有select此表的权利！");

                        return;
                    }
                }

                String[] conSet = conStr.split(" *and *");
                //get condition vector
                conditions = Utils.create_conditon(conSet);
                Vector<TableRow> ret = API.select(tabStr, attrNames, conditions);
                endTime = System.currentTimeMillis();
                Utils.print_rows(ret, tabStr);
            }
        }
        double usedTime = (endTime - startTime) / 1000.0;
        System.out.println("Finished in " + usedTime + " s");
            logger.info("执行成功，用时 "+ usedTime + " s，" + statement);
            Connect.out_to_client("Finished in " + usedTime + " s");
        }catch (Exception e){
            logger.info("执行失败 " + statement);
        }

    }

    private static void parse_insert(String statement) throws Exception {
        try{
        statement = statement.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
        statement = statement.replaceAll(" *, *", ",");
        statement = statement.trim();
        statement = statement.replaceAll("^insert", "").trim();  //skip insert keyword

        int startIndex, endIndex;
        if (statement.equals(""))
            throw new QException(0, 901, "Must add keyword 'into' after insert ");

        endIndex = statement.indexOf(" "); //check into keyword
        if (endIndex == -1)
            throw new QException(0, 902, "Not specify the table name");
        if (!statement.substring(0, endIndex).equals("into"))
            throw new QException(0, 903, "Must add keyword 'into' after insert");

        startIndex = endIndex + 1;
        endIndex = statement.indexOf(" ", startIndex); //check table name
        if (endIndex == -1)
            throw new QException(0, 904, "Not specify the insert value");

        String tableName = statement.substring(startIndex, endIndex); //get table name
        startIndex = endIndex + 1;
        endIndex = statement.indexOf(" ", startIndex); //check values keyword
        if (endIndex == -1)
            throw new QException(0, 905, "Syntax error: Not specify the insert value");

        if (!statement.substring(startIndex, endIndex).equals("values"))
            throw new QException(0, 906, "Must add keyword 'values' after table " + tableName);

        startIndex = endIndex + 1;
        if (!statement.substring(startIndex).matches("^\\(.*\\)$"))  //check brackets
            throw new QException(0, 907, "Can't not find the insert brackets in table " + tableName);

        String[] valueParas = statement.substring(startIndex + 1).split(","); //get attribute tokens
        TableRow tableRow = new TableRow();

        for (int i = 0; i < valueParas.length; i++) {
            if (i == valueParas.length - 1)  //last attribute
                valueParas[i] = valueParas[i].substring(0, valueParas[i].length() - 1);
            if (valueParas[i].equals("")) //empty attribute
                throw new QException(0, 908, "Empty attribute value in insert value");
            if (valueParas[i].matches("^\".*\"$") || valueParas[i].matches("^\'.*\'$"))  // extract from '' or " "
                valueParas[i] = valueParas[i].substring(1, valueParas[i].length() - 1);
            tableRow.add_attribute_value(valueParas[i]); //add to table row
        }

        //Check unique attributes
        if (tableRow.get_attribute_size() != CatalogManager.get_attribute_num(tableName))
            throw new QException(1, 909, "Attribute number doesn't match");
        Vector<Attribute> attributes = CatalogManager.get_table(tableName).attributeVector;
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = attributes.get(i);
            if (attr.isUnique) {
                Condition cond = new Condition(attr.attributeName, "=", valueParas[i]);
                if (CatalogManager.is_index_key(tableName, attr.attributeName)) {
                    Index idx = CatalogManager.get_index(CatalogManager.get_index_name(tableName, attr.attributeName));
                    if (IndexManager.select(idx, cond).isEmpty())
                        continue;
                } else {
                    Vector<Condition> conditions = new Vector<>();
                    conditions.add(cond);
                    Vector<TableRow> res = RecordManager.select(tableName, conditions); //Supposed to be empty
                    if (res.isEmpty())
                        continue;
                }
                throw new QException(1, 910, "Duplicate unique key: " + attr.attributeName);
            }
        }

        if (!rtype) {
            if (!ruser.Add_Authority(tableName)) {
                System.out.println("用户" + rusername + "没有insert表" + tableName + "的权利！");
                Connect.out_to_client("-->" + "用户" + rusername + "没有insert表" + tableName + "的权利！");

                return;
            }
        }

        API.insert_row(tableName, tableRow);
        System.out.println("-->Insert successfully");
        Connect.out_to_client("-->Insert successfully");
            logger.info("插入成功，" + statement);
        }catch (Exception e){
            logger.info("执行失败，" + statement);
        }
    }

    private static void parse_delete(String statement) throws Exception {
        try{
        //delete from [tabName] where []
        int num;
        String tabStr = Utils.substring(statement, "from ", " where").trim();
        String conStr = Utils.substring(statement, "where ", "").trim();

        Vector<Condition> conditions;
        Vector<String> attrNames;
        String tempstr = "";
        if (tabStr.equals("")) {  //delete from ...
            tabStr = Utils.substring(statement, "from ", "").trim();

            if (!rtype) {
                if (!ruser.Delete_Authority(tabStr)) {
                    System.out.println("用户" + rusername + "没有delete表" + tabStr + "的权利！");
                    Connect.out_to_client("-->" + "用户" + rusername + "没有delete表" + tabStr + "的权利！");

                    return;
                }
            }

            num = API.delete_row(tabStr, new Vector<>());
            System.out.println("Query ok! " + num + " row(s) are deleted");
            String s = String.valueOf(num);
            tempstr = "Query ok! " + s + " row(s) are deleted";
            Connect.out_to_client("Query ok! " + s + " row(s) are deleted");
        } else {  //delete from ... where ...

            if (!rtype) {
                if (!ruser.Delete_Authority(tabStr)) {
                    System.out.println("用户" + rusername + "没有delete表" + tabStr + "的权利！");
                    Connect.out_to_client("-->" + "用户" + rusername + "没有delete表" + tabStr + "的权利！");

                    return;
                }
            }

            String[] conSet = conStr.split(" *and *");
            //get condition vector
            conditions = Utils.create_conditon(conSet);
            num = API.delete_row(tabStr, conditions);
            System.out.println("Query ok! " + num + " row(s) are deleted");
            String s = String.valueOf(num);
            tempstr = "Query ok! " + s + " row(s) are deleted";
            Connect.out_to_client(tempstr);
        }
            logger.info("删除成功，" + statement);
        }catch (Exception e){
            logger.info("删除失败，" + statement);
        }
    }

    private static void parse_update(String statement) throws Exception {
        //select ... from ... where ...
        //update table set att = value; att=value where ...
        String tabStr = Utils.substring(statement, "update ", " set");
        String conStr = Utils.substring(statement, "where ", "");
        String setStr = Utils.substring(statement, "set ", "where");//要更新的属性和值

        if (!rtype) {
            if (!ruser.Update_Authority(tabStr)) {
                System.out.println("用户" + rusername + "没有update表" + tabStr + "的权利！");
                Connect.out_to_client("-->" + "用户" + rusername + "没有update表" + tabStr + "的权利！");

                return;
            }
        }

        Vector<Condition> conditions;
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        Vector<TableRow> ret;
        if (tabStr.equals(""))
            throw new QException(0, 902, "Not specify the table name");
        if (conStr.equals("")) {  // select * from [];
            //update table set att=value;
            setStr = Utils.substring(statement, "set ", "");
            ret = API.select(tabStr, new Vector<>(), new Vector<>());
        } else { //select * from [] where [];
            String[] conSet = conStr.split(" *and *");
            //get condition vector
            conditions = Utils.create_conditon(conSet);
            ret = API.select(tabStr, new Vector<>(), conditions);
        }

        setStr = setStr.toString().trim().replaceAll("\\s+", "");
        ;
        String[] token0 = setStr.split(";");
        int num_of_set_att = token0.length;
        String[] tokens = new String[num_of_set_att * 3];
        if (setStr.contains("-=")) {
            for (int ii = 0; ii < num_of_set_att; ii++) {
                tokens[ii] = Utils.substring(token0[ii], "", "-=");
                tokens[ii + 1] = "-=";
                tokens[ii + 2] = Utils.substring(token0[ii], "-=", "");
            }
        } else if (setStr.contains("+=")) {
            for (int ii = 0; ii < num_of_set_att; ii++) {
                tokens[ii] = Utils.substring(token0[ii], "", "\\+=");
                tokens[ii + 1] = "+=";
                tokens[ii + 2] = Utils.substring(token0[ii], "\\+=", "");
            }
        } else {
            for (int ii = 0; ii < num_of_set_att; ii++) {
                tokens[ii] = Utils.substring(token0[ii], "", "=");
                tokens[ii + 1] = "=";
                tokens[ii + 2] = Utils.substring(token0[ii], "=", "");
            }
        }

//       Connect.out_to_client("Token:" + tokens[0] + " " + tokens[1] + " "+ tokens[2]);

        int attrSize = ret.get(0).get_attribute_size();

        Table tmpTable = CatalogManager.get_table(tabStr);//获得表
        String attrName = "";
        Attribute tmpAttribute1;
        int isattright = 0;
        for (int k = 0; k < tokens.length; k += 3) {
            for (int j = 0; j < attrSize; j++) {
                tmpAttribute1 = tmpTable.attributeVector.get(j);
                attrName = tmpAttribute1.attributeName;//属性名  
                if (attrName.equals(tokens[k])) {//加上要更新的值
                    isattright = 1;
                    break;
                }
            }
            if (isattright == 0) {
                Connect.out_to_client(tabStr + " " + attrName + " " + tokens[k]);
                throw new QException(0, 907, "Can't not find the attributeName in table " + tabStr);
            }
            isattright = 0;
        }
        //delete from [tabName] where []
        Vector<Condition> conditions1;
        if (conStr.equals("")) {  //delete from ...
            API.delete_row(tabStr, new Vector<>());
        } else {  //delete from ... where ...
            String[] conSet = conStr.split(" *and *");
            //get condition vector
            conditions1 = Utils.create_conditon(conSet);
            API.delete_row(tabStr, conditions1);
        }


        for (int l = 0; l < ret.size(); l++) {//insert into student values(1080100006,'name6',89.5);
            TableRow row = ret.get(l);
            String insertstate = "insert into " + tabStr + " values(";
            attrName = "";
            Attribute tmpAttribute;
            for (int j = 0; j < attrSize; j++) {
                tmpAttribute = tmpTable.attributeVector.get(j);
                String tmpvalue = row.get_attribute_value(j);
                attrName = tmpAttribute.attributeName;//属性名

                for (int k = 0; k < tokens.length; k += 3) {
                    if (attrName.equals(tokens[k])) {//加上要更新的值

                        if (tokens[k + 1].equals("+=")) {
                            if (tmpAttribute.type.get_type() == NumType.INT) tmpvalue =
                                    String.valueOf(Integer.parseInt(tmpvalue) + Integer.parseInt(tokens[k + 2]));
                            else if (tmpAttribute.type.get_type() == NumType.FLOAT) tmpvalue =
                                    String.valueOf(Float.parseFloat(tmpvalue) + Float.parseFloat(tokens[k + 2]));
                            else tmpvalue = tokens[k + 2];
                        } else if (tokens[k + 1].equals("-=")) {
                            if (tmpAttribute.type.get_type() == NumType.INT) tmpvalue =
                                    String.valueOf(Integer.parseInt(tmpvalue) - Integer.parseInt(tokens[k + 2]));
                            else if (tmpAttribute.type.get_type() == NumType.FLOAT) tmpvalue =
                                    String.valueOf(Float.parseFloat(tmpvalue) - Float.parseFloat(tokens[k + 2]));
                            else tmpvalue = tokens[k + 2];
                        } else
                            tmpvalue = tokens[k + 2];
                        break;
                    }
                }

                if (tmpAttribute.type.get_type() == NumType.CHAR)//CHAR,INT,FLOAT
                {
                    insertstate += "\'" + tmpvalue + "\'";
                } else insertstate += tmpvalue;
                if (j != attrSize - 1) insertstate += ",";
            }
            insertstate += ")";
            insertstate = insertstate.replaceAll(" *\\( *", " (").replaceAll(" *\\) *", ") ");
            insertstate = insertstate.replaceAll(" *, *", ",");
            insertstate = insertstate.trim();
            insertstate = insertstate.replaceAll("^insert", "").trim();  //skip insert keyword

            int startIndex, endIndex;
            // if (insertstate.equals(""))
            //     throw new QException(0, 901, "Must add keyword 'into' after insert ");

            endIndex = insertstate.indexOf(" "); //check into keyword
            if (endIndex == -1)
                throw new QException(0, 902, "Not specify the table name");

            startIndex = endIndex + 1;
            endIndex = insertstate.indexOf(" ", startIndex); //check table name
            if (endIndex == -1)
                throw new QException(0, 904, "Not specify the insert value");

            String tableName = insertstate.substring(startIndex, endIndex); //get table name
            startIndex = endIndex + 1;
            endIndex = insertstate.indexOf(" ", startIndex); //check values keyword
            if (endIndex == -1)
                throw new QException(0, 905, "Syntax error: Not specify the insert value");

            if (!insertstate.substring(startIndex, endIndex).equals("values"))
                throw new QException(0, 906, "Must add keyword 'values' after table " + tableName);

            startIndex = endIndex + 1;
            if (!insertstate.substring(startIndex).matches("^\\(.*\\)$"))  //check brackets
                throw new QException(0, 907, "Can't not find the insert brackets in table " + tableName);

            String[] valueParas = insertstate.substring(startIndex + 1).split(","); //get attribute tokens
            TableRow tableRow = new TableRow();

            for (int i = 0; i < valueParas.length; i++) {
                if (i == valueParas.length - 1)  //last attribute
                    valueParas[i] = valueParas[i].substring(0, valueParas[i].length() - 1);
                if (valueParas[i].equals("")) //empty attribute
                    throw new QException(0, 909, "Empty attribute value in updata value");
                if (valueParas[i].matches("^\".*\"$") || valueParas[i].matches("^\'.*\'$"))  // extract from '' or " "
                    valueParas[i] = valueParas[i].substring(1, valueParas[i].length() - 1);
                tableRow.add_attribute_value(valueParas[i]); //add to table row
            }

            //Check unique attributes
            if (tableRow.get_attribute_size() != CatalogManager.get_attribute_num(tableName))
                throw new QException(1, 909, "Attribute number doesn't match");
            Vector<Attribute> attributes = CatalogManager.get_table(tableName).attributeVector;
            for (int i = 0; i < attributes.size(); i++) {
                Attribute attr = attributes.get(i);
                if (attr.isUnique) {
                    Condition cond = new Condition(attr.attributeName, "=", valueParas[i]);
                    if (CatalogManager.is_index_key(tableName, attr.attributeName)) {
                        Index idx = CatalogManager.get_index(CatalogManager.get_index_name(tableName, attr.attributeName));
                        if (IndexManager.select(idx, cond).isEmpty())
                            continue;
                    } else {
                        Vector<Condition> conditions2 = new Vector<>();
                        conditions2.add(cond);
                        Vector<TableRow> res = RecordManager.select(tableName, conditions2); //Supposed to be empty
                        if (res.isEmpty())
                            continue;
                    }
                    throw new QException(1, 910, "Duplicate unique key: " + attr.attributeName);
                }
            }
            API.insert_row(tableName, tableRow);
        }

        endTime = System.currentTimeMillis();

        System.out.println("-->Update successfully");
        Connect.out_to_client("-->Update successfully");

//        double usedTime = (endTime - startTime) / 1000.0;
//        System.out.println("Finished in " + usedTime + " s");
//        Connect.out_to_client("Finished in " + usedTime + " s");
    }


    private static void parse_quit(String statement, BufferedReader reader) throws Exception {
        try{
        String[] tokens = statement.split(" ");
        if (tokens.length != 1)
            throw new QException(0, 1001, "Extra parameters in quit");

        API.store();
//        reader.close(); //
        System.out.println("Bye");
        Connect.out_to_client("Bye");
        Connect.close_server();//关闭
        System.exit(0);
            logger.info("退出成功，" + statement);
        }catch (Exception e){
            logger.info("退出失败，" + statement);
        }
    }

    private static void parse_sql_file(String statement) throws Exception {
        try {
        execFile++;
        String[] tokens = statement.split(" ");
        if (tokens.length != 2)
            throw new QException(0, 1101, "Extra parameters in sql file execution");

        String fileName = tokens[1];
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
//            if (nestLock)  //first enter in sql file execution
//                throw new QException(0, 1102, "Can't use nested file execution");
//            nestLock = true; //lock, avoid nested execution
            interpret(fileReader, rtype, rusername, rpassword);
        } catch (FileNotFoundException e) {
            throw new QException(1, 1103, "Can't find the file");
        } catch (IOException e) {
            throw new QException(1, 1104, "IO exception occurs");
        } finally {
            execFile--;
//            nestLock = false; //unlock
        }
        logger.info("解析SQL文件成功，" + statement);
    }catch(Exception e){
        logger.info("解析SQL文件失败，" + statement);
        }
    }

    private static void parse_create_user(String statement) throws Exception {
        if(!rtype){
            System.out.println("普通用户没有创建用户的权利！");
            Connect.out_to_client("-->普通用户没有创建用户的权利！");
            return;
        }
        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new QException(0, 601, "Not specify user name or password");
        if (tokens.length != 4)
            throw new QException(0, 602, "SyntaxError!");

        if(rmanager.Ad_CreatUser(tokens[2],tokens[3])){
            Connect.out_to_client("-->creat user " + tokens[2] + " successfully");
        }
        else{
            Connect.out_to_client("-->creat user " + tokens[2] + " unsuccessfully");
        }

        return;
    }

    private static void parse_drop_user(String statement) throws Exception {
        if(!rtype){
            System.out.println("普通用户没有删除用户的权利！");
            Connect.out_to_client("-->普通用户没有删除用户的权利！");
            return;
        }

        String[] tokens = statement.split(" ");
        if (tokens.length == 2)
            throw new QException(0, 601, "Not specify user");
        if (tokens.length != 3)
            throw new QException(0, 602, "SyntaxError!");

        if(rmanager.Ad_DropUser(tokens[2])){
            Connect.out_to_client("-->drop user " + tokens[2] + " successfully");
        }
        else{
            Connect.out_to_client("-->drop user " + tokens[2] + " unsuccessfully");
        }

        return;
    }

    private static void parse_grant(String statement) throws Exception {
        if(!rtype){
            System.out.println("普通用户没有授权的权利！");
            Connect.out_to_client("-->普通用户没有授权的权利！");
            return;
        }

        String[] tokens = statement.split(" ");
        if (tokens.length != 4)
            throw new QException(0, 602, "SyntaxError!");

        int rauthority=0;
        if(tokens[3].equals("insert")){
            rauthority=1;
        }
        else if(tokens[3].equals("delete")){
            rauthority=2;
        }
        else if(tokens[3].equals("update")){
            rauthority=3;
        }
        else if(tokens[3].equals("select")){
            rauthority=4;
        }
        else{
            System.out.println("权限错误");
            Connect.out_to_client("-->权限错误");
        }

        if(rmanager.Grant(tokens[1],tokens[2],rauthority)){
            System.out.println("成功授予"+tokens[1]+"表"+tokens[2]+"的"+tokens[3]+"权限");
            Connect.out_to_client("-->"+"成功授予"+tokens[1]+"表"+tokens[2]+"的"+tokens[3]+"权限");
        }
        else {
            System.out.println("授权错误");
            Connect.out_to_client("-->授权错误");
        }

        return;
    }

    private static void parse_revoke(String statement) throws Exception {
        if(!rtype){
            System.out.println("普通用户没有权限回收的权利！");
            Connect.out_to_client("-->普通用户没有权限回收的权利！");
            return;
        }

        String[] tokens = statement.split(" ");
        if (tokens.length != 4)
            throw new QException(0, 602, "SyntaxError!");

        int rauthority=0;
        if(tokens[3].equals("insert")){
            rauthority=1;
        }
        else if(tokens[3].equals("delete")){
            rauthority=2;
        }
        else if(tokens[3].equals("update")){
            rauthority=3;
        }
        else if(tokens[3].equals("select")){
            rauthority=4;
        }
        else{
            System.out.println("权限错误");
            Connect.out_to_client("-->权限错误");
        }

        if(rmanager.Revoke(tokens[1],tokens[2],rauthority)){
            System.out.println("成功收回"+tokens[1]+"表"+tokens[2]+"的"+tokens[3]+"权限");
            Connect.out_to_client("-->"+"成功收回"+tokens[1]+"表"+tokens[2]+"的"+tokens[3]+"权限");
        }
        else {
            System.out.println("收回错误");
            Connect.out_to_client("-->收回错误");
        }

        return;
    }

}

class Utils {

    public static final int NONEXIST = -1;
    public static final String[] OPERATOR = {"<>", "<=", ">=", "=", "<", ">"};

    public static String substring(String str, String start, String end) {
        String regex = start + "(.*)" + end;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) return matcher.group(1);
        else return "";
    }

    public static <T> Vector<T> convert(T[] array) {
        Vector<T> v = new Vector<>();
        for (int i = 0; i < array.length; i++) v.add(array[i]);
        return v;
    }

    //ab <> 'c' | cab ="fabd"  | k=5  | char= '53' | int = 2
    public static Vector<Condition> create_conditon(String[] conSet) throws Exception {
        Vector<Condition> c = new Vector<>();
        for (int i = 0; i < conSet.length; i++) {
            int index = contains(conSet[i], OPERATOR);
            if (index == NONEXIST) throw new Exception("Syntax error: Invalid conditions " + conSet[i]);
            String attr = substring(conSet[i], "", OPERATOR[index]).trim();
            String value = substring(conSet[i], OPERATOR[index], "").trim().replace("\'", "").replace("\"", "");
            c.add(new Condition(attr, OPERATOR[index], value));
        }
        return c;
    }

    public static boolean check_type(String attr, boolean flag) {
        return true;
    }

    public static int contains(String str, String[] reg) {
        for (int i = 0; i < reg.length; i++) {
            if (str.contains(reg[i])) return i;
        }
        return NONEXIST;
    }

    public static void printRow(TableRow row) throws Exception {
        for (int i = 0; i < row.get_attribute_size(); i++) {
            System.out.print(row.get_attribute_value(i) + "\t");
            String str = row.get_attribute_value(i) + "\t" ;
            Connect.out_to_client(str);
        }
        System.out.println();
        Connect.out_to_client("");
    }

    public static int get_max_attr_length(Vector<TableRow> tab, int index) {
        int len = 0;
        for (int i = 0; i < tab.size(); i++) {
            int v = tab.get(i).get_attribute_value(index).length();
            len = v > len ? v : len;
        }
        return len;
    }

    public static void print_rows(Vector<TableRow> tab, String tabName) throws Exception {
        if (tab.size() == 0) {
            System.out.println("-->Query ok! 0 rows are selected");
            Connect.out_to_client("-->Query ok! 0 rows are selected");
            return;
        }
        int attrSize = tab.get(0).get_attribute_size();
        int cnt = 0;
        Vector<Integer> v = new Vector<>(attrSize);
        String tempstr="";
        for (int j = 0; j < attrSize; j++) {
            int len = get_max_attr_length(tab, j);
            String attrName = CatalogManager.get_attribute_name(tabName, j);
            if (attrName.length() > len) len = attrName.length();
            v.add(len);
            String format = "|%-" + len + "s";
            System.out.printf(format, attrName);
            String str = String.format(format,attrName);
            tempstr += str;
            cnt = cnt + len + 1;
        }
        Connect.out_to_client(tempstr + "|");
        cnt++;
        System.out.println("|");
        tempstr = "";
        for (int i = 0; i < cnt; i++) {
            System.out.print("-");
            tempstr += "-";
        }
        Connect.out_to_client(tempstr);
        Connect.out_to_client("");
        System.out.println();

        for (int i = 0; i < tab.size(); i++) {
            TableRow row = tab.get(i);
            tempstr = "";
            for (int j = 0; j < attrSize; j++) {
                String format = "|%-" + v.get(j) + "s";
                System.out.printf(format, row.get_attribute_value(j));
                String str  = String.format(format,row.get_attribute_value(j));
                tempstr += str;
            }
            System.out.println("|");
            tempstr += "|";
            Connect.out_to_client(tempstr);
        }
        System.out.println("-->Query ok! " + tab.size() + " rows are selected");
        Connect.out_to_client("-->Query ok! " + tab.size() + " rows are selected");
    }
}
