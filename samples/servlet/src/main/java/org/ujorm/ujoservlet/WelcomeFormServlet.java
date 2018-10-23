package org.ujorm.ujoservlet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.HtmlElement;
import org.ujorm.tools.XmlElement;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
public class WelcomeFormServlet extends HttpServlet {

    /** A HTML code page */
    private static final Charset CODE_PAGE = StandardCharsets.UTF_8; // Charset.forName("windows-1250");

    /** Link to the source code */
    public static final String SOURCE_LINK = "https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/"
            + WelcomeFormServlet.class.getName().replace('.', '/') + ".java#L" + 36;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        input.setCharacterEncoding(CODE_PAGE.toString());
        final String title = "Simple user form";
        final HtmlElement html = new HtmlElement(title, CODE_PAGE);
        html.addCssLink("welcomeForm.css");
        final XmlElement form = html.getBody().addElement(Html.FORM)
                .addAttrib(Html.A_METHOD, Html.V_POST)
                .addAttrib(Html.A_ACTION, null /* input.getRequestURI() */);
        form.addElement(Html.H1).addText(title);
        final XmlElement table = form.addElement(Html.TABLE);
        for (Field field : getFieldDescription()) {
            final XmlElement row = table.addElement(Html.TR);
            row.addElement(Html.TD)
                    .addElement(Html.LABEL)
                    .addAttrib(Html.A_FOR, field.getName())
                    .addText(field.getLabelSeparated());
            XmlElement valueCell = row.addElement(Html.TD);
            valueCell.addElement(Html.INPUT)
                    .addAttrib(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                    .addAttrib(Html.A_ID, field.getName())
                    .addAttrib(Html.A_NAME, field.getName())
                    .addAttrib(Html.A_VALUE, input.getParameter(field.getName()));
            field.getErrorMessage(input).ifPresent(msg -> valueCell.addElement(Html.DIV)
                    .addAttrib(Html.A_CLASS, "error")
                    .addText(msg)); // A validation message
        }
        XmlElement footer = html.getBody().addElement(Html.DIV)
                .addAttrib(Html.A_CLASS, "footer");
        footer.addTextWithSpace("See a ")
                .addElement(Html.A)
                .addAttrib(Html.A_HREF, SOURCE_LINK)
                .addAttrib(Html.A_TARGET, Html.V_BLANK)
                .addText(getClass().getSimpleName());
        footer.addTextWithSpace("class of the Ujorm framework.");
        html.toResponse(output, true); // Render the result
    }

    /** Form field description data */
    private Field[] getFieldDescription() {
        Field[] reslt = { new Field("First name", "firstname", "^.{2,99}$")
                        , new Field("Last name", "lastname", "^.{2,99}$")
                        , new Field("E-mail", "email", "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$")
                        , new Field("Phone number", "phone", "^\\+?[ \\d]{5,15}$")
                        , new Field("Nickname", "nick", "^.{3,10}$")
                        , new Field("", "submit", "", true)};
        return reslt;
    }

    /** Form field description class */
    static class Field {
        private final String label;
        private final String name;
        private final String regexp;
        private final boolean submit;

        public Field(String label, String key, String regexp) {
            this(label, key, regexp, false);
        }

        public Field(String label, String name, String regexp, boolean submit) {
            this.label = label;
            this.name = name;
            this.regexp = regexp;
            this.submit = submit;
        }

        public String getLabelSeparated() {
            char separator = submit || label.isEmpty() ? ' ' : ':';
            return label + separator;
        }

        public String getName() {
            return name;
        }

        public boolean isSubmit() {
            return submit;
        }

        /** Check the POST value and regurn an error message */
        public Optional<String> getErrorMessage(HttpServletRequest input) {
            if (Html.V_POST.equalsIgnoreCase(input.getMethod())) {
                final String value = input.getParameter(name);
                if (Check.isEmpty(value)) {
                    return Optional.of("Required field");
                }
                if (Check.hasLength(regexp) && !Pattern.matches(regexp, value)) {
                     return Optional.of("Wrong value for: " + regexp); // localiza it!
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        processRequest(input, output);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        processRequest(input, output);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return getClass().getSimpleName();
    }
}
