//-----------------------------------------------------------------------//
//                                                                       //
//                           S t i c k U t i l                           //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2005. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//
//      $Id$
package omr.stick;

import omr.check.FailureResult;
import omr.constant.Constant;
import omr.constant.ConstantSet;
import omr.glyph.GlyphLag;
import omr.glyph.GlyphLagView;
import omr.glyph.GlyphSection;
import omr.graph.DigraphView;
import omr.lag.Lag;
import omr.lag.LagView;
import omr.lag.Run;
import omr.math.Line;
import omr.math.VerticalLine;
import omr.sheet.Picture;
import omr.util.Logger;
import omr.ui.Zoom;

import java.util.ArrayList;
import java.util.List;
import omr.lag.Section;

/**
 * Class <code>StickUtil</code> gathers static utilities for sticks.
 */
public class StickUtil
{
    //~ Static variables/initializers ----------------------------------------

    private static final Constants constants = new Constants();
    private static final Logger logger = Logger.getLogger(StickUtil.class);

    //~ Methods --------------------------------------------------------------

    //---------------//
    // areExtensions //
    //---------------//

    /**
     * Checks whether two sticks can be considered as extensions of the other
     * one.  Due to some missing points, a long stick can be broken into
     * several smaller ones, that we must check for this.  This is checked
     * before actually merging them.
     *
     * @param foo           one stick
     * @param bar           one other stick
     * @param maxDeltaCoord Max gap in coordinate (x for horizontal)
     * @param maxDeltaPos   Max gap in position (y for horizontal)
     * @param maxDeltaSlope Max difference in slope
     *
     * @return The result of the test
     */
    public static boolean areExtensions (Stick foo,
                                         Stick bar,
                                         int maxDeltaCoord,
                                         // X for horizontal
                                         int maxDeltaPos,
                                         // Y for horizontal
                                         double maxDeltaSlope)
    {
        // Check that a pair of start/stop is compatible
        if ((Math.abs(foo.getStart() - bar.getStop()) <= maxDeltaCoord)
            || (Math.abs(foo.getStop() - bar.getStart()) <= maxDeltaCoord)) {
            // Check that a pair of positions is compatible
            if ((Math.abs(foo.getLine().yAt(foo.getStart())
                          - bar.getLine().yAt(foo.getStop())) <= maxDeltaPos)
                || (Math.abs(foo.getLine().yAt(foo.getStop())
                             - bar.getLine().yAt(foo.getStart())) <= maxDeltaPos)) {
                // Check that slopes are compatible (a useless test ?)
                if (Math.abs(foo.getLine().getSlope()
                             - bar.getLine().getSlope()) <= maxDeltaSlope) {
                    return true;
                } else if (logger.isDebugEnabled()) {
                    logger.debug("isExtensionOf:  Incompatible slopes");
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("isExtensionOf:  Incompatible positions");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("isExtensionOf:  Incompatible coordinates");
        }

        return false;
    }

    //---------//
    // cleanup //
    //---------//

    /**
     * When a stick is logically removed, the crossing objects must be
     * extended through the former stick.
     *
     * @param minPointNb Minimum number of points, across the stick, to be
     *                   able to compute an extension axis. Otherwise, the
     *                   extension is performed orthogonally to the stick.
     * @param picture    the picture which hosts the pixels handled by the
     *                   stick
     */
    public static void cleanup (Stick stick,
                                GlyphLag lag,
                                int minPointNb,
                                Picture picture)
    {
        List<GlyphSection> members = stick.getMembers();
        List<GlyphSection> borders = new ArrayList<GlyphSection>();
        List<GlyphSection> patches = new ArrayList<GlyphSection>();

        // Extend crossing objects
        for (GlyphSection s : members) {
            StickSection section = (StickSection) s;
            // Extend crossing vertices before and after
            for (GlyphSection source : section.getSources()) {
                cleanupSection(stick, borders, patches, lag, picture,
                               minPointNb, section, (StickSection) source,
                               +1, true);
            }
            for (GlyphSection target : section.getTargets()) {
                cleanupSection(stick, borders, patches, lag, picture,
                               minPointNb, section, (StickSection) target,
                               -1, true);
            }

            // Delete the section itself
            section.delete();
        }

        //logger.debug("cleanup. " + members.size() + " members " + borders.size() + " borders " + stick);
        // Include the border sections as line members
        members.addAll(borders);

        // Extend crossing objects for borders
        for (GlyphSection s : borders) {
            StickSection section = (StickSection) s;
            // Extend crossing vertices before and after
            for (GlyphSection source : section.getSources()) {
                cleanupSection(stick, borders, patches, lag, picture,
                               minPointNb, section, (StickSection) source,
                               +1, false);
            }
            for (GlyphSection target : section.getTargets()) {
                cleanupSection(stick, borders, patches, lag, picture,
                               minPointNb, section, (StickSection) target,
                               -1, false);
            }

            // Delete the section itself
            section.delete();
        }

        // Erase pixels from members
        write(members, picture, Picture.BACKGROUND);

        // Write patches to the picture
        write(patches, picture, constants.patchGreyLevel.getValue());

        // Get rid of cached data
        // TBD TBD TBD
    }

    //----------------//
    // cleanupSection //
    //----------------//

    /**
     * Cleanup one line section, by extending potential crossing objects in a
     * certain direction. During this operation, we may consider that tangent
     * vertices are in fact borders that we should include in the line, rather
     * than consider them as real crossing objects.
     *
     * @param picture       the picture whose pixels must be modified
     * @param lineSection   the section to clean up
     * @param sct           a potentially crossing section
     * @param direction     in which direction we extend objects
     * @param borderEnabled do we consider adding borders to the line
     */
    private static void cleanupSection (Stick stick,
                                        List<GlyphSection> borders,
                                        List<GlyphSection> patches,
                                        GlyphLag lag,
                                        Picture picture,
                                        int minPointNb,
                                        StickSection lineSection,
                                        StickSection sct,
                                        int direction,
                                        boolean borderEnabled)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("cleanup stick=" + stick
                         + ", lag=" + lag
                         + ", minPointNb=" + minPointNb
                         + ", lineSection=" + lineSection
                         + ", sct=" + sct
                         + ", direction=" + direction
                         + ", borderEnabled=" + borderEnabled);
        }

        // We are interested in non-stick vertices, and also in failed sticks
        if (sct.isMember()) {
            if (!(sct.getGlyph().getResult() instanceof FailureResult)) {
                return;
            }
        }

        // Is this sct just a border of the line ?
        if (borderEnabled) {
            if (sct.getRunNb() == 1) { // Too restrictive ??? TBD

                double adj = (direction > 0)
                             ? sct.getFirstAdjacency()
                             : sct.getLastAdjacency();

                if (adj <= constants.maxBorderAdjacency.getValue()) {
                    // No concrete crossing object, let's aggregate this
                    // section to the line border.
                    sct.setParams(SectionRole.BORDER, 0, 0);
                    borders.add(sct);

                    return;
                }
            }
        }

        // This sct is actually crossing, we extend it
        patchSection(stick, patches, lag, minPointNb, lineSection, sct, direction);
    }

