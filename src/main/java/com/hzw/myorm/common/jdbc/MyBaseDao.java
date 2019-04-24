package com.hzw.myorm.common.jdbc;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @Auther: huangzuwang
 * @Date: 2019/4/24 09:43
 * @Description:
 */
public class MyBaseDao<T> implements MyBaseDaoI<T> {

    String driverName = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://localhost:3306/hzwPlay?useUnicode=true&characterEncoding=utf8";
    String userName = "root";
    String password = "12345678";
    String tableName = null;
    Class clazz = null;

    /**
     * insert 用
     */
    String primaryKeyName = null;

    String columnNameString = null;

    String columnValueString = null;

    /**
     * update 用
     */
    String columnValueUpdateString = null;

    /**
     * key:数据库字段名  value:实体类字段名
     */
    Map<String, String> columnNameMap = new HashMap<>();

    /**
     * key:实体类字段名  value:数据库字段名
     */
    Map<String, String> fieldsMap = new HashMap<>();

    Connection conn = null;
    PreparedStatement pstm = null;
    ResultSet rs = null;
    Field[] fields = null;

    {
        try {
            //1.加载驱动类
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //获取泛型class
        clazz = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).
                getActualTypeArguments()[0];

        //表名
        Table table = (Table) clazz.getAnnotation(Table.class);
        tableName = table.name();
        StringBuffer columnNameSb = new StringBuffer();
        StringBuffer columnValueSb = new StringBuffer();
        StringBuffer columnValueUpdateSb = new StringBuffer();
        columnValueUpdateSb.append(" SET ");
        fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            //建立实体类字段名和数据库字段名相互映射
            String columnName;
            if (field.isAnnotationPresent(Column.class)) {
                columnName = field.getAnnotation(Column.class).name();
                columnNameMap.put(columnName, field.getName());
                fieldsMap.put(field.getName(), columnName);
                columnNameSb.append(columnName);
                columnValueUpdateSb.append(columnName + " = ?");
            } else {
                columnName = field.getName();
                columnNameMap.put(columnName, columnName);
                fieldsMap.put(columnName, columnName);
                columnNameSb.append(columnName);
                columnValueUpdateSb.append(columnName + " = ?");
            }
            if (field.isAnnotationPresent(Id.class)){
                primaryKeyName = columnName;
            }
            columnValueSb.append("?");
            if (i < fields.length - 1) {
                columnNameSb.append(",");
                columnValueSb.append(",");
                columnValueUpdateSb.append(",");
            }
        }
        columnNameString = columnNameSb.toString();
        columnValueString = columnValueSb.toString();
        columnValueUpdateString = columnValueUpdateSb.toString();
    }

    @Override
    public List<T> select(T t) {
        List<T> list = null;
        try {
            conn = getConn();
            //3.创建语句集
            StringBuffer sb = new StringBuffer();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(t);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (value != null) {
                    sb.append(" and " + fieldsMap.get(field.getName()) + " = '" + value + "'");
                }

            }
            String sql = "select * from " + tableName + " where 1 = 1 " + sb.toString();
            pstm = conn.prepareStatement(sql);
            //4.执行语句集
            rs = pstm.executeQuery();
            //MetaData 保存了处理真正数值以外的其他全部信息
            int columnCounts = rs.getMetaData().getColumnCount();
            //5.获取结果集
            list = new ArrayList<>();
            while (rs.next()) {
                T entity = (T) clazz.newInstance();
                for (int i = 1; i <= columnCounts; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Field field = null;
                    try {
                        field = clazz.getDeclaredField(columnNameMap.get(columnName));
                    } catch (NoSuchFieldException e) {
                        System.out.println("未找到属性：" + columnName);
                    }
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(entity, rs.getObject(columnName));
                    }
                }

                list.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }

        return list;
    }

    @Override
    public Integer insert(T t) {
        Integer insert = null;
        try {
            conn = getConn();
            String sql = String.format("insert into %s (%s) values(%s)", tableName, columnNameString, columnValueString);
            pstm = conn.prepareStatement(sql);
            setValueToStatement(t);
            insert = pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return insert;
    }

    @Override
    public Integer update(T t) {
        Integer update = null;
        try {
            conn = getConn();
            Object value = null;
            for(Field field : fields){
                if (field.getName().equals(primaryKeyName)){
                    value = field.get(t);
                }
            }
            String sql = String.format("update %s %s where %s = %s", tableName, columnValueUpdateString, primaryKeyName, value);
            pstm = conn.prepareStatement(sql);
            setValueToStatement(t);
            update = pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return update;
    }

    @Override
    public Integer delete(T t) {
        Integer delete = null;
        try {
            conn = getConn();
            Object value = null;
            for(Field field : fields){
                if (field.getName().equals(primaryKeyName)){
                    value = field.get(t);
                }
            }
            String sql = String.format("delete from %s where %s = %s", tableName, primaryKeyName, value);
            pstm = conn.prepareStatement(sql);
            delete = pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return delete;
    }

    private Connection getConn() throws SQLException {
        //2.建立连接
        return DriverManager.getConnection(url, userName, password);
    }

    private void setValueToStatement(T t) throws Exception {
        for(int i = 0; i < fields.length; i++){
            Field field = fields[i];
            field.setAccessible(true);
            Object value = field.get(t);
            Class<?> classType = field.getType();
            int index = i + 1;
            if (classType == String.class){
                if (value == null){
                    pstm.setNull(index, Types.VARBINARY);
                }else{
                    pstm.setString(index, (String) value);
                }
            }else if (classType == Integer.class){
                if (value == null){
                    pstm.setNull(index, Types.INTEGER);
                }else{
                    pstm.setInt(index, (Integer) value);
                }
            }else if (classType == java.util.Date.class){
                if (value == null){
                    pstm.setNull(index, Types.DATE);
                }else{
                    long time = ((java.util.Date) value).getTime();
                    pstm.setDate(index, new Date(time));
                }
            }else if (classType == Double.class){
                if (value == null){
                    pstm.setNull(index, Types.DOUBLE);
                }else{
                    pstm.setDouble(index, (Double) value);
                }
            }else if (classType == Boolean.class){
                if (value == null){
                    pstm.setNull(index, Types.BOOLEAN);
                }else{
                    pstm.setBoolean(index, (Boolean) value);
                }
            }
        }




    }

    private void closeResources() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
