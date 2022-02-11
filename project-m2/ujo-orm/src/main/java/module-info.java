module ujo.orm {
    requires org.jetbrains.annotations;
    requires ujo.core;
    requires ujo.tools;

    exports org.ujorm.core;
    exports org.ujorm.implementation.orm;
    exports org.ujorm.orm;
    exports org.ujorm.orm.annot;
    exports org.ujorm.orm.ao;
    exports org.ujorm.orm.dialect;
    exports org.ujorm.orm.metaModel;
    exports org.ujorm.orm.utility;
}