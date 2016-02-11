package org.version2.tools;

import org.ujorm.Ujo;

/**
 * Generic POJO to UJO converter
 * @author Pavel Ponec
 */
public class MyConverter<U extends Ujo> extends DefaultUjoConverter {

    public MyConverter() {
        super("generated", "$", "");
    }

}
