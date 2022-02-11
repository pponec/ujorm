module ujo.core {
    requires org.slf4j;
    requires org.jetbrains.annotations;
    requires ujo.tools;

    uses org.slf4j.spi.LoggerFactoryBinder;

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