package util.processing;

import processing.core.PApplet;

/**
 * Created by lilia on 4/12/2017.
 */
public class Guess  extends PApplet {
    float number;//Define 'number' as a random float
    int newNum; //Define 'newNum' as an integer
    long starttime = 0;

    public void settings() {
        size(600, 300);
    }

    public void setup() {
  //      size(600,300);
        background(255);
        //frameRate(5);
        float number = random(1,5); //Define 'number' as a random float ranging from 1 -5
        newNum = round(number); //Convert float into an integer


    }


    public void draw() {
        background(255);
        intro();
        text(newNum, 100, 100);

        if (keyPressed) {
            println (key + ", " + newNum);
            if (Character.digit(key, 10) == newNum) {

                fill(0);
                text("You are right. The number is ", 200, 200);
                text(newNum, 350,50);


            } else {
                fill(0);
                text("Sorry. You are wrong. The number is ", 200, 200);
                text(newNum, 350,250);

            }
        }
    }



    public void intro() {
        fill(0);
        textSize(16);
        text("Guess a number ranging from 1 to 5.", 50, 50);
    }

    public static void main(String... args){

        Guess guess = new Guess();
        PApplet.main(guess.getClass().getCanonicalName());

    }
}
