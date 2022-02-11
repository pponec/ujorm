module ujo.web {
    requires javax.servlet.api;
    requires org.jetbrains.annotations;
    requires ujo.tools;

    exports org.ujorm.tools.web;
    exports org.ujorm.tools.web.ajax;
    exports org.ujorm.tools.web.ao;
    exports org.ujorm.tools.web.json;
    exports org.ujorm.tools.web.report;
    exports org.ujorm.tools.web.table;
}