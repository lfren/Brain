package util.processing;

/**
 * Created by lilia on 3/31/2017.
 */
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.serial.Serial;

import java.awt.*;
import java.io.PrintWriter;

public class BrainProcessing extends PApplet{
    public ControlP5 controlP5;

    public Serial serial;

    public Channel[] channels = new Channel[11];
    public Monitor[] monitors = new Monitor[10];
    public Graph graph;
    public ConnectionLight connectionLight;
    public PrintWriter output;

    public int packetCount = 0;
    public int globalMax = 0;
    public String scaleMode;
    public void setup() {
        // Set up window
        //    size(1024, 768);
        frameRate(60);
        smooth();
        surface.setTitle("Processing Brain Grapher");
        frame.setVisible(false);

        // Set up serial connection
        println("Find your Arduino in the list below, note its [index]:\n");
        Toolkit.getDefaultToolkit().beep();
        System.err.println(System.getProperty("java.library.path"));

        for (int i = 0; i < Serial.list().length; i++) {
            println("[" + i + "] " + Serial.list()[i]);
        }
        Toolkit.getDefaultToolkit().beep();
        // Put the index found above here:
        //   serial = new Serial(this, Serial.list()[0], 9600);
        //    serial.bufferUntil(10);
        try {
            serial = new Serial(this, Serial.list()[0], 9600);
            serial.bufferUntil('\n');
        }
        catch(Exception e) {
            println("check settings above and usb cable, something went wrong:");
            e.printStackTrace();
        }



        output = createWriter("data.txt");

    }

    public void serialEvent(Serial p) {
        // Split incoming packet on commas
        // See https://github.com/kitschpatrol/Arduino-Brain-Library/blob/master/README for information on the CSV packet format
        String incomingString = "";
        try {
            incomingString = p.readString();
            if (incomingString.contains(",")) {
                System.out.println(incomingString);
                output.println(incomingString);
                output.flush();
            }

        } catch(Exception e) {
            System.out.println(" incomingString error " + incomingString);
            e.printStackTrace();
        }
    }

    public void settings(){
        size(1024, 768);
    }

    public void draw(){
        /*background(0);
        ellipse(mouseX, mouseY, 20, 20);*/
    }

    public static void main(String... args){
        BrainProcessing pt = new BrainProcessing();
        PApplet.main(pt.getClass().getCanonicalName());

    }
}