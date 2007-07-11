package omr.jaxb.basic;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
public class Purse
{
    @XmlElement(name="tip")
    public double[] tips = new double[]{1.0, 2.345, 4.5};

    public Double[] getTips()
    {
        Double[] dd = null;
        if (tips != null) {
            dd = new Double[tips.length];
            for (int i = 0; i < tips.length; i++) {
                dd[i] = tips[i];
            }
        }
        return dd;
    }

    public void setTips (Double[] tips)
    {
        this.tips = new double[tips.length];
        for (int i = 0; i < tips.length; i++) {
            this.tips[i] = tips[i];
        }
    }
}
