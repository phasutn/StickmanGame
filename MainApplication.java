
package Project3;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;  

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

    private int frameWidth = 1366, frameHeight  = 768;


    public MainApplication(){
        setTitle("Project 3");
        setBounds(50, 50, frameWidth, frameHeight);
        setResizable(false);
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
        int[] map1 = {1,0,1,0,1}; 
        grassfloorLabel = new GrassfloorLabel(currentFrame, map1);

        swimButton = new JButton("Swim");  
        swimButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

            }
        });
        

        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {

            }
        });
        

        String[] speed = { "fast", "medium", "slow"};
        combo = new JComboBox(speed);
        combo.setSelectedIndex(1);
        combo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

            }
        });

        
        tb = new JToggleButton[2];
        bgroup = new ButtonGroup();      
        tb[0] = new JRadioButton("Left");   tb[0].setName("Left");
        tb[1] = new JRadioButton("Right");  tb[1].setName("Right"); 
        tb[0].setSelected(true);

        for (int i=0; i < 2; i++){
            bgroup.add( tb[i] );
            tb[i].addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                }
            });
        }
    

        moreButton = new JButton("More item");
            moreButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                }
            });

        currentLevel = new JTextField("0", 3);		
        currentLevel.setEditable(false);

        JPanel control  = new JPanel();
        control.setBounds(0,0,1000,50);
        control.add(new JLabel("Level - "));
        control.add(currentLevel);
        control.add(swimButton);
        control.add(stopButton);
        control.add(combo);
        control.add(tb[0]);
        control.add(tb[1]);
        control.add(new JLabel("                 "));
        control.add(new JLabel("Item Control : "));
        control.add(moreButton);
        control.add(new JLabel("                 "));
        control.add(new JLabel("Score : "));

        drawpane = new JLabel();
        drawpane.setLayout(null);
        drawpane.setIcon(backgroundImg);
        drawpane.add(stickmanLabel);
        drawpane.add(grassfloorLabel);
        contentpane.add(control, BorderLayout.NORTH);
        contentpane.add(drawpane, BorderLayout.CENTER);   
        validate(); //To Update Components Added
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
    
    //Size based on original image
    private int width = 348/2, height  = 493/2;
    private int curX = 0, curY = 0;
    private int speed = 200;

    public StickManLabel(MainApplication pf){
        parentFrame = pf;
        frameWidth = parentFrame.getWidth();
        frameHeight = parentFrame.getHeight();
        curX = frameWidth / 20;
        curY = frameHeight / 2 - 50;

        StickMan = new MyImageIcon(imagePath).resize(width, height);
        setIcon(StickMan);
        setBounds(curX, curY, width, height);
    }

    public void moveLeft(){
        if(getX() - speed > 0) setLocation( getX() - speed, getY());
        else setLocation( frameWidth - offsetX, getY());
    }
    
    public void moveRight(){
        if(getX() + speed < frameWidth - offsetX) setLocation( getX() + speed, getY());
        else setLocation( 0, getY());
    }

    public void moveUp(){
        if(getX() + speed < frameWidth - offsetX) setLocation( getX() + speed, getY());
        else setLocation( 0, getY());
    }
}

class GrassfloorLabel extends JLabel{
    private MyImageIcon GrassImage;
    private MainApplication parentFrame;
    private int[] layout;

    //String imagePath = "src/main/java/Project3/resources/Grassfloor2.png"; //Maven
    String imagePath = "./resources/Grassfloor2.png";
    
    //Size and Bounds
    private int width, height;
    private int curX = 0, curY = 0;
    private int sectionWidth; //width of each floor sections;

    public GrassfloorLabel(MainApplication pf, int[] Maplayout){
        parentFrame = pf;
        width = parentFrame.getWidth();
        height = parentFrame.getHeight();
        parentFrame = pf;
        layout = Maplayout;
        sectionWidth = width / layout.length;
        setBounds(curX, curY, width, height);

        for(int i = 0; i < layout.length; i++){
            //Take in map layout and create floor based on it
            //First, we create a temp. JLabel(sectionLabel) to hold a section of the floor
            //then we set its image to the floor, adjust its size and add it to the main label
            if(layout[i] == 1){
                GrassImage = new MyImageIcon(imagePath).resize(sectionWidth, height);
                JLabel sectionLabel = new JLabel(GrassImage);
                sectionLabel.setBounds(curX, curY, sectionWidth, height);
                add(sectionLabel);
                System.out.println("New Floor");
            }
            curX += sectionWidth;
        }
    }
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

