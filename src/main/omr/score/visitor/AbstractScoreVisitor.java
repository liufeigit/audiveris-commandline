//----------------------------------------------------------------------------//
//                                                                            //
//                  A b s t r a c t S c o r e V i s i t o r                   //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.score.visitor;

import omr.score.Score;
import omr.score.entity.Arpeggiate;
import omr.score.entity.Barline;
import omr.score.entity.Beam;
import omr.score.entity.Chord;
import omr.score.entity.Clef;
import omr.score.entity.Coda;
import omr.score.entity.Dynamics;
import omr.score.entity.Fermata;
import omr.score.entity.KeySignature;
import omr.score.entity.Measure;
import omr.score.entity.MeasureElement;
import omr.score.entity.MeasureNode;
import omr.score.entity.Note;
import omr.score.entity.Ornament;
import omr.score.entity.PartNode;
import omr.score.entity.Pedal;
import omr.score.entity.ScoreNode;
import omr.score.entity.Segno;
import omr.score.entity.Slur;
import omr.score.entity.Staff;
import omr.score.entity.System;
import omr.score.entity.SystemPart;
import omr.score.entity.TimeSignature;
import omr.score.entity.Tuplet;
import omr.score.entity.Wedge;

/**
 * Class <code>AbstractScoreVisitor</code> provides a default implementation of
 * the ScoreVisitor interface
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class AbstractScoreVisitor
    implements ScoreVisitor
{
    //~ Constructors -----------------------------------------------------------

    //----------------------//
    // AbstractScoreVisitor //
    //----------------------//
    /**
     * Creates a new AbstractScoreVisitor object.
     */
    public AbstractScoreVisitor ()
    {
    }

    //~ Methods ----------------------------------------------------------------

    //------------------//
    // visit Arpeggiate //
    //------------------//
    public boolean visit (Arpeggiate arpeggiate)
    {
        return true;
    }

    //---------------//
    // visit Barline //
    //---------------//
    public boolean visit (Barline barline)
    {
        return true;
    }

    //------------//
    // visit Beam //
    //------------//
    public boolean visit (Beam beam)
    {
        return true;
    }

    //-------------//
    // visit Chord //
    //-------------//
    public boolean visit (Chord chord)
    {
        return true;
    }

    //------------//
    // visit Clef //
    //------------//
    public boolean visit (Clef clef)
    {
        return true;
    }

    //------------//
    // visit Coda //
    //------------//
    public boolean visit (Coda coda)
    {
        return true;
    }

    //----------------//
    // visit Dynamics //
    //----------------//
    public boolean visit (Dynamics dynamics)
    {
        return true;
    }

    //---------------//
    // visit Fermata //
    //---------------//
    public boolean visit (Fermata fermata)
    {
        return true;
    }

    //--------------------//
    // visit KeySignature //
    //--------------------//
    public boolean visit (KeySignature keySignature)
    {
        return true;
    }

    //---------------//
    // visit Measure //
    //---------------//
    public boolean visit (Measure measure)
    {
        return true;
    }

    //----------------------//
    // visit MeasureElement //
    //----------------------//
    public boolean visit (MeasureElement measureElement)
    {
        return true;
    }

    //-------------------//
    // visit MeasureNode //
    //-------------------//
    public boolean visit (MeasureNode measureNode)
    {
        return true;
    }

    //------------//
    // visit Note //
    //------------//
    public boolean visit (Note note)
    {
        return true;
    }

    //----------------//
    // visit Ornament //
    //----------------//
    public boolean visit (Ornament ornament)
    {
        return true;
    }

    //----------------//
    // visit PartNode //
    //----------------//
    public boolean visit (PartNode partNode)
    {
        return true;
    }

    //-------------//
    // visit Pedal //
    //-------------//
    public boolean visit (Pedal pedal)
    {
        return true;
    }

    //-----------------//
    // visit ScoreNode //
    //-----------------//
    public boolean visit (ScoreNode scoreNode)
    {
        return true;
    }

    //-------------//
    // visit Score //
    //-------------//
    public boolean visit (Score score)
    {
        return true;
    }

    //-------------//
    // visit Segno //
    //-------------//
    public boolean visit (Segno segno)
    {
        return true;
    }

    //------------//
    // visit Slur //
    //------------//
    public boolean visit (Slur slur)
    {
        return true;
    }

    //-------------//
    // visit Staff //
    //-------------//
    public boolean visit (Staff staff)
    {
        return true;
    }

    //--------------//
    // visit System //
    //--------------//
    public boolean visit (System system)
    {
        return true;
    }

    //------------------//
    // visit SystemPart //
    //------------------//
    public boolean visit (SystemPart systemPart)
    {
        return true;
    }

    //---------------------//
    // visit TimeSignature //
    //---------------------//
    public boolean visit (TimeSignature timeSignature)
    {
        return true;
    }

    //--------------//
    // visit Tuplet //
    //--------------//
    public boolean visit (Tuplet tuplet)
    {
        return true;
    }

    //-------------//
    // visit Wedge //
    //-------------//
    public boolean visit (Wedge wedge)
    {
        return true;
    }
}
