package com.hzw.myorm.common.jdbc;

import java.util.List;

/**
 * @Auther: huangzuwang
 * @Date: 2019/4/24 09:41
 * @Description:
 */
public interface MyBaseDaoI<T> {

    List<T> select(T t);

    Integer insert(T t);

    Integer update(T t);

    Integer delete(T t);
}
