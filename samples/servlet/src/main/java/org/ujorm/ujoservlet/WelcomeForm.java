/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujorm.ujoservlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.HtmlElement;
import org.ujorm.tools.XmlElement;
import static org.ujorm.ujoservlet.Html.*;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
public class WelcomeForm extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final String title = "User form";
        final Field[] fields = { new Field("First name", "firstname")
                               , new Field("Last name", "lastname")
                               , new Field("E-mail", "email")
                               , new Field("Phone number", "phone")
                               , new Field("Nickname", "nick")
                               , new Field("", "submit", true) };

        final HtmlElement html = new HtmlElement(title);
        html.addCssBody("h1{color:SteelBlue;} td:first-child{text-align:right;}");
        final XmlElement form = html.getBody().addElement(FORM);
        form.addElement(H1).addText(title);
        final XmlElement table = form.addElement(TABLE);
        for (Field field : fields) {
            final XmlElement row = table.addElement(TR);
            row.addElement(TD)
                    .addElement(LABEL)
                    .addAttrib(A_FOR, field.getName())
                    .addText(field.getLabel());
            row.addElement(TD)
                    .addElement(INPUT)
                    .addAttrib(A_ID, field.getName())
                    .addAttrib(A_NAME, field.getName())
                    .addAttrib(A_TYPE, field.isSubmit() ? V_SUBMIT : V_TEXT)
                    .addAttrib(A_VALUE, request.getParameter(field.getName()));
        }

        html.toResponse(response, true);
    }

    /** Form field description */
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

        public String getLabel() {
            return label + (submit || label.isEmpty() ? ' ' : ':');
        }

        public String getName() {
            return name;
        }

        public boolean isSubmit() {
            return submit;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
