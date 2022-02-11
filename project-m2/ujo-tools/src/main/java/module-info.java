module ujo.tools {
    requires org.jetbrains.annotations;
    requires java.sql;
    requires java.sql.rowset;

    exports org.ujorm.tools;
    exports org.ujorm.tools.common;
    exports org.ujorm.tools.jdbc;
    exports org.ujorm.tools.msg;
    exports org.ujorm.tools.set;
    exports org.ujorm.tools.xml;
    exports org.ujorm.tools.xml.builder;
    exports org.ujorm.tools.xml.config;
    exports org.ujorm.tools.xml.config.impl;
    exports org.ujorm.tools.xml.model;
}