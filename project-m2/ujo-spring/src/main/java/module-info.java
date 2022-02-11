module ujo.spring {
    requires javax.servlet.api;
    requires aspectjweaver;
    requires org.jetbrains.annotations;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires spring.web;
    requires ujo.core;
    requires ujo.orm;
    requires ujo.tools;

    exports org.ujorm.orm.support;
    exports org.ujorm.spring;
}