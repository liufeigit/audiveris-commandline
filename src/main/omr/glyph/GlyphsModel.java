//----------------------------------------------------------------------------//
//                                                                            //
//                           G l y p h s M o d e l                            //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Hervé Bitteur 2000-2011. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph;

import omr.Main;

import omr.glyph.facets.BasicGlyph;
import omr.glyph.facets.Glyph;

import omr.lag.Section;

import omr.log.Logger;

import omr.score.ui.ScoreActions;

import omr.sheet.Sheet;
import omr.sheet.SystemInfo;

import omr.step.Step;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class <code>GlyphsModel</code> is a common model for synchronous glyph
 * and section handling.
 *
 * <p>Nota: User gesture should trigger actions in GlyphsController which will
 * asynchronously delegate to this model.
 *
 * @author Hervé Bitteur
 */
public class GlyphsModel
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(GlyphsModel.class);

    //~ Instance fields --------------------------------------------------------

    /** Underlying glyph nest */
    protected final Nest nest;

    /** Related Sheet */
    protected final Sheet sheet;

    /** Related Step */
    protected final Step step;

    /** Latest shape assigned if any */
    protected Shape latestShape;

    //~ Constructors -----------------------------------------------------------

    //-------------//
    // GlyphsModel //
    //-------------//
    /**
     * Create an instance of GlyphsModel, with its underlying glyph lag
     *
     * @param sheet the related sheet (can be null)
     * @param nest the related nest (cannot be null)
     * @param step the step after which update should be perform (can be null)
     */
    public GlyphsModel (Sheet sheet,
                        Nest  nest,
                        Step  step)
    {
        // Null sheet is allowed (for GlyphVerifier use)
        this.sheet = sheet;

        if (nest == null) {
            throw new IllegalArgumentException(
                "Attempt to create a GlyphsModel with null underlying nest");
        } else {
            this.nest = nest;
        }

        this.step = step;
    }

    //~ Methods ----------------------------------------------------------------

    //--------------//
    // getGlyphById //
    //--------------//
    /**
     * Retrieve a glyph, knowing its id
     *
     * @param id the glyph id
     * @return the glyph found, or null if not
     */
    public Glyph getGlyphById (int id)
    {
        return nest.getGlyph(id);
    }

    //----------------//
    // setLatestShape //
    //----------------//
    /**
     * Assign the latest useful shape
     *
     * @param shape the current / latest shape
     */
    public void setLatestShape (Shape shape)
    {
        if (shape != Shape.GLYPH_PART) {
            latestShape = shape;
        }
    }

    //----------------//
    // getLatestShape //
    //----------------//
    /**
     * Report the latest non null shape that was assigned, or null if none
     *
     * @return latest shape assigned, or null if none
     */
    public Shape getLatestShape ()
    {
        return latestShape;
    }

    //---------//
    // getNest //
    //---------//
    /**
     * Report the underlying glyph nest
     * @return the related glyph nest
     */
    public Nest getNest ()
    {
        return nest;
    }

    //----------------//
    // getRelatedStep //
    //----------------//
    /**
     * Report the step this GlyphsModel is used for, so that we know from which
     * step updates must be propagated (we have to update the steps that follow
     * this one)
     * @return the step related to this glyphs model
     */
    public Step getRelatedStep ()
    {
        return step;
    }

    //----------//
    // getSheet //
    //----------//
    /**
     * Report the model underlying sheet
     * @return the underlying sheet instance
     */
    public Sheet getSheet ()
    {
        return sheet;
    }

    //--------------//
    // assignGlyphs //
    //--------------//
    /**
     * Assign a shape to the selected collection of glyphs.
     *
     * @param glyphs the collection of glyphs to be assigned
     * @param shape the shape to be assigned
     * @param compound flag to build one compound, rather than assign each
     *                 individual glyph
     * @param doubt the doubt we have wrt the assigned shape
     */
    public void assignGlyphs (Collection<Glyph> glyphs,
                              Shape             shape,
                              boolean           compound,
                              double            doubt)
    {
        if (compound) {
            // Build & insert one compound
            Glyph      glyph = null;

            SystemInfo system = sheet.getSystemOf(glyphs);

            if (system != null) {
                glyph = system.buildTransientCompound(glyphs);
            } else {
                glyph = new BasicGlyph(sheet.getScale().interline());

                for (Glyph g : glyphs) {
                    glyph.addGlyphSections(g, Glyph.Linking.NO_LINK_BACK);

                    if (glyph.getNest() == null) {
                        glyph.setNest(g.getNest());
                    }
                }

                // Register (a copy of) the parts in the compound itself
                glyph.setParts(glyphs);
            }

            assignGlyph(glyph, shape, doubt);
        } else {
            // Assign each glyph individually
            for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
                if (glyph.getShape() != Shape.NOISE) {
                    assignGlyph(glyph, shape, doubt);
                }
            }
        }
    }

    //----------------//
    // assignSections //
    //----------------//
    /**
     * Assign a shape to the selected collection of sections.
     *
     * @param sections the collection of sections to be aggregated as a glyph
     * @param shape the shape to be assigned
     * @param doubt the doubt we have wrt the assigned shape
     * @return the newly built glyph
     */
    public Glyph assignSections (Collection<Section> sections,
                                 Shape               shape,
                                 double              doubt)
    {
        // Build & insert one glyph out of the sections
        SystemInfo system = sections.iterator()
                                    .next()
                                    .getSystem();
        Glyph      glyph = system.buildGlyph(sections);

        return assignGlyph(glyph, shape, doubt);
    }

    //----------------//
    // deassignGlyphs //
    //----------------//
    /**
     * De-Assign a collection of glyphs.
     *
     * @param glyphs the collection of glyphs to be de-assigned
     */
    public void deassignGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deassignGlyph(glyph);
        }
    }

    //--------------//
    // deleteGlyphs //
    //--------------//
    public void deleteGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deleteGlyph(glyph);
        }
    }

    //-------------//
    // assignGlyph //
    //-------------//
    /**
     * Assign a Shape to a glyph, inserting the glyph to its containing system
     * and lag if it is still transient
     *
     * @param glyph the glyph to be assigned
     * @param shape the assigned shape, which may be null
     * @param doubt the doubt about shape
     * @return the assigned glyph (perhaps an original glyph)
     */
    protected Glyph assignGlyph (Glyph  glyph,
                                 Shape  shape,
                                 double doubt)
    {
        if (glyph == null) {
            return null;
        }

        if (shape != null) {
            SystemInfo system = sheet.getSystemOf(glyph);

            if (system != null) {
                glyph = system.addGlyph(glyph); // System then nest
            } else {
                // Insert in nest directly, which assigns an id to the glyph
                glyph = nest.addGlyph(glyph);
            }

            boolean isTransient = glyph.isTransient();

            if (logger.isFineEnabled()) {
                logger.fine(
                    "Assign " + (isTransient ? "compound " : "") + "glyph#" +
                    glyph.getId() + " to " + shape);
            }

            // Remember the latest shape assigned
            setLatestShape(shape);
        }

        // Do the assignment of the shape to the glyph
        glyph.setShape(shape, doubt);

        // Should we persist the assigned glyph?
        if ((shape != null) &&
            (doubt == Evaluation.MANUAL) &&
            (Main.getGui() != null) &&
            ScoreActions.getInstance()
                        .isManualPersisted()) {
            // TODO: We should also record the glyph into the training data for
            // this sheet, in order not to lose this information.
            GlyphChecker.getInstance()
                        .relax(shape, glyph, GlyphEvaluator.feedInput(glyph));
        }

        return glyph;
    }

    //---------------//
    // deassignGlyph //
    //---------------//
    /**
     * Deassign the shape of a glyph
     *
     * @param glyph the glyph to deassign
     */
    protected void deassignGlyph (Glyph glyph)
    {
        // Assign the null shape to the glyph
        assignGlyph(glyph, null, Evaluation.ALGORITHM);
    }

    //-------------//
    // deleteGlyph //
    //-------------//
    protected void deleteGlyph (Glyph glyph)
    {
        if (glyph == null) {
            return;
        }

        if (!glyph.isVirtual()) {
            logger.warning(
                "Attempt to delete non-virtual glyph#" + glyph.getId());

            return;
        }

        SystemInfo system = sheet.getSystemOf(glyph.getAreaCenter());

        if (system != null) {
            system.removeGlyph(glyph);
        }

        nest.removeVirtualGlyph((VirtualGlyph) glyph);
    }
}
