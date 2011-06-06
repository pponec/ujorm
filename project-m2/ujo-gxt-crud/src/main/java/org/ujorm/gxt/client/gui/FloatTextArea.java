package org.ujorm.gxt.client.gui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextArea;

public class FloatTextArea extends TextArea {

    public static final int KEY_ENTER = 13;
    public static final int KEY_BACKSPACE = 8;
    public static final int KEY_DELETE = 46;
    public static final String FONT_SIZE = "font-size";
    // vychozi/minimalni vyska
    private int initHeight;
    // sirka pro pismeno
    private double charWidth;
    // vyska pro pismeno
    private double charHeight;
    private boolean previousScroll = false;
    private int previousW = 0;
    private int previousH = 0;

    /**
     * Vytvori plovouci TextArea.
     * Optimalni vysku a sirku znaku si sam spocita.
     */
    public FloatTextArea() {
            this(0, 0);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (height != previousH || width != previousW) {
            previousH = height;
            previousW = width;
            this.fireEvent(Events.KeyDown);
//            refreshParent(height);
        }
    }

    @Override
    public void afterRender() {
        super.afterRender();
        this.fireEvent(Events.KeyDown);
    }

    /**
     * Vytvori plovouci TextArea, ktera dynamicky zvetsuje svou velikost.
     * Pokud neni znama presna vyska a sirka znaku, tak pouzit prazdny konstruktor!!!
     *
     * @param w presna sirka znaku
     * @param h presna vyska znaku
     */
    public FloatTextArea(double w, double h) {
        charWidth = w;
        charHeight = h;

        addKeyListener(new FloatAreaKeyListener());
    }

    public FloatTextArea(double w, double h, LayoutContainer parent, int parentMargin) {
        this(w, h, true);
    }

    public FloatTextArea(double w, double h, boolean editable) {
        charWidth = w;
        charHeight = h;

        if (editable) {
            addKeyListener(new FloatAreaKeyListener());
        }
    }

    /**
     * Nastaveni vychozi (minimalni) vysky
     *
     * @param height vyska v px
     */
    public void setMinHeight(int height) {
        super.setHeight(height);
        initHeight = height;
    }

    /**
     * Nastaveni vysky pisma
     *
     * @param charHeight vyska v px
     */
    public void setCharHeight(double charHeight) {
        this.charHeight = charHeight;
    }

    /**
     * Nastaveni velikosti pro kazde pismeno
     *
     * @param charWidth sirka v px
     */
    public void setCharWidth(double charWidth) {
        this.charWidth = charWidth;
    }

    protected void refreshParent(int height) {
        // TODO:
    }

    /**
     * KeyListener zastitujici veskerou logiku souvisejici s dynamickou
     * zmenou velikosti
     */
    private class FloatAreaKeyListener extends KeyListener {

        @Override
        public void componentKeyDown(ComponentEvent event) {
            int fontPx = 12;

            if (charWidth < 0.1 || charHeight < 0.1) {
                recalcuteCharDimensions(fontPx);
            }

            int charCount = (int) (getWidth() / charWidth);
            int key = event.getKeyCode();
            int size = 0;

            // hodnota v textboxu - NEobsahuje prave stisknuty znak
            String value = getValue();
            if (value == null) {
                value = "";
            }

            // zvyseni vysky pri stisku Enter
            if (key == KEY_ENTER) {
                size++;

                // Backspace - smazani znaku pred pozici kurzoru
            } else if (key == KEY_BACKSPACE) {
                value = onKeyBackspace(value);

                // Delete - smazani znaku za pozici kurzoru
            } else if (key == KEY_DELETE) {
                value = onKeyDelete(value);

                // pridani normalniho znaku
            } else {
                if (Character.isLetterOrDigit((char) key)) {
                    value = value.concat(String.valueOf((char) key));
                }
            }

            // rozdeleni po radcich a vypocet poctu radku
            value = value.replaceAll("\r", "");
            String elems[] = value.split("\n");
            for (int i = 0; i < elems.length; i++) {
                size += (elems[i].length() / charCount);
            }
            size += value.replaceAll("[^\\n]", "").length();

            // osetreni pro pripad, ze textArea pretekl
            if (getInputEl().getScrollTop() > 0 || previousScroll) {
                previousScroll = true;
                size++;
            } else {
                previousScroll = false;
            }

            // nastaveni vysky
            int calculateHeight = charHeight * (size + 1) < initHeight ? initHeight : (int) (charHeight * (size + 2));
            if (getHeight() != calculateHeight) {
                setHeight(calculateHeight);
                refreshParent(calculateHeight);
            }
        }

        /**
         * Ze vstupniho retezce odstrani nechtenou sekvenci
         */
        private String deleteSelectedText(String value, String selected) {
            int start = value.indexOf(selected);
            int end = start + selected.length();
            String firstPart = value.substring(0, start);
            String secondPart = value.substring(end, value.length());
            value = firstPart + secondPart;
            return value;
        }

        /**
         * Akce pri stisku delete klavesy
         */
        private String onKeyDelete(String value) {
            int cursor = getCursorPos();
            if (cursor < value.length()) {
                String selected = getSelectedText();

                // neni oznaceny text
                if (selected.isEmpty()) {
                    String firstPart = value.substring(0, cursor);
                    String secondPart = value.substring(cursor + 1);
                    value = firstPart + secondPart;

                    // je oznaceny text
                } else {
                    value = deleteSelectedText(value, selected);
                }
            }
            return value;
        }

        /**
         * Akce pri stisku backspace klavesy
         */
        private String onKeyBackspace(String value) {
            int cursor = getCursorPos();
            if (cursor > 0) {
                String selected = getSelectedText();

                // neni oznaceny text
                if (selected.isEmpty()) {
                    String firstPart = value.substring(0, cursor - 1);
                    String secondPart = value.substring(cursor);
                    value = firstPart + secondPart;

                    // je oznaceny text
                } else {
                    value = deleteSelectedText(value, selected);
                }
            }
            return value;
        }

        /**
         * Prepocitani velikosti znaku
         */
        private void recalcuteCharDimensions(int fontPx) throws NumberFormatException {
            // urceni vysky fontu a prevod na px
            String fontSize = getInputEl().getStyleAttribute("font-size");
            if (fontSize.toLowerCase().endsWith("pt")) {
                fontPx = (int) (Double.valueOf(fontSize.replaceAll("[^\\d]", "")) * (4.0 / 3));
            } else if (fontSize.toLowerCase().endsWith("px")) {
                fontPx = Integer.valueOf(fontSize.replaceAll("[^\\d]", ""));
            }
            // zjisteni potrebne vysky a sirky
            if (charWidth < 0.1) {
                charWidth = fontPx * 0.45;
            }
            if (charHeight < 0.1) {
                charHeight = fontPx * 1.17;
            }
        }
    }
}
