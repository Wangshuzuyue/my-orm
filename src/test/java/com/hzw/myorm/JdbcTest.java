package com.hzw.myorm;

import com.hzw.myorm.entity.User;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @Auther: huangzuwang
 * @Date: 2019/4/20 20:07
 * @Description:
 */
public class JdbcTest {

    public static void main(String[] args) {
        User condition = new User();
        condition.setAge(26);
        condition.setName("张三2");
        List<?> result = select(condition);
        System.out.println("结果：" + result);
    }

    private static List<?> select(Object condition){
        Class<?> entityClass = condition.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        List<Object> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            //1.加载驱动类
            Class.forName("com.mysql.jdbc.Driver");

            //2.建立连接
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hzwPlay?useUnicode=true&characterEncoding=utf8", "root", "12345678");
            //3.创建语句集

            Table table = entityClass.getAnnotation(Table.class);

            //key:数据库字段名  value:实体类字段名
            Map<String,String> columnNameMap = new HashMap<>();

            //key:实体类字段名  value:数据库字段名
            Map<String,String> fieldsMap = new HashMap<>();

            StringBuffer sb = new StringBuffer();
            for (Field field :  fields){
                if (field.isAnnotationPresent(Column.class)){
                    columnNameMap.put(field.getAnnotation(Column.class).name(), field.getName());
                    fieldsMap.put(field.getName(), field.getAnnotation(Column.class).name());
                }else{
                    columnNameMap.put(field.getName(), field.getName());
                    fieldsMap.put(field.getName(), field.getName());
                }
                field.setAccessible(true);
                Object value = field.get(condition);
                if (value != null){
                    sb.append(" and " + fieldsMap.get(field.getName()) + " = '" + value + "'");
                }
            }



            String sql = "select * from " + table.name() +" where 1 = 1 " + sb.toString();
            pstm = conn.prepareStatement(sql);
            //4.执行语句集
            rs = pstm.executeQuery();
            //MetaData 保存了处理真正数值以外的其他全部信息
            int columnCounts = rs.getMetaData().getColumnCount();
            //5.获取结果集
            while (rs.next()){
                Object entity = entityClass.newInstance();
                for(int i = 1; i <= columnCounts; i++){
                    String columnName = rs.getMetaData().getColumnName(i);
                    Field field = null;
                    try {
                        field = entityClass.getDeclaredField(columnNameMap.get(columnName));
                    }catch (NoSuchFieldException e){
                        System.out.println("未找到属性：" + columnName);
                    }
                    if (field != null){
                        field.setAccessible(true);
                        field.set(entity, rs.getObject(columnName));
                    }
                }

                list.add(entity);
            }

            //6.关闭结果集、关闭语句集、关闭连接
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (conn != null){
                    conn.close();
                }
                if (pstm != null){
                    pstm.close();
                }
                if (rs != null){
                    rs.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return list;
    }
}
