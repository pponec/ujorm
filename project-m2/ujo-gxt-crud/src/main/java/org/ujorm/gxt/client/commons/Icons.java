/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.commons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

public interface Icons extends ImageBundle {

    public static final Icons Pool = GWT.create(Icons.class);

  @Resource("org/ujorm/gxt/staticweb/resources/icons/table.png")
  AbstractImagePrototype table();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/table_go.png")
  AbstractImagePrototype select();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/add.png")
  AbstractImagePrototype add();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/delete2.png")
  AbstractImagePrototype delete();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/application_edit.png")
  AbstractImagePrototype edit();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/application_go.png")
  AbstractImagePrototype detail();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/table_add.png")
  AbstractImagePrototype list();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/door_out.png")
  AbstractImagePrototype goBack();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/help.png")
  AbstractImagePrototype help();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/bullet_error.png")
  AbstractImagePrototype error();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/application_view_tile.png")
  AbstractImagePrototype selectionDialog();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/tick.png")
  AbstractImagePrototype ok();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/house.png")
  AbstractImagePrototype home();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_report.png")
  AbstractImagePrototype report();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/lightning_add.png") // _application_double.png")
  AbstractImagePrototype copy();

  /** Hot Task button */
  @Resource("org/ujorm/gxt/staticweb/resources/icons/note_add.png")
  AbstractImagePrototype hotTask();

  /** Hot Task button pro a private actions */
  @Resource("org/ujorm/gxt/staticweb/resources/icons/note_add_private.png")
  AbstractImagePrototype hotTaskPrivate();

  // ------ MAIN TABS

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_application_view_list.png")
  AbstractImagePrototype event();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_application_double.png")
  AbstractImagePrototype task();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_application_double.png")
  AbstractImagePrototype project();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/application_edit.png")
  AbstractImagePrototype product();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/flag_green.png")
  AbstractImagePrototype account();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/user_add.png")
  AbstractImagePrototype user();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_wrench.png")
  AbstractImagePrototype params();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_resultset_next.png")
  AbstractImagePrototype goNext();

  @Resource("org/ujorm/gxt/staticweb/resources/icons/_resultset_previous.png")
  AbstractImagePrototype goPrev();


}
