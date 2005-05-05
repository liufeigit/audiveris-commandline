//-----------------------------------------------------------------------//
//                                                                       //
//                         D y n a m i c M e n u                         //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2005. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//
//      $Id$
package omr.ui;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Class <code>DynamicMenu</code> simplifies the definition of a menu, whose
 * content needs to be updated on-the-fly when the menu is being selected.
 */
public abstract class DynamicMenu
        extends JMenu
{
    //~ Constructors ---------------------------------------------------------

    //-------------//
    // DynamicMenu //
    //-------------//

    /**
     * Create the dynamic menu
     *
     * @param menuLabel the label to be used for the menu
     */
    public DynamicMenu (String menuLabel)
    {
        super(menuLabel);

        // Listener to menu selection, to modify content on-the-fly
        addMenuListener(new MenuListener()
        {
            public void menuCanceled (MenuEvent e)
            {
            }

            public void menuDeselected (MenuEvent e)
            {
            }

            public void menuSelected (MenuEvent e)
            {
                // Clean up the whole menu
                removeAll();

                // Rebuild the whole list of menu items on the fly
                buildItems();
            }
        });
    }

    //~ Methods --------------------------------------------------------------

    //------------//
    // buildItems //
    //------------//

    /**
     * This is the method that is called whenever the menu is selected, so
     * this is the method which must be implemented in a subclass.
     */
    protected abstract void buildItems ();
}
