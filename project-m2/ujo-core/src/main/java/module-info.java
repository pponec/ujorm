module ujo.core {
    //requires hibernate.core;
    requires org.jetbrains.annotations;
    //requires slf4j.api;
    //requires ujo.tools;

    exports org.ujorm;
    exports org.ujorm.core;
    exports org.ujorm.core.annot;
    exports org.ujorm.core.enums;
    exports org.ujorm.criterion;
    exports org.ujorm.extensions;
    exports org.ujorm.extensions.types;
    exports org.ujorm.implementation.map;
    exports org.ujorm.implementation.quick;
    exports org.ujorm.logger;
    exports org.ujorm.swing;
    exports org.ujorm.validator;
    exports org.ujorm.validator.impl;
}