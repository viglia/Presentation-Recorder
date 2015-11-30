package com.android.viglia.pdfaudiorecorderapp.utility;

import java.io.Serializable;

/**
 *This class represents a portion of the presentation.
 *It states a page index and the time this page was first selected in the given portion.
 *
 * Ex. Frame1:(pag.1 - seconds 0)  --> Frame2:(page.2 - seconds 4)  --> Frame3: (pag.1 - seconds 10)
 */
public class SlideRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private int slide;
    private long seconds;

    /**
     *
     * @param slide the index of the pdf page
     * @param seconds the time (expressed in seconds) when this page has been selected
     */
    public SlideRecord(int slide, long seconds){
        this.slide = slide;
        this.seconds = seconds;
    }

    public SlideRecord(){}

    public void setSlide(int slide){
        this.slide = slide;
    }

    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    public int getSlide(){
        return slide;
    }

    public long getSeconds(){
        return seconds;
    }
}
