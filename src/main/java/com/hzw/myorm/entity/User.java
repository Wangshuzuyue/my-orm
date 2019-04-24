package com.hzw.myorm.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Auther: huangzuwang
 * @Date: 2019/4/20 20:15
 * @Description:
 */

@Table(name = "user")
@Data
public class User {

    @Id
    @Column(name = "id")
    Integer id;

    String birth;

    String name;

    Integer age;

    @Column(name = "updated_at")
    Date updatedAt;

    String code;

    @Column(name = "user_code")
    String userCode;
}
