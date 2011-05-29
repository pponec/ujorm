/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.server;

import org.ujorm.gxt.client.Cujo;
import org.ujorm.Ujo;

/**
 * UjoTranslator CallBack
 * @author Pavel Ponec
 */
public interface UjoTranslatorCallback<CUJO extends Cujo> {

    /** Copy Ujo to CUJO */
    public void copy(Ujo ujo, CUJO cujo);

}
