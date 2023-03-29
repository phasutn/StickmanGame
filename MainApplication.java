
package StickmanGame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*; 
import java.util.ArrayList; 

public class MainApplication extends JFrame implements KeyListener{

    private MainApplication currentFrame;
    private ProjectLabel currentLabel;

    private JPanel contentpane;
    private JLabel drawpane;

    private JComboBox        combo;
    private JToggleButton    []tb;
    private ButtonGroup      bgroup;
    private JButton          swimButton, stopButton, moreButton;
    private JTextField       currentLevel;

    private MySoundEffect themeSound;
    private MyImageIcon backgroundImg;
    private StickManLabel stickmanLabel;
    private GrassfloorLabel grassfloorLabel;
    private int floorNum = 0;
    private int[] map1 = {1,0,1,0,1}; 

    private int frameWidth = 1366, frameHeight  = 768;
    private boolean GameRunning = true;

    public MainApplication(){
        setTitle("Project 3");
        setBounds(50, 50, frameWidth, frameHeight);
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
        currentFrame = this;

        contentpane = (JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout());
        addKeyListener(this);
        AddComponents();
    }

    public void AddComponents(){
        //String path = "src/main/java/Project3/resources/"; //Maven
        String path = "./resources/";
        backgroundImg  = new MyImageIcon(path + "testbg.jpg").resize(frameWidth, frameHeight);
        
        stickmanLabel = new StickManLabel(currentFrame);

        currentLevel = new JTextField("0", 3);		
        currentLevel.setEditable(false);

        JPanel control  = new JPanel();
        control.setBounds(0,0,1000,50);
        control.add(new JLabel("Level - "));

        drawpane = new JLabel();
        drawpane.setLayout(null);
        drawpane.setIcon(backgroundImg);
        drawpane.add(stickmanLabel);
        contentpane.add(control, BorderLayout.NORTH);
        contentpane.add(drawpane, BorderLayout.CENTER); 
        setFloorThread(); 
        setStickmanThread(); 
        validate(); //To Update Components Added
    }

    public void setStickmanThread(){
        Thread stickmanThread = new Thread() {
            public void run()
            {
                while (true)
                {
                    stickmanLabel.stickmanGravity();
                }          
            } 
        }; 
        stickmanThread.start();
    }

    public void setFloorThread(){
            Thread floorThread= new Thread() 
            {
                public void run() {
                    GrassfloorLabel grassfloorLabel = new GrassfloorLabel(currentFrame, map1, 0);
                    drawpane.add(grassfloorLabel);
                    GrassfloorLabel grassfloorLabelnext = new GrassfloorLabel(currentFrame, map1, frameWidth);
                    drawpane.add(grassfloorLabelnext);
                    while (GameRunning) {
                        //System.out.println("Floor 1: " + grassfloorLabel.getX());
                        //System.out.println("Floor 2: " + grassfloorLabelnext.getX());
                        grassfloorLabel.updateLocation();
                        grassfloorLabelnext.updateLocation();
                        if (grassfloorLabel.getX() < -frameWidth) {
                            grassfloorLabel.setLocation(frameWidth);
                        }
                        if (grassfloorLabelnext.getX() < -frameWidth){
                            grassfloorLabelnext.setLocation(frameWidth);
                        }
                    }
                    currentThread().interrupt();
                } 
            }; 
        floorThread.start();
    }


    @Override
    public void keyPressed(KeyEvent e){
        char key = e.getKeyChar();
        if(key == 'a' || key == 'A'){
            stickmanLabel.moveLeft();
        }
        if(key == 'd' || key == 'D'){
            stickmanLabel.moveRight();
        }
        if(key == 'w' || key == 'W'){
            stickmanLabel.moveUp();
        }
        if(key == 's' || key == 'S'){
            stickmanLabel.moveDown();
        }
        

    }
    
    @Override
    public void keyReleased(KeyEvent e){}
    @Override
    public void keyTyped(KeyEvent e){}

    public static void main(String[] args) {
        new MainApplication();
        System.out.println("Hello");
    }

}

class StickManLabel extends JLabel{
    private MyImageIcon StickMan;
    private MainApplication parentFrame;
    private int frameWidth, frameHeight;
    
    private int offsetX = 0;

    //String imagePath = "src/main/java/Project3/resources/stickman.png"; //Maven
    String imagePath = "./resources/stickman.png";
    
    //Stickman Properties
    private int width = 348/2, height  = 493/2;
    private int curX = 0, curY = 0;
    private int speed = 20;
    private int speedY = 50;
    private int jumpHeight = 300;
    
    //Environment Properties
    private int floorHeight;
    private int gravity = 20;

