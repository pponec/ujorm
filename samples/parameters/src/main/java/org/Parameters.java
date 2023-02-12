package org;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Locale;
import org.ujorm.Key;
import org.ujorm.implementation.quick.SmartUjo;

public class Parameters extends SmartUjo {

    /** The SysTray action on a second mouse click. */
    public enum Action {
        NONE, EVENT, HIDE
    }

    /** Localization */
    public static final Key<Parameters,Locale> P_LANG
        = newKey("Language", Locale.getDefault());

    /** Working Hours */
    public static final Key<Parameters,Float> P_WORKING_HOURS
        = newKey("WorkingHours", 8f);

    /** The First Day of the Week Day. */
    public static final Key<Parameters,Integer> P_FIRST_DAY_OF_WEEK
        = newKey("FirstDayOfWeek", Calendar.getInstance().getFirstDayOfWeek());

    /** Decimal time format. */
    public static final Key<Parameters,Boolean> P_DECIMAL_TIME_FORMAT
        = newKey("DecimalTimeFormat", true);

    /** The Main selecton format. */
    public static final Key<Parameters,String> P_DATE_MAIN_FORMAT
        = newKey("DateMainFormat", "EE, yyyy/MM/dd'  Week: 'ww");

    /** The Export Date Selection. */
    public static final Key<Parameters,String> P_DATE_REPORT_FORMAT
        = newKey("DateReportFormat", P_DATE_MAIN_FORMAT.getDefault() );

    /** The Export Date Selection. */
    public static final Key<Parameters,String> P_DATE_REPORT_FORMAT2
        = newKey("DateReportFormat2", "d'<br/><span class=\"smallMonth\">'MMMM'</span>'" );

    /** The complementary report CSS style. */
    public static final Key<Parameters,String> P_REPORT_CSS
        = newKey("ReportCSS", "styles/style.css" );

    /** The Goto Date format. */
    public static final Key<Parameters,String> P_DATE_GOTO_FORMAT
        = newKey("DateGotoFormat", "yyyy/MM/dd");

    /** A Color of a private project. */
    public static final Key<Parameters,Color> P_COLOR_PRIVATE
        = newKey("ColorOfPrivateProject", new Color(0x5DA158));

    /** A Color of finished project. */
    public static final Key<Parameters,Color> P_COLOR_FINISHED_PROJ
        = newKey("ColorOfFinishedProject", new Color(0xA9AC88));

    /** A Color of an editable area. */
    public static final Key<Parameters,Color> P_COLOR_EDITABLE
        = newKey("ColorOfEditableArea", new Color(0xFFFACD));

    /** Action on a second click */
    public static final Key<Parameters,Action> P_SYSTRAY_SECOND_CLICK
        = newKey("SystemTraySecondClick", Action.NONE);

    /** Modify value of finished project or task. */
    public static final Key<Parameters,Boolean> P_MODIFY_FINESHED_PROJ
        = newKey("ModifyFinishedProject", false);

    /** Create a new Event on an EXIT action. */
    public static final Key<Parameters,Boolean> P_EXIT_EVENT_CREATE
        = newKey("ExitEventCreating", Boolean.TRUE);

    /** Description of an EXIT action. */
    public static final Key<Parameters,String> P_EXIT_EVENT_DESCR
        = newKey("ExitEventDescription", "EXIT");

    /** Hide Button Icon. */
    public static final Key<Parameters,Boolean> P_HIDE_ICONS
        = newKey("HideButtonIcons", false);

    /** Last window size and position. */
    public static final Key<Parameters,Rectangle> P_WINDOW_SIZE
        = newKey("WindowSize", new Rectangle(0, 0, 622, 405));

    /** Restore a last application window size and position. */
    public static final Key<Parameters,Boolean> P_WINDOW_SIZE_RESTORATION
        = newKey("WindowSizeRestoration", true);
}