    //--------//
    // middle //
    //--------//

    /**
     * Return the position (y) of the middle of the line between c1 & c2
     * abscissa
     *
     * @param c1 left abscissa (if horizontal)
     * @param c2 right abscissa
     *
     * @return the y for middle of found vertices
     */
    private static int middle (Stick stick,
                               int c1,
                               int c2)
    {
        int firstPos = Integer.MAX_VALUE;
        int lastPos = Integer.MIN_VALUE;

        for (Section section : stick.getMembers()) {
            // Check overlap with coordinates at hand
            if (Math.max(c1, section.getStart()) <= Math.min(c2,
                                                             section
                                                             .getStop())) {
                firstPos = Math.min(firstPos, section.getFirstPos());
                lastPos = Math.max(lastPos, section.getLastPos());
            }
        }

        return (int) Math.rint((double) (firstPos + lastPos) / 2);
    }

    //--------------//
    // patchSection //
    //--------------//

    /**
     * Build a patching section, which extends the crossing sct in the given
     * direction.
     *
     * @param section   the line section, through which the sct is extended
     * @param sct       the section of the crossing object
     * @param direction the direction in which extension must be performed
     */
    private static void patchSection (Stick stick,
                                      List<GlyphSection> patches,
                                      GlyphLag lag,
                                      int minPointNb,
                                      StickSection section,
                                      StickSection sct,
                                      int direction)
    {
        Run lineRun; // Run of staff line in contact
        Run run; // Run to be extended
        int yBegin; // y value at beginning of the extension
        Line startTg = new VerticalLine(); // Tangent at runs starts
        Line stopTg = new VerticalLine(); // Tangent at runs stops
        Line axis = null; // Middle axis
        int x1; // left and right abscissas
        int x2; // left and right abscissas
        double x; // Middle abscissa

        if (direction > 0) { // Going downwards
            run = sct.getLastRun();
            lineRun = section.getFirstRun();
            yBegin = section.getFirstPos();
        } else {
            run = sct.getFirstRun();
            lineRun = section.getLastRun();
            yBegin = section.getLastPos();
        }

        int length = run.getLength(); // Length of this contact run

        // Use line portion instead if shorter
        if (lineRun.getLength() < length) {
            if (logger.isDebugEnabled()) {
                logger.debug("line shorter than external contact");
            }

            startTg.include(lineRun.getStart(), yBegin);
            stopTg.include(lineRun.getStop(), yBegin);
            x1 = lineRun.getStart();
            x2 = lineRun.getStop();
            length = lineRun.getLength();
        } else {
            x1 = run.getStart();
            x2 = run.getStop();
        }

        x = (double) (x1 + x2) / 2;

        // Compute the y position where our patch should stop
        final int mid = middle(stick, x1, x2);

        if (mid == 0) {
            logger.warning("Cannot find line");

            return;
        }

        final int yPast = mid + direction; // y value, past the last one

        // Try to compute on a total of minPointNb points
        if ((startTg.getPointNb() + sct.getRunNb()) >= minPointNb) {
            int y = yBegin;

            while (startTg.getPointNb() < minPointNb) {
                y -= direction;

                Run r = sct.getRunAt(y);
                startTg.include(r.getStart(), y);
                stopTg.include(r.getStop(), y);
            }
        }

        // Check whether we have enough runs to compute extension axis
        if (startTg.getPointNb() >= minPointNb) {
            // Check that we don't diverge (convergence to the line is OK)
            if (((stopTg.getSlope() - startTg.getSlope()) * direction) > constants.maxDeltaSlope
                    .getValue()) {
                axis = startTg.include(stopTg); // Merge the two sides
                startTg = stopTg = null;
            }
        } else {
            // No way to compute an axis, just use straight direction
            axis = startTg = stopTg = null;
        }

        // Extend the section, using whatever line(s) we have :
        // With startTg & stopTg : we use both directions
        // With just axis, we use a constant length in the axis direction
        // With nothing, we use a constant length in the y direction
        GlyphSection newSct = null;

        // Sanity check
        if (((yPast - yBegin) * direction) <= 0) {
            logger.debug("Weird relative positions yBegin=" + yBegin
                         + " yPast=" + yPast + " dir=" + direction);
            logger.debug("patchSection line=" + section);
            logger.debug("patchSection contact=" + sct);
        } else {
            for (int y = yBegin; y != yPast; y += direction) {
                int start;

                if (startTg != null) {
                    start = startTg.xAt(y);
                    length = stopTg.xAt(y) - start + 1;

                    if (length <= 0) { // We have decreased to nothing

                        break;
                    }
                } else {
                    if (axis != null) {
                        x = axis.xAt((double) y);
                    }

                    start = (int) Math.rint(x - ((double) length / 2) + 0.5);
                }

                //Run newRun = new Run(start, length, Picture.FOREGROUND); // TBD
                Run newRun = new Run(start, length, 127); // TBD for the 127 value of course

                if (newSct == null) {
                    newSct = lag.createSection(y, newRun);

                    // Make the proper junction
                    if (direction > 0) {
                        StickSection.addEdge(sct, newSct);
                    } else {
                        StickSection.addEdge(newSct, sct);
                    }

                    patches.add(newSct);
                } else {
                    // extend newSct in proper direction
                    if (direction > 0) {
                        newSct.append(newRun);
                    } else {
                        newSct.prepend(newRun);
                    }
                }
            }

            // Update potential lagviews on the lag
            if (newSct != null) {
                for (DigraphView graphView : lag.getViews()) {
                    GlyphLagView lagView = (GlyphLagView) graphView;
                    lagView.addSectionView(newSct);
                }
            }
        }
    }

    //-------//
    // write //
    //-------//
    private static void write (List<GlyphSection> list,
                               Picture picture,
                               int pixel)
    {
        for (GlyphSection section : list) {
            section.write(picture, pixel);
        }
    }

    //~ Classes --------------------------------------------------------------

    private static class Constants
            extends ConstantSet
    {
        //~ Instance variables -----------------------------------------------

        Constant.Double maxDeltaSlope = new Constant.Double
                (0.5d,
                 "Maximum difference of side tangent slopes when patching TBD");

        Constant.Double maxBorderAdjacency = new Constant.Double
                (0.7d,
                 "Maximum adjacency for a section to be a border");

        Constant.Integer patchGreyLevel = new Constant.Integer
                (200,
                 "Grey level to be used when patching crossing objects");

        Constants ()
        {
            initialize();
        }
    }
}
