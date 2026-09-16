package com.kalab.clientroute;

import android.content.Context;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.text.TextPosition;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.SimpleDateFormat;

/** Deliberately template-specific: fail closed rather than guess a new layout. */
public final class SchedulePdf {
    public static class Stop {
        public String label="",address="",notes="",flags="",first="",last="";
        public boolean include=true;
    }
    public static class Crew {
        public String workers="";
        public final List<Stop> stops=new ArrayList<>();
        public String toString(){return workers.replace("\n"," / ")+" • "+stops.size()+" stops";}
    }
    public static class Schedule {
        public String date="";
        public final List<Crew> crews=new ArrayList<>();
    }
    static class Glyph {
        float x,right,space;String text;
        Glyph(TextPosition p){x=p.getXDirAdj();right=x+p.getWidthDirAdj();space=p.getWidthOfSpace();text=p.getUnicode();}
    }
    static class Positions extends PDFTextStripper {
        TreeMap<Integer,List<Glyph>> rows=new TreeMap<>();
        Positions() throws IOException{super();}
        @Override protected void processTextPosition(TextPosition t){
            int y=Math.round(t.getYDirAdj());List<Glyph> r=rows.get(y);
            if(r==null){r=new ArrayList<>();rows.put(y,r);}r.add(new Glyph(t));
        }
    }
    static String cell(List<Glyph> row,float from,float to){
        StringBuilder s=new StringBuilder();float right=-1;
        Collections.sort(row,(a,b)->Float.compare(a.x,b.x));
        for(Glyph g:row)if(g.x>=from&&g.x<to){
            if(right>=0 && g.x-right>Math.max(.5f,g.space*.5f) && s.length()>0 && s.charAt(s.length()-1)!=' ')s.append(' ');
            s.append(g.text);right=g.right;
        }
        return s.toString().trim().replaceAll("\\s+"," ");
    }
    public static String key(String s){return s.toLowerCase(Locale.US).replace("texas","tx").replaceAll("[^a-z0-9]","");}
    public static Schedule read(Context context,InputStream input) throws Exception {
        PDFBoxResourceLoader.init(context);
        Schedule schedule=new Schedule();Map<String,String> notes=new HashMap<>();
        try(PDDocument doc=PDDocument.load(input)){
            if(doc.isEncrypted())throw new IOException("Password-protected schedules are not supported.");
            if(doc.getNumberOfPages()>10)throw new IOException("Please choose a daily schedule with 10 pages or fewer.");
            for(int page=1;page<=doc.getNumberOfPages();page++){
                float width=doc.getPage(page-1).getMediaBox().getWidth();
                if(Math.abs(width-612)>3)throw new IOException("This PDF uses a different page layout. No jobs were imported.");
                Positions p=new Positions();p.setStartPage(page);p.setEndPage(page);p.getText(doc);
                boolean schedulePage=false,notesPage=false;
                for(List<Glyph> row:p.rows.values()){
                    String all=cell(row,0,612);
                    if(all.contains("Address")&&all.contains("Date")&&all.contains("TIME"))schedulePage=true;
                    if(all.contains("Address")&&all.contains("Notes"))notesPage=true;
                }
                if(!schedulePage&&!notesPage)continue;
                Crew crew=null;
                for(List<Glyph> row:p.rows.values()){
                    if(notesPage&&!schedulePage){
                        String label=cell(row,49,124),address=cell(row,124,241),note=cell(row,241,565);
                        if(!label.isEmpty()&&!address.isEmpty()&&!note.isEmpty()&&!label.equals("Start of Day")&&!note.equals("Notes"))
                            notes.put(key(label)+"|"+key(address),note);
                        continue;
                    }
                    String label=cell(row,49,152),address=cell(row,152,285),date=cell(row,285,318),worker=cell(row,396,442);
                    if(label.equals("Start of Day")){
                        crew=new Crew();schedule.crews.add(crew);
                        if(date.matches("\\d{1,2}/\\d{1,2}/\\d{4}")){
                            SimpleDateFormat source=new SimpleDateFormat("M/d/yyyy",Locale.US);source.setLenient(false);
                            String parsed=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(source.parse(date));
                            if(!schedule.date.isEmpty()&&!schedule.date.equals(parsed))throw new IOException("Multiple schedule dates found. No jobs were imported.");
                            schedule.date=parsed;
                        }
                    }
                    if(crew==null)continue;
                    if(!worker.isEmpty()&&!worker.contains("Total")&&!worker.contains("Hours")){
                        if(!crew.workers.isEmpty())crew.workers+="\n";crew.workers+=worker;
                    }
                    if(label.isEmpty()||label.equals("Start of Day")||address.isEmpty()||address.contains("#N/A")||label.equals("Address"))continue;
                    Stop stop=new Stop();stop.label=label;stop.address=address;
                    Matcher gate=Pattern.compile("(?i)gate(?:code)?\\s*#?\\d+#?").matcher(label);
                    String name=label;if(gate.find()){stop.flags=gate.group();name=label.substring(0,gate.start()).trim();}
                    String[] parts=name.split("\\s+",2);stop.last=parts[0];stop.first=parts.length>1?parts[1]:"";
                    // Ambiguous labels and incomplete addresses require a deliberate selection.
                    stop.include=parts.length==2 && parts[1].split("\\s+").length==1 && address.matches("^\\d+.*") && address.contains(",");
                    crew.stops.add(stop);
                }
            }
        }
        for(Crew c:schedule.crews)for(Stop stop:c.stops)
            stop.notes=notes.containsKey(key(stop.label)+"|"+key(stop.address))?notes.get(key(stop.label)+"|"+key(stop.address)):"";
        if(schedule.crews.isEmpty()||schedule.date.isEmpty())throw new IOException("The known daily-schedule table and date were not found. Use the text-based PDF layout provided for this app; scanned PDFs and Excel files are not supported.");
        for(Crew c:schedule.crews)if(c.workers.isEmpty())c.workers="Unassigned crew";
        return schedule;
    }
}