    public StickManLabel(MainApplication pf){
        parentFrame = pf;
        frameWidth = parentFrame.getWidth();
        frameHeight = parentFrame.getHeight();
        floorHeight = frameHeight / 2 - 50;
        curX = frameWidth / 20;
        curY = floorHeight;
        

        StickMan = new MyImageIcon(imagePath).resize(width, height);
        setIcon(StickMan);
        setBounds(curX, curY, width, height);
    }

    public void moveLeft(){
        if(getX() - speed > 0) setLocation( getX() - speed, getY());
        else setLocation( frameWidth, getY());
    }
    
    public void moveRight(){
        if(getX() + speed < frameWidth) setLocation( getX() + speed, getY());
        else setLocation( 0, getY());
    }

    public void moveUp(){
        if(getY() == floorHeight) setLocation( getX(), getY() - jumpHeight);
    }

    public void moveDown(){
        if(getY() != floorHeight){
            if(getY() - speedY < floorHeight){
                setLocation(getX(), getY() + speedY);
            }
            else{
                setLocation(getX(), floorHeight);
            }
        }
    }

    public void stickmanGravity(){
        if(getY() != floorHeight){
            if(getY() - 1 < floorHeight){
                setLocation(getX(), getY() + gravity);
            }
            else{
                setLocation(getX(), floorHeight);
            }
            repaint();
            try { Thread.sleep(gravity); } 
            catch (InterruptedException e) { e.printStackTrace(); } 
        }
        else{
            repaint();
        }
    }

}

class GrassfloorLabel extends JLabel{
    private MyImageIcon GrassImage;
    private MyImageIcon SpikeImage;
    private MainApplication parentFrame;
    private int[] layout;

    //String imagePath = "src/main/java/Project3/resources/Grassfloor.png"; //Maven
    String grassPath = "./resources/Grassfloor.png";
    String spikePath = "./resources/Spikefloor.png";
    
    //Size and Bounds
    private int width, height;
    private int curX = 0, curY = 0;
    private int sectionWidth; //width of each floor sections;

    private int stageSpeed = 10;

    public GrassfloorLabel(MainApplication pf, int[] Maplayout, int xPosition){
        System.out.println("floor created");
        parentFrame = pf;
        width = parentFrame.getWidth() + 15;
        height = parentFrame.getHeight();
        parentFrame = pf;
        layout = Maplayout;
        sectionWidth = width / layout.length;
        setBounds(curX + xPosition, curY, width, height);

        for(int i = 0; i < layout.length; i++){
            //Take in map layout and create floor based on it
            //First, we create a temp. JLabel(sectionLabel) to hold a section of the floor
            //then we set its image to the floor, adjust its size and add it to the main label
            if(layout[i] == 1){
                GrassImage = new MyImageIcon(grassPath).resize(sectionWidth, height);
                JLabel sectionLabel = new JLabel(GrassImage);
                sectionLabel.setBounds(curX, curY, sectionWidth, height);
                add(sectionLabel);
            }
            if(layout[i] == 0){
                SpikeImage = new MyImageIcon(spikePath).resize(sectionWidth, height);
                JLabel sectionLabel = new JLabel(SpikeImage);
                sectionLabel.setBounds(curX, curY - 20, sectionWidth, height);
                add(sectionLabel);
            }
            curX += sectionWidth;
        }
    }

    public void updateLocation(){
        setLocation(getX() - 10, getY());
        repaint();
        try { Thread.sleep(stageSpeed); } 
        catch (InterruptedException e) { e.printStackTrace(); } 
    }

    public void setLocation(int xPosition){
        setLocation(xPosition, getY());
        repaint();
    }

    // public void changeMap(int[] map){
        
    // }

}

class ProjectLabel extends JLabel{

}


class MyImageIcon extends ImageIcon
{
    public MyImageIcon(String fname)  { super(fname); }
    public MyImageIcon(Image image)   { super(image); }

    public MyImageIcon resize(int width, int height)
    {
	Image oldimg = this.getImage();
	Image newimg = oldimg.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        return new MyImageIcon(newimg);
    }
};


class MySoundEffect
{
    private Clip         clip;
    private FloatControl gainControl;         

    public MySoundEffect(String filename)
    {
	try
	{
            java.io.File file = new java.io.File(filename);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);            
            gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
	}
	catch (Exception e) { e.printStackTrace(); }
    }
    public void playOnce()             { clip.setMicrosecondPosition(0); clip.start(); }
    public void playLoop()             { clip.loop(Clip.LOOP_CONTINUOUSLY); }
    public void stop()                 { clip.stop(); }
    public void setVolume(float gain)
    {
        if (gain < 0.0f)  gain = 0.0f;
        if (gain > 1.0f)  gain = 1.0f;
        float dB = (float)(Math.log(gain) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }
}

