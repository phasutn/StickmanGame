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
    private String hatOptions[] = {"none", "sombrero", "crown", "beanie", "tophat", "tinfoil"};
    private int hatSelected = 0;

    

    public MainApplication(){
        setTitle("Title Page");
        setLocationRelativeTo(null);
        setBounds(500, 250, frameWidth, frameHeight);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
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
                new GameWindow((MainApplication)SwingUtilities.getWindowAncestor((Component)e.getSource()), hatOptions[hatSelected]);
                dispose();
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

        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);

        JPanel bottomPanel = new JPanel();

        bottomPanel.setBackground(new Color(0, 191, 64));

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

class GameWindow extends JFrame implements KeyListener{

    private GameWindow currentFrame;
    private MainApplication parentFrame;

    private JPanel contentpane;
    private JLabel drawpane;

    private MySoundEffect themeSound;
    private MyImageIcon backgroundImg;
    private StickManLabel stickmanLabel;
    private GrassfloorLabel grassfloorLabel;
    private JButton speedButton;
    private JTextField scoreText;

    private int frameWidth = 1366, frameHeight  = 768;
    private boolean GameRunning = true;
    private boolean boosting = false;
    private String hatFileName;
    private int score;
    private int enemyRequired = 5;
    private int enemyCount;


    public GameWindow(MainApplication pf, String hatName){
        setTitle("Stickman Game");
        setBounds(250, 150, frameWidth, frameHeight);
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        currentFrame = this;
        parentFrame = pf;
        hatFileName = hatName;

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                GameRunning = false;
                EnemyLabel.killAllAlive();
                themeSound.stop();
            }
        });

        contentpane = (JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout());
        addKeyListener(this);
        AddComponents();
        requestFocus();
        setInvincibleFrame();
    }

    public void AddComponents(){
        //String path = "src/main/java/Project3/resources/"; //Maven
        String path = "./resources/";
        backgroundImg  = new MyImageIcon(path + "gamebg.jpg").resize(frameWidth, frameHeight);
        
        themeSound = new MySoundEffect(path + "/sounds/themeSound.wav"); 
        themeSound.playLoop(); themeSound.setVolume(0.4f);

        stickmanLabel = new StickManLabel(currentFrame, hatFileName);
        //stickmanLabel.setBorder(BorderFactory.createLineBorder(Color.RED));

        JPanel control  = new JPanel();
        control.setBounds(0,0,1000,50);

        JButton endButton = new JButton("Quit Game");
        endButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                GameRunning = false;
                EnemyLabel.killAllAlive();
                dispose();
                themeSound.stop();
                new EndWindow(parentFrame, score);
            }
        });
        control.add(endButton);

        control.add(new JLabel("Diffuculty - "));

        
        JRadioButton superEasyBtn = new JRadioButton("Super Slow");
        superEasyBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){
            if(!boosting) setSpeed(300, 20, 300);
        }});
        JRadioButton easyBtn = new JRadioButton("Slow");
        easyBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){
            if(!boosting) setSpeed(200, 15, 250);
        }});
        JRadioButton mediumBtn = new JRadioButton("Medium");
        mediumBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){
            if(!boosting) setSpeed(120, 10, 200);
        }});
        mediumBtn.setSelected(true);
        JRadioButton hardBtn = new JRadioButton("Fast");
        hardBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){
            if(!boosting) setSpeed(80, 8, 100);
        }});
        JRadioButton superHardBtn = new JRadioButton("LIGHTSPEED");
        superHardBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e){
            if(!boosting) setSpeed(1, 3, 50);
        }});

        ButtonGroup diffBtnGroup = new ButtonGroup();
        diffBtnGroup.add(superEasyBtn);
        diffBtnGroup.add(easyBtn);
        diffBtnGroup.add(mediumBtn);
        diffBtnGroup.add(hardBtn);
        diffBtnGroup.add(superHardBtn);
        control.add(superEasyBtn);
        control.add(easyBtn);
        control.add(mediumBtn);
        control.add(hardBtn);
        control.add(superHardBtn);

        speedButton = new JButton("Boost Not Ready"); // Button to use speed boost
        speedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(enemyCount >= enemyRequired){
                    enemyCount = 0;
                    speedButton.setText("Boost Not Ready");
                    enemyCount = 0;
                    setSpeedBoostThread();
                }
                requestFocus();
            }
        });

        
        JLabel scoreTextLabel = new JLabel("Score:");
        scoreText = new JTextField("0", 5); // Text Field for score
        scoreText.setEditable(false);

        control.add(speedButton);
        control.add(scoreTextLabel);
        control.add(scoreText);
        drawpane = new JLabel();
        drawpane.setLayout(null);
        drawpane.setIcon(backgroundImg);
        drawpane.add(stickmanLabel);
        contentpane.add(control, BorderLayout.NORTH);
        contentpane.add(drawpane, BorderLayout.CENTER); 
        setFloorThread(); 
        setScoreThread();
        setStickmanThread();
        setEnemySpawnThread(stickmanLabel);
        validate(); //To Update Components Added
        setInvincibleFrame();
    }

    public void setSpeed(int jumpDistance, int stageSpeed, int enemySpeed){
        StickManLabel.setJumpDistance(jumpDistance);
        GrassfloorLabel.changeSpeed(stageSpeed);
        EnemyLabel.changeSpeed(enemySpeed);
        requestFocus();
    }

    public synchronized void plusEnemy(){
        enemyCount++;
        if(enemyCount >= enemyRequired) speedButton.setText("Boost Ready");
        else if(enemyCount > 0) speedButton.setText(enemyCount + "/" + enemyRequired);
    }

    public synchronized void deductScore(int minusScore){
        //System.out.println(minusScore);
        score -= minusScore;
        scoreText.setText("Score: " + Integer.toString(score));
    }

    public void setScoreThread(){
        Thread scoreThread = new Thread() {
            public void run()
            {
                while (GameRunning)
                {
                    synchronized(this){
                        score++;
                        scoreText.setText(Integer.toString(score));
                    }
                    try { Thread.sleep(GrassfloorLabel.getSpeed()); } // Time between enemy spawn
                    catch(InterruptedException e) {}
                }
            } 
        }; 
        scoreThread.start();
    }
    
    public void setStickmanThread(){
        Thread stickmanThread = new Thread() {
            public void run()
            {
                while (GameRunning)
                {
                    stickmanLabel.stickmanGravity();
                    stickmanLabel.updateHatLocation();
                }
            } 
        }; 
        stickmanThread.start();
    }

    public void setFloorThread(){
            Thread floorThread= new Thread() 
            {
                public void run() {
                    //1 - grass, 0 - spike
                    int[] map1 = {1,1,0,1,1};
                    int[] map2 = {1,0,1,1,0};
                    GrassfloorLabel grassfloorLabel = new GrassfloorLabel(currentFrame, map1, 0);
                    drawpane.add(grassfloorLabel);
                    GrassfloorLabel grassfloorLabelnext = new GrassfloorLabel(currentFrame, map2, frameWidth);
                    drawpane.add(grassfloorLabelnext);
                    grassfloorLabelnext.setLocation(frameWidth);
                    
                    MySoundEffect hurtSound;
                    String hurtSoundPath = "./resources/sounds/hurtSound.wav";
                    hurtSound = new MySoundEffect(hurtSoundPath);
                    while (GameRunning) {
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
                                System.out.println("hurt");
                                hurtSound.playOnce();
                                setInvincibleFrame();
                                deductScore(500);
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
                while(GameRunning){
                    setEnemyThread(stickmanLabel);
                    try { Thread.sleep(3000); } // Time between enemy spawn
                    catch(InterruptedException e) {}
                }
                Thread.currentThread().interrupt();
            }
        };
        enemySpawnThread.start();
    }

    public void setEnemyThread(StickManLabel stickmanLabel){
        Thread enemyThread = new Thread(){
            public void run(){
                EnemyLabel enemyLabel = new EnemyLabel(currentFrame, drawpane, stickmanLabel);
                drawpane.add(enemyLabel);
                while(enemyLabel.isAlive() && EnemyLabel.isAllAlive()){
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
                int grassSpeed = 1, enemySpeed = 10;
                int duration = ((int)(Math.random() * 222) % (3000)) + 2000; // Random spawn interval from 2-5 seconds

                if(!stickmanLabel.isInvincible()) stickmanLabel.setInvincible(true);
                boosting = true;
                grassSpeed = GrassfloorLabel.changeSpeed(grassSpeed);
                enemySpeed = EnemyLabel.changeSpeed(enemySpeed);
                try { Thread.sleep(duration); } 
                catch (InterruptedException e) { e.printStackTrace(); }
                grassSpeed = GrassfloorLabel.changeSpeed(grassSpeed);
                enemySpeed = EnemyLabel.changeSpeed(enemySpeed);
                boosting = false;
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
                //stickmanLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                for(int i=0; i<=time; i+=time/10){
                    stickmanLabel.setVisible(visible);
                    visible = !visible;
                    try { Thread.sleep(time/10); } 
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                stickmanLabel.setVisible(true);
                stickmanLabel.setInvincible(false);
                Thread.currentThread().interrupt();
                //stickmanLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        };
        invisibleframeThread.start();
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

}


class StickManLabel extends JLabel{
    private JLabel hatLabel;
    private GameWindow parentFrame;
    private int frameWidth, frameHeight;
    
    private int offsetX = 0;

    //String imagePath = "src/main/java/Project3/resources/stickman.png"; //Maven
    String imagePath = "./resources/stickman.png";
    String hatPath = "./resources/hats/";
    String jumpSoundPath = "./resources/sounds/jumpSound.wav";

    //Resources
    private MyImageIcon stickImage, hatImage;
    private MySoundEffect jumpSound;

    //Stickman Properties
    private int width = 348/2, height  = 493/2;
    private int curX = 0, curY = 0;
    private int speed = 20;
    private int speedY = 50;
    private int jumpHeight = 300;
    private static int jumpDistance = 100;
    private boolean invincible = false;

    //Environment Properties
    private int floorHeight;
    private int gravity = 10;

    public StickManLabel(GameWindow pf, String hatName){
        parentFrame = pf;
        frameWidth = parentFrame.getWidth();
        frameHeight = parentFrame.getHeight();
        floorHeight = frameHeight / 2 - 50;
        curX = frameWidth / 20;
        curY = floorHeight;

        stickImage = new MyImageIcon(imagePath).resize(width, height);
        setIcon(stickImage);
        setBounds(curX, curY, width, height);

        // Add hat image
        this.hatImage = new MyImageIcon(hatPath + hatName + ".png").resize(100, 100);
        this.hatLabel = new JLabel(hatImage);
        hatLabel.setBounds(curX, curY, 200/2, 200/2);
        //hatLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
        parentFrame.add(hatLabel, 0);

        //Add sounds
        this.jumpSound = new MySoundEffect(jumpSoundPath);
    }

    public void updateHatLocation(){
        hatLabel.setLocation(getX() + 38, getY() - 30);
    }

    public void moveLeft(){
        if(getX() - speed + 50 > 0) setLocation( getX() - speed, getY());
        else setLocation( getX(), getY());
    }
    
    public void moveRight(){
        if(getX() + speed + 150 < frameWidth) setLocation( getX() + speed, getY());
        else setLocation( getX(), getY());
    }

    public void moveUp(){
        if(getY() == floorHeight){
            jumpSound.playOnce();
            //System.out.println("JUMP");
            new Thread(() -> {
                int initialY = getY();
                int initialX = getX();
                int targetY = floorHeight - jumpHeight;
                int targetX = initialX + jumpDistance;
                int currentY = initialY;
                int currentX = initialX;
                double t = 0;
                while (t <= 1.0) {
                    t += 0.1;
                    currentX = (int) (initialX + (targetX - initialX) * t);
                    currentY = (int) (initialY + (targetY - initialY) * t);
                    if(currentX + 150 > frameWidth){setLocation(getX(), currentY);}
                    else setLocation(currentX, currentY);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentY = targetY;
                setLocation(getX(), currentY);
            }).start();
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
            validate();
            repaint();
            try { Thread.sleep(gravity); } 
            catch (InterruptedException e) { e.printStackTrace(); } 
        }
        validate();
        repaint();
    }

    public static void setJumpDistance(int newDistance){
        jumpDistance = newDistance;
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

        updateMap(Maplayout);
    }

    public void updateMap(int[] newLayout){
        removeAll(); // remove all the current labels from the GrassfloorLabel
        spikeLabels.clear();
        spikeOriginalXPositions.clear();
        layout = newLayout; 
        sectionWidth = width / layout.length;
        curX = 0;
    
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
        revalidate(); // refresh the layout of the GrassfloorLabel
    }

    public void updateLocation(){
        setLocation(getX() - 10, getY());
        for(JLabel spikeLabel : spikeLabels){
            spikeLabel.setLocation(spikeLabel.getX() - 10, getY());
        }
        validate();
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
        validate();
        repaint();
    }

    public static int changeSpeed(int spd){
        int oldSpeed = stageSpeed;
        stageSpeed = spd;
        return oldSpeed;
    }

    public static int getSpeed(){
        return stageSpeed;
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
    private int damage = 700;
    private StickManLabel stickManLabel;
    private MySoundEffect hurtSound, monsterSound;
    private String hurtSoundPath = "./resources/sounds/hurtSound.wav";
    private String monsterSoundPath = "./resources/sounds/monsterSound.wav";

    public EnemyLabel(GameWindow pf, JLabel dp, StickManLabel stickman){
        parentFrame = pf;
        drawpane = dp;
        alive = true;
        allAlive = true;
        stickManLabel = stickman;

        hurtSound = new MySoundEffect(hurtSoundPath);
        monsterSound = new MySoundEffect(monsterSoundPath);
        curY = (int)(Math.random() * 7777) % ((parentFrame.getHeight() /2) + 50);
        curX = parentFrame.getWidth() + 100;
        GrassImage = new MyImageIcon(imagePath).resize(width, height);
        setIcon(GrassImage);
        setBounds(curX, curY, width, height);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                parentFrame.plusEnemy();
                monsterSound.playOnce();
                drawpane.remove((EnemyLabel)e.getSource());
                alive = false;
                revalidate();
                repaint();
            }
        });
        //this.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
    }

    public void move(){
        curX = curX - 10;
        if(curX < (-width-10)) alive = false;
        setLocation(curX, curY);

        if(stickManLabel.getBounds().intersects(this.getBounds())){
            if(!stickManLabel.isInvincible()){
                hurtSound.playOnce();
                drawpane.remove(this);
                parentFrame.setInvincibleFrame();
                parentFrame.deductScore(damage);
                alive = false;
            }
        }
        validate();
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

class EndWindow extends JFrame{
    private EndWindow currentFrame;
    private MainApplication mainFrame;
    private JPanel contentpane;
    private JTextArea textArea;
    private MyImageIcon backgroundImg;
    private int frameWidth = 800, frameHeight  = 600;
    private int totalScore;
    

    public EndWindow(MainApplication pf, int score){
        setTitle("End Page");
        setBounds(500, 250 , frameWidth, frameHeight);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); 
        currentFrame = this;
        mainFrame = pf;
        totalScore = score;
        
        contentpane = (JPanel)getContentPane();
        contentpane.setLayout(new BorderLayout());

        AddComponents();
        validate();
        repaint();
    }

    public void AddComponents(){

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton titleButton = new JButton("Back to Title");
        titleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                dispose();
                new MainApplication();
            }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                mainFrame.dispose();
                dispose();
                System.exit(0);
            }
        });

        buttonPanel.add(titleButton);
        buttonPanel.add(exitButton);

        JPanel textPanel = new JPanel(new FlowLayout());
        JLabel textLabel = new JLabel("Total Score = " + totalScore);
        textLabel.setForeground(Color.WHITE);
        textPanel.add(textLabel);

        textPanel.setBackground(new Color(16, 133, 232));
        buttonPanel.setBackground(new Color(181, 221, 255));
        contentpane.add(textPanel, BorderLayout.NORTH);
        contentpane.add(buttonPanel, BorderLayout.CENTER);
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

