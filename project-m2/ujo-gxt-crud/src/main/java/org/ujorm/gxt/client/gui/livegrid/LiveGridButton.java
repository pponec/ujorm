package org.ujorm.gxt.client.gui.livegrid;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.ujorm.gxt.client.AbstractCujo;

public interface LiveGridButton<CUJO extends AbstractCujo> {

    /** PĹ™ipravĂ­ tlaÄŤĂ­tka k pouĹľitĂ­ (vÄŤetnÄ› napojenĂ­ listenerĹŻ). */
    public abstract void initButtons();

    /** VytvoĹ™Ă­ listener pro pro nĂˇvrat zpÄ›t bez vĂ˝stupu. */
    public abstract SelectionListener<ButtonEvent> buttonActionBack();

    /** VytvoĹ™Ă­ listener pro odstranÄ›nĂ­ poloĹľky */
    public abstract SelectionListener<ButtonEvent> buttonActionDelete();

    /** VytvoĹ™Ă­ listener pro editaci poloĹľky */
    public abstract SelectionListener<ButtonEvent> buttonActionUpdate();

    /** VytvoĹ™Ă­ listener pro vytvoĹ™enĂ­ novĂ© poloĹľky */
    public abstract SelectionListener<ButtonEvent> buttonActionCreate();

    /** VytvoĹ™Ă­ listener pro dohledĂˇnĂ­ hodnoty pro danou poloĹľku. */
    public abstract SelectionListener<ButtonEvent> buttonActionSelect();

    /** VytvoĹ™Ă­ listener pro znovunaÄŤtenĂ­ obsahu tabulky. */
    public abstract SelectionListener<ButtonEvent> buttonActionReload();
}
