package org.ujorm.ujoservlet;

import com.sun.istack.internal.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.HtmlElement;
import org.ujorm.tools.XmlElement;
import static org.ujorm.tools.Check.hasLength;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
public class WelcomeFormServlet extends HttpServlet {

    /** Link to the source code */
    public static final String SOURCE_LINK = "https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/"
            + WelcomeFormServlet.class.getName().replace('.', '/') + ".java#L" + 28;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {

        final String title = "User form";
        final HtmlElement html = new HtmlElement(title);
        html.addCssLink("welcomeForm.css");
        final XmlElement form = html.getBody().addElement(Html.FORM);
        form.addElement(Html.H1).addText(title);
        final XmlElement table = form.addElement(Html.TABLE);
        for (Field field : getFieldDescription()) {
            final XmlElement row = table.addElement(Html.TR);
            row.addElement(Html.TD)
                    .addElement(Html.LABEL)
                    .addAttrib(Html.A_FOR, field.getName())
                    .addText(field.getLabelSeparated());
            row.addElement(Html.TD)
                    .addElement(Html.INPUT)
                    .addAttrib(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                    .addAttrib(Html.A_ID, field.getName())
                    .addAttrib(Html.A_NAME, field.getName())
                    .addAttrib(Html.A_VALUE, getParameter(input, field));
        }
        XmlElement footer = html.addElement(Html.DIV).addAttrib(Html.A_CLASS, "footer");
        footer.addTextWithSpace("The source of the")
                .addElement(Html.A)
                .addAttrib(Html.A_HREF, SOURCE_LINK)
                .addAttrib(Html.A_TARGET, Html.V_BLANK)
                .addText(getClass().getSimpleName());
        footer.addTextWithSpace("class.");

        // Render the result:
        html.toResponse(output, true);
    }

    /** Form field description data */
    private Field[] getFieldDescription() {
        Field[] reslt = { new Field("First name", "firstname")
                        , new Field("Last name", "lastname")
                        , new Field("E-mail", "email")
                        , new Field("Phone number", "phone")
                        , new Field("Nickname", "nick")
                        , new Field("", "submit", true)};
        return reslt;
    }

    /** Get parameter from request */
    @Nullable
    private String getParameter(HttpServletRequest input, Field field) {
        final String value = input.getParameter(field.getName());
        return hasLength(value)
                ? new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)
                : null;
    }

    /** Form field description class */
    static class Field {
        private final String label;
        private final String name;
        private final boolean submit;

        public Field(String label, String key) {
            this(label, key, false);
        }

        public Field(String label, String name, boolean submit) {
            this.label = label;
            this.name = name;
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
