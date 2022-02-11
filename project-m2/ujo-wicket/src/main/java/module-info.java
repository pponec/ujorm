module ujo.wicket {
    requires org.jetbrains.annotations;
    requires wicket.core;
    requires wicket.extensions;
    requires wicket.jquery.ui;
    requires wicket.jquery.ui.core;
    requires wicket.request;
    requires wicket.util;
    requires ujo.core;
    requires ujo.orm;
    requires ujo.tools;

    exports org.ujorm.wicket;
    exports org.ujorm.wicket.component.dialog.domestic;
    exports org.ujorm.wicket.component.form;
    exports org.ujorm.wicket.component.form.fields;
    exports org.ujorm.wicket.component.grid;
    exports org.ujorm.wicket.component.link;
    exports org.ujorm.wicket.component.tabs;
    exports org.ujorm.wicket.component.toolbar;
    exports org.ujorm.wicket.component.tools;
    exports org.ujorm.wicket.component.waiting;
}