import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MusicPlayerApplication {

    public static void main(String[] args) {

        // ================= FRAME =================

        JFrame frame = new JFrame("Music Player");

// Window Icon
        ImageIcon icon = new ImageIcon("music.png");
        frame.setIconImage(icon.getImage());

// Menu Bar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");

        JMenuItem openItem = new JMenuItem("Open Song");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem aboutItem = new JMenuItem("About");

// Add menu items
        fileMenu.add(openItem);
        fileMenu.addSeparator();     // Separator line
        fileMenu.add(exitItem);

        helpMenu.add(aboutItem);

// Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

// Set menu bar
        frame.setJMenuBar(menuBar);

// Frame settings
        frame.setSize(750, 500);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // ================= TITLE =================

        JLabel title = new JLabel("MUSIC PLAYER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD,30));
        title.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));

        // ================= SONG LABEL =================

        JLabel songLabel = new JLabel("No song selected",SwingConstants.CENTER);
        songLabel.setFont(new Font("Arial",Font.PLAIN,18));

        // ================= PROGRESS BAR =================

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(650, 30));
        progressBar.setValue(0);
        progressBar.setStringPainted(false);

        // ================= TIME LABEL =================

        JLabel timeLabel = new JLabel("00:00 / 00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ================= VOLUME =================

        JLabel volumeLabel = new JLabel("Volume");

        JSlider volumeSlider = new JSlider(0,100,80);

        // ================= STATUS =================

        JLabel statusLabel = new JLabel("Status : Ready",SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial",Font.BOLD,16));

        // ================= BUTTONS =================

        JButton chooseButton = new JButton("Choose Song");
        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        JButton resumeButton = new JButton("Resume");
        JButton stopButton = new JButton("Stop");

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(chooseButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(stopButton);

        // ================= VARIABLES =================

        File[] selectedFile = new File[1];
        Clip[] clip = new Clip[1];
        long[] clipPosition = new long[1];
        Timer[] timer = new Timer[1];

        // ================= CHOOSE SONG =================

        chooseButton.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();

            // Show only WAV files
            chooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));

            int result = chooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {

                selectedFile[0] = chooser.getSelectedFile();

                songLabel.setHorizontalAlignment(SwingConstants.CENTER);
                songLabel.setText("Selected Song: " + selectedFile[0].getName());

                statusLabel.setText("Status : Song Selected");
                statusLabel.setForeground(Color.BLUE);

                try {

                    AudioInputStream audio =
                            AudioSystem.getAudioInputStream(selectedFile[0]);

                    Clip tempClip = AudioSystem.getClip();
                    tempClip.open(audio);

                    long totalSeconds = tempClip.getMicrosecondLength() / 1_000_000;

                    String totalTime = String.format("%02d:%02d",
                            totalSeconds / 60,
                            totalSeconds % 60);

                    timeLabel.setText("00:00 / " + totalTime);

                    tempClip.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                progressBar.setValue(0);

                playButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
            }

        });

        // ================= MENU ACTIONS =================

// Open Song menu
        openItem.addActionListener(e -> chooseButton.doClick());

// Exit menu
        exitItem.addActionListener(e -> System.exit(0));

// About menu
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        frame,
                        "Music Player Application\n\n" +
                                "Developed by Sathwika\n" +
                                "Technology: Core Java, Swing\n\n" +
                                "Features:\n" +
                                "• Choose WAV Song\n" +
                                "• Play\n" +
                                "• Pause\n" +
                                "• Resume\n" +
                                "• Stop\n" +
                                "• Progress Bar\n" +
                                "• Timer\n" +
                                "• Volume Control",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        // ================= PLAY =================

        playButton.addActionListener(e->{

            try{

                if(selectedFile[0]==null){

                    JOptionPane.showMessageDialog(frame,
                            "Choose a WAV file first.");

                    return;
                }

                if(clip[0]!=null){

                    clip[0].stop();

                    clip[0].close();

                }

                AudioInputStream audio=
                        AudioSystem.getAudioInputStream(selectedFile[0]);

                clip[0]=AudioSystem.getClip();

                clip[0].open(audio);
                if (clip[0].isControlSupported(FloatControl.Type.MASTER_GAIN)) {

                    FloatControl volume =
                            (FloatControl) clip[0].getControl(FloatControl.Type.MASTER_GAIN);

                    volumeSlider.addChangeListener(event -> {

                        int value = volumeSlider.getValue();

                        float min = volume.getMinimum();
                        float max = volume.getMaximum();

                        float gain = min + (max - min) * value / 100.0f;

                        volume.setValue(gain);

                    });

                }

                clip[0].start();
                timer[0] = new Timer(100, event -> {

                    if (clip[0] != null && clip[0].isOpen()) {

                        long current = clip[0].getMicrosecondPosition();
                        long total = clip[0].getMicrosecondLength();

                        int progress = (int)((current * 100) / total);
                        progressBar.setValue(progress);

                        // Current Time
                        long currentSec = current / 1_000_000;
                        long totalSec = total / 1_000_000;

                        String currentTime = String.format("%02d:%02d",
                                currentSec / 60,
                                currentSec % 60);

                        String totalTime = String.format("%02d:%02d",
                                totalSec / 60,
                                totalSec % 60);

                        timeLabel.setText(currentTime + " / " + totalTime);

                        if (!clip[0].isRunning()) {
                            timer[0].stop();
                        }

                    }

                });

                timer[0].start();

                statusLabel.setText("Status : Playing");
                statusLabel.setForeground(new Color(0, 150, 0));
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(true);

            }

            catch(Exception ex){

                ex.printStackTrace();

            }

        });

        // ================= PAUSE =================

        pauseButton.addActionListener(e->{

            if(clip[0]!=null && clip[0].isRunning()){

                clipPosition[0]=clip[0].getMicrosecondPosition();

                clip[0].stop();
                if (timer[0] != null) {
                    timer[0].stop();
                }

                statusLabel.setText("Status : Paused");
                statusLabel.setForeground(Color.ORANGE);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);

            }

        });

        // ================= RESUME =================

        resumeButton.addActionListener(e->{

            if(clip[0]!=null){

                clip[0].setMicrosecondPosition(clipPosition[0]);

                clip[0].start();
                if (timer[0] != null) {
                    timer[0].start();
                }

                statusLabel.setText("Status : Playing");
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);

            }

        });

        // ================= STOP =================

        stopButton.addActionListener(e->{

            if(clip[0]!=null){

                clip[0].stop();
                if (timer[0] != null) {
                    timer[0].stop();
                }

                clip[0].setMicrosecondPosition(0);

                progressBar.setValue(0);

                statusLabel.setText("Status : Stopped");
                statusLabel.setForeground(Color.RED);
                playButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);

                timeLabel.setText("00:00 / 00:00");

            }

        });

        // ================= CENTER PANEL =================

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

// Create volume panel first
        JPanel volumePanel = new JPanel(new BorderLayout());

        volumePanel.add(volumeLabel, BorderLayout.WEST);
        volumePanel.add(volumeSlider, BorderLayout.CENTER);

// Add components
        centerPanel.add(songLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        centerPanel.add(progressBar);
        centerPanel.add(Box.createVerticalStrut(15));

        centerPanel.add(timeLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        centerPanel.add(volumePanel);
        centerPanel.add(Box.createVerticalStrut(15));

        centerPanel.add(statusLabel);
        // ================= ADD COMPONENTS =================

        frame.add(title,BorderLayout.NORTH);
        frame.add(centerPanel,BorderLayout.CENTER);
        frame.add(buttonPanel,BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}