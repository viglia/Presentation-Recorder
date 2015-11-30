package com.android.viglia.pdfaudiorecorderapp.utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by viglia on 10/31/15.
 */
public class Presentation implements Serializable{

    private static final long serialVersionUID = 0L;

    private String id;
    private String title;
    private String Description;

    //A Red-Black tree based NavigableMap implementation
    // this is useful to find the correct slide given a specific time
    // Ex. Frame1(time:0,slide:2),Frame2(time:7,slide:3),.........,FrameN(time:40,slide:i)
    // if we want to get the SlideRecord related to the second 5
    // we can just call slideRecords.floorEntry(5L) and it will return the Frame1
    private TreeMap<Long,Integer> slideRecords;
    private String dirPath;


    public Presentation(){
        slideRecords = new TreeMap<Long,Integer>();
    }

    public void setId(String id){
        this.id = id;
    }

    public void setTitle(String title){
        this.title = title;
    }


    public void addRecord(SlideRecord slideRecord){
        slideRecords.put(slideRecord.getSeconds(),slideRecord.getSlide());
    }

    public String getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }


    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public TreeMap<Long,Integer> getSlides(){
        return slideRecords;
    }

    public void setDirPath(String dirPath){
        this.dirPath = dirPath;
    }

    public String getDirPath(){
        return dirPath;
    }
}
