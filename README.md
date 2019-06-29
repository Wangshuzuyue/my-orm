# My-ORM

#### 2019-04-24 手写 Mini版 ORM框架完成<br/>
代码路径：            

    |-- src
           |-- main
                  |-- java
                          |-- com.hzw.myorm.common.jdbc.MyBaseDao
                          


#### 2019-06-29 MyBatis体系结构与工作原理<br/>

时序图路径：

    |-- Mybatis作业
           |-- 时序图
                 

总结Mybatis设计模式<br/>
1.工厂模式：DefaultSqlSessionFactory。<br/>
2.构造器模式：MappedStatement.Builder,构造器内部类。<br/>
3.模板方法模式：BaseExecutor的doUpdate抽象方法，交由子类实现。<br/>
4.装饰器模式：CachingExecutor装饰SimpleExecutor增加缓存功能。<br/>
5.动态代理模式：MapperProxy代理Mapper接口。<br/>
6.委派模式：RoutingStatementHandler，根据语句类型，委派到不同的语句处理器(STATEMENT | PREPARED | CALLABLE)。<br/>
