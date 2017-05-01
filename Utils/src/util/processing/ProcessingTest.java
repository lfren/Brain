package util.processing;

/**
 * Created by lilia on 3/31/2017.
 */
import processing.core.PApplet;

public class ProcessingTest extends PApplet{

    public void settings(){
        size(200, 200);
    }

    public void draw(){
        background(0);
        ellipse(mouseX, mouseY, 20, 20);
    }

    public static void main(String... args){
        ProcessingTest pt = new ProcessingTest();
        PApplet.main(pt.getClass().getCanonicalName());

    }
}