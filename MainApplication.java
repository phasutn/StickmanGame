
package Project3;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*; 
import java.util.ArrayList; 

public class MainApplication extends JFrame{
    private MainApplication currentFrame;
    private JPanel contentpane;
    private JLabel drawpane;
    private JTextArea textArea;
    private MyImageIcon backgroundImg;
    private int frameWidth = 800, frameHeight  = 600;
    private String hatOptions[] = {"stickman"};
    private int hatSelected = 0;

    

    public MainApplication(){
        setTitle("Title Page");
        setBounds(50, 50, frameWidth, frameHeight);
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
        currentFrame = this;
        
        contentpane = (JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout());

        AddComponents();
        validate();
    }

    public void AddComponents(){
        //String path = "src/main/java/Project3/resources/"; //Maven
        String path = "./resources/mainappbg.png";
        backgroundImg  = new MyImageIcon(path).resize(frameWidth, frameHeight);
        drawpane = new JLabel();
        drawpane.setIcon(backgroundImg);
        drawpane.setLayout(null);

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                dispose();
                new GameWindow(hatOptions[hatSelected]);
            }
        });

        JComboBox<String> comboBox = new JComboBox<>(hatOptions);
        comboBox.setSelectedIndex(0);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hatSelected = comboBox.getSelectedIndex();
            }
        });
        comboBox.setPreferredSize(new Dimension(100, 20));

        textArea = new JTextArea(10, 50);
        textArea.setEditable(false);

        JPanel bottomPanel = new JPanel();

        bottomPanel.setBackground(new Color(255, 255, 0));

        bottomPanel.add(startButton);
        bottomPanel.add(comboBox);
        bottomPanel.add(textArea, BorderLayout.EAST);

        
        contentpane.add(bottomPanel, BorderLayout.SOUTH);
        contentpane.add(drawpane, BorderLayout.NORTH);
    }

    public static void main(String[] args) {
        new MainApplication();
    }
}

public class GameWindow extends JFrame implements KeyListener{

    private GameWindow currentFrame;

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
    private JTextField scoreText;
    private int score;
    private String hatFileName;


