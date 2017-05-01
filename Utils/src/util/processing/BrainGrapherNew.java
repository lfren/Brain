package util.processing;

/**
 * Created by lilia on 3/31/2017.
 */
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.serial.Serial;

import java.awt.*;
import java.io.PrintWriter;

public class BrainGrapherNew extends PApplet{
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

            // Set up the ControlP5 knobs and dials
        controlP5 = new ControlP5(this);
        // controlP5.setColorLabel(color(0));
        controlP5.setColorBackground(color(0));
        controlP5.disableShortcuts();
        //controlP5.disableMouseWheel();
        controlP5.setMoveable(false);

        // Create the channel objects
        channels[0] = new Channel("Signal Quality", color(0), "");
        channels[1] = new Channel("Attention", color(100), "");
        channels[2] = new Channel("Meditation", color(50), "");
        channels[3] = new Channel("Delta", color(219, 211, 42), "Dreamless Sleep");
        channels[4] = new Channel("Theta", color(245, 80, 71), "Drowsy");
        channels[5] = new Channel("Low Alpha", color(237, 0, 119), "Relaxed");
        channels[6] = new Channel("High Alpha", color(212, 0, 149), "Relaxed");
        channels[7] = new Channel("Low Beta", color(158, 18, 188), "Alert");
        channels[8] = new Channel("High Beta", color(116, 23, 190), "Alert");
        channels[9] = new Channel("Low Gamma", color(39, 25, 159), "Multi-sensory processing");
        channels[10] = new Channel("High Gamma", color(23, 26, 153), "???");

        // Manual override for a couple of limits.
        channels[0].minValue = 0;
        channels[0].maxValue = 200;
        channels[1].minValue = 0;
        channels[1].maxValue = 100;
        channels[2].minValue = 0;
        channels[2].maxValue = 100;
        channels[0].allowGlobal = false;
        channels[1].allowGlobal = false;
        channels[2].allowGlobal = false;

        // Set up the monitors, skip the signal quality
        for (int i = 0; i < monitors.length; i++) {
            monitors[i] = new Monitor(channels[i + 1], i * (width / 10), height / 2, width / 10, height / 2, controlP5);
        }

        monitors[monitors.length - 1].w += width % monitors.length;

        // Set up the graph
        graph = new Graph(0, 0, width, height / 2, controlP5, channels);

        // Set yup the connection light
        connectionLight = new ConnectionLight(width - 140, 10, 20, controlP5, channels);

        output = createWriter("data.txt");

    }

    public void serialEvent(Serial p) {
        // Split incoming packet on commas
        // See https://github.com/kitschpatrol/Arduino-Brain-Library/blob/master/README for information on the CSV packet format
        String incomingString = "";
        try {
            incomingString = p.readString(); //.trim();
            System.out.println(" incomingString " + incomingString);
        } catch(Exception e) {
            System.out.println(" incomingString error " + incomingString);
            e.printStackTrace();
        }
        println("hello");
     /*   print("Received string over serial: ");
        println(incomingString);
//  saveStrings("processing_test.txt",incomingString.split("!"));
        output.println(incomingString);
        output.flush();*/
/*
        String[] incomingValues = split(incomingString, ',');

        // Verify that the packet looks legit
        if (incomingValues.length > 1) {
            packetCount++;

            // Wait till the third packet or so to start recording to avoid initialization garbage.
            if (packetCount > 3) {

                for (int i = 0; i < incomingValues.length; i++) {
                    String stringValue = incomingValues[i].trim();

                    int newValue = Integer.parseInt(stringValue);

                    // Zero the EEG power values if we don't have a signal.
                    // Can be useful to leave them in for development.
                    if ((Integer.parseInt(incomingValues[0]) == 200) && (i > 2)) {
                        newValue = 0;
                    }

                    channels[i].addDataPoint(newValue);
                }
            }
        }*/
    }

    public void settings(){
        size(1024, 768);
    }

    public void draw(){
        background(0);
        ellipse(mouseX, mouseY, 20, 20);
    }

    public static void main(String... args){
        BrainGrapherNew pt = new BrainGrapherNew();
        PApplet.main(pt.getClass().getCanonicalName());

    }
}