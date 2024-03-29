import BUFFERMANAGER.BufferManager;
import CATALOGMANAGER.NumType;
import INDEXMANAGER.Index;
import CATALOGMANAGER.Attribute;
import CATALOGMANAGER.CatalogManager;
import CATALOGMANAGER.Table;
import INDEXMANAGER.IndexManager;
import RECORDMANAGER.RecordManager;

import java.util.Vector;

    /*
create table PASSWORD(
    Name char(20) unique,
    Password char(10),
    Type char(1),
    primary key(Name)
);
insert into PASSWORD values('root','0123456789','1');
select * from PASSWORD;

create table ADMINISTRATOR_root(
    TableName char(20) unique,
    primary key(TableName)
);

create table test1(
id int,
primary key(id)
);
insert into test1 values(1);
select * from test1;

     */

public class Main {

    public static void main(String[] args) {

        try {
            API.initial();

            DBManager Manager=new DBManager("root","0123456789");
            //Manager.Ad_creatAd();
            Manager.Ad_AddTable("test1");
            Manager.Ad_CreatUser("user1","1111111111");
            //Manager.Grantinit("user1","test1");

            DBUser User=new DBUser("user1","1111111111");
            System.out.println("-----------------------------------");
            //授权测试
            //当管理员没有表的权限时
            Manager.Grant("user1","test2",1);
            Manager.Revoke("user1","test2",1);
            System.out.println("-----------------------------------");

            //Add
            User.Add_Authority("test1");
            Manager.Grant("user1","test1",1);
            User.Add_Authority("test1");
            Manager.Revoke("user1","test1",1);
            User.Add_Authority("test1");
            System.out.println("-----------------------------------");
            //Delete
            User.Delete_Authority("test1");
            Manager.Grant("user1","test1",2);
            User.Delete_Authority("test1");
            Manager.Revoke("user1","test1",2);
            User.Delete_Authority("test1");
            System.out.println("-----------------------------------");
            //update
            User.Update_Authority("test1");
            Manager.Grant("user1","test1",3);
            User.Update_Authority("test1");
            Manager.Revoke("user1","test1",3);
            User.Update_Authority("test1");
            System.out.println("-----------------------------------");
            //select
            User.Select_Authority("test1");
            Manager.Grant("user1","test1",4);
            User.Select_Authority("test1");
            Manager.Revoke("user1","test1",4);
            User.Select_Authority("test1");
            System.out.println("-----------------------------------");

            Manager.Ad_DropUser("user1");

            //API.store();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

/*

    public static void buffer_unit_test() {
        String buffer_test_file_name = "buffer_test";
        try {
            BufferManager m = new BufferManager();
            m.test_interface();
            int bid = m.read_block_from_disk(buffer_test_file_name, 15);
            buffer_print(m,bid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void buffer_print(BufferManager m, int bid) {
        System.out.println(bid);
        System.out.println("isLock = " + m.buffer[bid].lock());
        System.out.println("isDirty = " + m.buffer[bid].dirty());
        System.out.println("isValid = " + m.buffer[bid].valid());
        System.out.println(m.buffer[bid].read_integer(1200));
        System.out.println(m.buffer[bid].read_float(76));
        System.out.println(m.buffer[bid].read_string(492, 6));
        m.buffer[bid].write_integer(128, -23333);
        System.out.println("isLock = " + m.buffer[bid].lock());
        System.out.println("isDirty = " + m.buffer[bid].dirty());
        System.out.println("isValid = " + m.buffer[bid].valid());
        System.out.println("LRUCnt = " + m.buffer[bid].get_LRU());
        System.out.println(m.buffer[bid].read_integer(128));
    }

    private static void catalog_unit_test2() {
        try {
            CatalogManager.initial_catalog();
            CatalogManager.show_catalog();
            CatalogManager.store_catalog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void catalog_unit_test1() {
        try {
            CatalogManager.initial_catalog();
            Attribute tmpAttribute1 = new Attribute("id", NumType.valueOf("INT"), true);
            Attribute tmpAttribute2 = new Attribute("name", NumType.valueOf("CHAR"), 12, true);
            Attribute tmpAttribute3 = new Attribute("category", NumType.valueOf("CHAR"), 20, true);
            Vector<Attribute> tmpAttributeVector = new Vector<>();
            tmpAttributeVector.addElement(tmpAttribute1);
            tmpAttributeVector.addElement(tmpAttribute2);
            Table tmpTable1 = new Table("students", "id", tmpAttributeVector);
            CatalogManager.create_table(tmpTable1);
            CatalogManager.show_catalog();
            Index tmpIndex1 = new Index("idIndex", "students", "id");
            CatalogManager.create_index(tmpIndex1);
            CatalogManager.show_catalog();
            tmpAttributeVector.addElement(tmpAttribute3);
            Table tmpTable2 = new Table("book", "name", tmpAttributeVector);
            CatalogManager.create_table(tmpTable2);
            CatalogManager.show_catalog();
            //CatalogManager.drop_table("students");
            //CatalogManager.show_catalog();
            //CatalogManager.drop_index("idIndex");
            Index tmpIndex2 = new Index("categoryIndex", "book", "category");
            CatalogManager.create_index(tmpIndex2);
            CatalogManager.show_catalog();
            CatalogManager.store_catalog();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