    public GameWindow(String hatName){
        setTitle("Stickman Game");
        setBounds(50, 50, frameWidth, frameHeight);
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
        currentFrame = this;
        hatFileName = hatName;

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                GameWindow frame = (GameWindow)e.getWindow();
              JOptionPane.showMessageDialog(frame, ("Score = " + score), "Game Ended", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        contentpane = (JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout());
        addKeyListener(this);
        AddComponents();
        requestFocus();
    }

    public void AddComponents(){
        //String path = "src/main/java/Project3/resources/"; //Maven
        String path = "./resources/";
        backgroundImg  = new MyImageIcon(path + "testbg.jpg").resize(frameWidth, frameHeight);
        
        stickmanLabel = new StickManLabel(currentFrame, hatFileName);
        //stickmanLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
        currentLevel = new JTextField("0", 3);		
        currentLevel.setEditable(false);

        JPanel control  = new JPanel();
        control.setBounds(0,0,1000,50);
        control.add(new JLabel("Diffuculty - "));

        JButton itemButton = new JButton("Use item"); // Button to use speed boost
        itemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                setSpeedBoostThread();
                requestFocus();
            }
        });

        scoreText = new JTextField("0", 5); // Text Field for score
        scoreText.setEditable(false);

        control.add(itemButton);
        control.add(scoreText);
        drawpane = new JLabel();
        drawpane.setLayout(null);
        drawpane.setIcon(backgroundImg);
        drawpane.add(stickmanLabel);
        contentpane.add(control, BorderLayout.NORTH);
        contentpane.add(drawpane, BorderLayout.CENTER); 
        setFloorThread(); 
        setStickmanThread();
        setEnemySpawnThread(stickmanLabel);
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
                        if(grassfloorLabel.intersectsSpike(stickmanLabel) ||
                           grassfloorLabelnext.intersectsSpike(stickmanLabel)){
                            if(!stickmanLabel.isInvincible()){
                                setInvincibleFrame();
                                deductScore(3);
                            }
                        }
                    }
                    Thread.currentThread().interrupt();
                } 
            }; 
        floorThread.start();
    }

    public void setEnemySpawnThread(StickManLabel stickmanLabel){
        Thread enemySpawnThread = new Thread(){
            public void run(){
                while(true){
                    setEnemyThread(stickmanLabel);
                    try { Thread.sleep(3000); } // Time between enemy spawn
                    catch(InterruptedException e) {}
                }
            }
        };
        enemySpawnThread.start();
    }

    public void setEnemyThread(StickManLabel stickmanLabel){
        Thread enemyThread = new Thread(){
            public void run(){
                EnemyLabel enemyLabel = new EnemyLabel(currentFrame, drawpane, stickmanLabel);
                drawpane.add(enemyLabel);
                while(enemyLabel.isAlive() && enemyLabel.isAllAlive()){
                    enemyLabel.move();
                }
                Thread.currentThread().interrupt();
            }
        };
        enemyThread.start();
    }

    public void setSpeedBoostThread(){ // During the duration, switch the speed of grass floor and enemy, also put the stickman into invincible state
        Thread speedBoostThread = new Thread(){
            public void run(){
                int grassSpeed = 1, enemySpeed = 10, duration = 5000;

                if(!stickmanLabel.isInvincible()) stickmanLabel.setInvincible(true);
                grassSpeed = GrassfloorLabel.changeSpeed(grassSpeed);
                enemySpeed = EnemyLabel.changeSpeed(enemySpeed);
                try { Thread.sleep(duration); } 
                catch (InterruptedException e) { e.printStackTrace(); }
                grassSpeed = GrassfloorLabel.changeSpeed(grassSpeed);
                enemySpeed = EnemyLabel.changeSpeed(enemySpeed);
                setInvincibleFrame();

                Thread.currentThread().interrupt();
            }
        };
        speedBoostThread.start();
    }

    public void setInvincibleFrame(){
        Thread invisibleframeThread = new Thread(){
            public void run(){
                int time = 2000;
                boolean visible = false;
                stickmanLabel.setInvincible(true);
                for(int i=0; i<=time; i+=time/10){
                    stickmanLabel.setVisible(visible);
                    visible = !visible;
                    try { Thread.sleep(time/10); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                stickmanLabel.setVisible(true);
                stickmanLabel.setInvincible(false);
                Thread.currentThread().interrupt();
            }
        };
        invisibleframeThread.start();
    }

    public synchronized void deductScore(int minusScore){
            score -= minusScore;
            scoreText.setText(Integer.toString(score));
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

    // public static void main(String[] args) {
    //     new GameWindow();
    // }

}

class StickManLabel extends JLabel{
    private MyImageIcon StickMan;
    private GameWindow parentFrame;
    private int frameWidth, frameHeight;
    
    private int offsetX = 0;

    //String imagePath = "src/main/java/Project3/resources/stickman.png"; //Maven
    String imagePath = "./resources/";
    
    //Stickman Properties
    private int width = 348/2, height  = 493/2;
    private int curX = 0, curY = 0;
    private int speed = 20;
    private int speedY = 50;
    private int jumpHeight = 300;
    
    //Environment Properties
    private int floorHeight;
    private int gravity = 20;

    private boolean invincible = false;

    public StickManLabel(GameWindow pf, String imageName){
        parentFrame = pf;
        frameWidth = parentFrame.getWidth();
        frameHeight = parentFrame.getHeight();
        floorHeight = frameHeight / 2 - 50;
        curX = frameWidth / 20;
        curY = floorHeight;
        

        StickMan = new MyImageIcon(imagePath + imageName + ".png").resize(width, height);
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

    public int getFloor(){
        return floorHeight;
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
            try { Thread.sleep(50); } 
            catch (InterruptedException e) { e.printStackTrace(); } 
        }
        repaint();
    }

    public boolean isInvincible(){
        return invincible;
    }

    public void setInvincible(boolean status){
        invincible = status;
    }
}

class GrassfloorLabel extends JLabel{
    private MyImageIcon GrassImage;
    private MyImageIcon SpikeImage;
    private GameWindow parentFrame;
    private int[] layout;

    //String imagePath = "src/main/java/Project3/resources/Grassfloor.png"; //Maven
    String grassPath = "./resources/Grassfloor.png";
    String spikePath = "./resources/Spikefloor.png";
    
    //Size and Bounds
    private int width, height;
    private int curX = 0, curY = 0;
    private int sectionWidth; //width of each floor sections;

    //Spike HitBoxes
    private ArrayList<JLabel> spikeLabels = new ArrayList<>();
    private ArrayList<Integer> spikeOriginalXPositions = new ArrayList<>();

    //Floor Environment
    private static int stageSpeed = 10;

    public GrassfloorLabel(GameWindow pf, int[] Maplayout, int xPosition){
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
                sectionLabel.setBounds(curX, curY, sectionWidth, height);
                //sectionLabel.setBounds(curX + sectionWidth/4, curY, sectionWidth/3, height);
                //sectionLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                
                //this label is for hitbox and cannot be seen on the frame
                JLabel spikeLabel = new JLabel();
                spikeLabel.setBounds(curX + sectionWidth/4, curY, sectionWidth/3, height);
                spikeOriginalXPositions.add(spikeLabel.getX());
                spikeLabels.add(spikeLabel);
                
                //System.out.println(spikeLabel.getBounds());

                add(sectionLabel);
            }

            curX += sectionWidth;
        }
    }

    public void updateLocation(){
        setLocation(getX() - 10, getY());
        for(JLabel spikeLabel : spikeLabels){
            spikeLabel.setLocation(spikeLabel.getX() - 10, getY());
        }
        repaint();
        try { Thread.sleep(stageSpeed); } 
        catch (InterruptedException e) { e.printStackTrace(); } 
    }

    public void setLocation(int xPosition){
        setLocation(xPosition, getY());
        for (int i = 0; i < spikeLabels.size(); i++) {
            
            //Adjust the location to match its original location
            JLabel spikeLabel = spikeLabels.get(i);
            int originalX = spikeOriginalXPositions.get(i);
            spikeLabel.setLocation(originalX + getX(), getY()); 
            //System.out.println("AAAAAAAA" + spikeLabel.getBounds());
        }
        repaint();
    }

    public static int changeSpeed(int spd){
        int oldSpeed = stageSpeed;
        stageSpeed = spd;
        return oldSpeed;
    }

    public boolean intersectsSpike(StickManLabel Player) {
        Rectangle playerBounds = Player.getBounds();

        for(JLabel spikeLabel : spikeLabels){
            //Intersects and is on the ground
            if(playerBounds.intersects(spikeLabel.getBounds()) && 
               playerBounds.getY() == Player.getFloor()){
                return true;
            }
        }
        return false;
    }
}

