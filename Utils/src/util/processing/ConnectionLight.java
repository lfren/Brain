package util.processing;

import controlP5.ControlP5;
import controlP5.Textlabel;
import processing.core.PApplet;

class ConnectionLight extends PApplet {
  // View class to display EEG connection strength.
  // Used as a singleton.

  public int x, y, diameter, latestConnectionValue;
  public int currentColor = 0;
  public int goodColor = color(0, 255, 0);
  public int badColor = color(255, 255, 0);
  public int noColor = color(255, 0, 0);
  public Textlabel label;
  public Textlabel packetsRecievedLabel;
  public ControlP5 controlP5;
  public Channel[] channels;
  public int packetCount;
  public ConnectionLight(int _x, int _y, int _diameter) {
    new ConnectionLight(_x, _y, _diameter, null, null);
  }
  public ConnectionLight(int _x, int _y, int _diameter, ControlP5 controlP5, Channel[] channels ) {
    this.channels = channels;
    this.controlP5 = controlP5;
    x = _x;
    y = _y;
    diameter = _diameter;

    // Set up the text label
    label = new Textlabel(controlP5, "CONNECTION QUALITY", 32, 11, 200, 30);
    label.setMultiline(true);	
    label.setColorValue(color(0));
    
    packetsRecievedLabel = new Textlabel(controlP5, "PACKETS RECEIVED: 0", 5, 35, 200, 30);
    packetsRecievedLabel.setMultiline(false);  
    packetsRecievedLabel.setColorValue(color(0));    
  }

  public void update() {
    // Show red if no packets yet
    if (channels[0].points.size() == 0) {
      latestConnectionValue = 200;
    }
    else {
      latestConnectionValue = channels[0].getLatestPoint().value;
    }

    if (latestConnectionValue == 200) currentColor = noColor;
    if (latestConnectionValue < 200) currentColor = badColor;
    if (latestConnectionValue == 00) currentColor = goodColor;
    
    packetsRecievedLabel.setText("PACKETS RECIEVED: " + packetCount);
    
  }

  public void draw() {
    pushMatrix();
    translate(x, y);

    noStroke();
    fill(255, 150);
    rect(0, 0, 132, 50);

    noStroke();
    fill(currentColor);
    ellipseMode(CORNER);
    ellipse(5, 4, diameter, diameter);

    label.draw();
   packetsRecievedLabel.draw(); 		
    popMatrix();
  }
}