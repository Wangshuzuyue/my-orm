package com.hzw.myorm;

import com.hzw.myorm.entity.User;
import com.hzw.myorm.repository.UserDao;

import java.util.Date;
import java.util.List;

/**
 * @Auther: huangzuwang
 * @Date: 2019/4/24 10:05
 * @Description:
 */

public class OrmTest {

    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        User user1 = new User();
        user1.setAge(26);
        List<User> list = userDao.select(user1);
        System.out.println(list);

        User user2 = new User();
        user2.setId(12);
        user2.setName("李四33");
        user2.setBirth("20010102");
        user2.setAge(28);
        user2.setUpdatedAt(new Date());
        user2.setCode("a1004");
        user2.setUserCode("user1004");
//        Integer insert = userDao.insert(user2);
//        System.out.println("insert: " + insert);


//        Integer update = userDao.update(user2);
//        System.out.println("update: " + update);

        Integer delete = userDao.delete(user2);
        System.out.println("delete: " + delete);
    }

}