class EnemyLabel extends JLabel{
    private MyImageIcon GrassImage;
    private GameWindow parentFrame;
    private JLabel drawpane;

    //String imagePath = "src/main/java/Project3/resources/enemy.png"; //Maven
    String imagePath = "./resources/enemy.png";
    
    private int width = 100, height = 100;
    private int curX = 0, curY = 0;
    private boolean alive;
    private static boolean allAlive;
    private static int speed = 100;
    private int damage = 5;
    private StickManLabel stickManLabel;

    public EnemyLabel(GameWindow pf, JLabel dp, StickManLabel stickman){
        parentFrame = pf;
        drawpane = dp;
        alive = true;
        allAlive = true;
        stickManLabel = stickman;
        
        curY = (int)(Math.random() * 7777) % ((parentFrame.getHeight() /2) + 50);
        curX = parentFrame.getWidth() + 100;
        GrassImage = new MyImageIcon(imagePath).resize(width, height);
        //setText("ENERMY");
        setIcon(GrassImage);
        setBounds(curX, curY, width, height);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                drawpane.remove((EnemyLabel)e.getSource());
                Thread.currentThread().interrupt();
            }
        });
    }

    public void move(){
        curX = curX - 10;
        if(curX < (-width-10)) alive = false;
        setLocation(curX, curY);

        if(stickManLabel.getBounds().intersects(this.getBounds())){
            if(!stickManLabel.isInvincible()){
                drawpane.remove(this);
                parentFrame.setInvincibleFrame();
                parentFrame.deductScore(damage);
                alive = false;
            }
        }

        repaint();
        try { Thread.sleep(speed); } 
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    public boolean isAlive(){
        return alive;
    }

    public static boolean isAllAlive(){
        return allAlive;
    }

    public static void killAllAlive(){
        allAlive = false;
    }

    public static int changeSpeed(int spd){
        int oldSpeed = speed;
        speed = spd;
        return oldSpeed;
    }
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

