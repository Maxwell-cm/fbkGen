
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Maxwell
 */
public class FeedbackPanel extends JFrame {

    private String title = "Feedback Generator";
    //Meun
    private final JMenuBar mBar = new JMenuBar();
    private JMenu menu = new JMenu("Menu");
    private JMenuItem newMItem = new JMenuItem("New");
    private JMenuItem save = new JMenuItem("Save");
    private JMenuItem exit = new JMenuItem("Exit");
    private JMenuItem help = new JMenuItem("Help");
    private JMenuItem about = new JMenuItem("About");
    //North Panel
    private String warning1 = "Warning: The data produced are random generate, not real data !";
    private String warning2 = "Do not use for any real life application!!!";
    private JPanel nPanel = new JPanel();
    private JLabel warn1Label = new JLabel(warning1);
    private JLabel warn2Label = new JLabel(warning2);
    private JLabel spaceLabel = new JLabel(" ");
    //North Panel - Feedback number Panel
    private JPanel fbkNumPane = new JPanel();
    private JLabel fbkNumLable = new JLabel("Number of feedbacks: ");
    private JTextField fbkNumTextF = new JTextField(5);
    //North Panel - Button Panel
    private JPanel btnPanel = new JPanel();
    private JButton addBtn = new JButton("Add Question");
    private JButton genBtn = new JButton("Generate");
    private JButton rmvBtn = new JButton("Remove");
    //Center Panel   
    private JPanel cPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(cPanel);
    //Eastst panel
    private JPanel fbkPane = new JPanel();
    private JScrollPane fbkScrollPane = new JScrollPane(fbkPane);
    //some attributes
    private Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private ArrayList<QuestionUnit> questionUnits = new ArrayList();
    private ArrayList<String> feedbacks;
    private FbkGenerator generator;
    private final String defaultFileName = "Generated_Feedbacks";
    private String fileName = "Generated_Feedbacks";
    private String abtMsg = "Develop by Maxwell\n\n"
            + warning1 + "\n" + warning2 + "\n" + warning2
            + "\n\nSpeical thanks to COMP S201 S311";

    public FeedbackPanel() {
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        //set menu
        setJMenuBar(mBar);
        mBar.add(menu);
        menu.add(newMItem);
        menu.add(save);
        menu.addSeparator();
        menu.add(help);
        menu.add(about);
        menu.addSeparator();
        menu.add(exit);
        //North Panel
        add(nPanel, BorderLayout.NORTH);
        nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.Y_AXIS));
        nPanel.add(warn1Label);
        nPanel.add(warn2Label);
        warn2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        warn1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        nPanel.add(spaceLabel);
        //North Panel - Feedback number Panel
        fbkNumPane.add(fbkNumLable);
        fbkNumPane.add(fbkNumTextF);
        fbkNumTextF.setText("10");
        nPanel.add(fbkNumPane);
        //North Panel - Button panel
        nPanel.add(btnPanel);
        btnPanel.add(addBtn);
        btnPanel.add(genBtn);
        btnPanel.add(rmvBtn);
        //Center Panel
        add(scrollPane, BorderLayout.CENTER);
        cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));
        addQuestion();
        //East panel
        add(fbkScrollPane, BorderLayout.EAST);
        //tooltips
        newMItem.setToolTipText("Clean all and retart again");
        save.setToolTipText("Default save location is user\\Downloads");
        fbkNumTextF.setToolTipText("Set the number of feedbacks you want, default is 10");
        addBtn.setToolTipText("Add a new Question Box at the Bottom");
        genBtn.setToolTipText("Generate feedbacks for review, not yet save to Excel");
        rmvBtn.setToolTipText("Remove the bottom Question Box");
        //Listeners, save listener only applies when at least gen once
        newMItem.addActionListener(e -> makeNew());
        help.addActionListener(e -> help());
        about.addActionListener(e
                -> JOptionPane.showMessageDialog(null, abtMsg, "About", INFORMATION_MESSAGE)
        );
        exit.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(null, "Exit and close?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        addBtn.addActionListener(e -> addQuestion());
        genBtn.addActionListener(e -> genAll());
        rmvBtn.addActionListener(e -> remove());

        pack();
        setFrameLocation();
    }

    //for genBtn
    private void genAll() {
        //check parameters
        String numStr = fbkNumTextF.getText();
        int fbkNum = 10;
        String result = allVaildResults();
        if (!numStr.isBlank()) {
            try {
                int num = Integer.parseInt(numStr);
                if (num > 1) {
                    fbkNum = num;
                }
            } catch (NumberFormatException nfe) {
                result = result + "\nNumber of feedbacks must be number";
            }
        }
        //generate
        if (result.isBlank()) {
            //add listener to save btn for first generate
            if (feedbacks == null) {
                save.addActionListener(e -> {
                    String msg = generator.toExcel(fileName);
                    JOptionPane.showMessageDialog(null, msg);

                    if (msg.contains("Unable")) {
                        fileSelect();
                        if (!fileName.equals(defaultFileName)) {
                            msg = generator.toExcel(fileName);
                        }
                        JOptionPane.showMessageDialog(null, msg);
                    }                    
                });
            }
            //get input paras
            ArrayList<Map> qMaps = new ArrayList();
            questionUnits.stream().forEach(k -> qMaps.add(k.gen()));
            //pass para to generate
            generator = new FbkGenerator(qMaps, fbkNum);
            feedbacks = generator.getFeedbacks();
            //setup fbkPanel
            if(generator.isDone()){
                fbkPane.removeAll();
            fbkPane.setLayout(new BoxLayout(fbkPane, BoxLayout.Y_AXIS));
            String fbkMsg = "The data may not be aligned here, but will be fine when save to Excel. ";
            JLabel fbkMsgLabel = new JLabel(fbkMsg);
            fbkPane.add(fbkMsgLabel);
            //show feedbacks to fbkPanel
            feedbacks.stream().map(str -> str.split(":"))
                    .map(strArray -> Arrays.stream(strArray).collect(Collectors.joining("   ")))
                    .map(str -> new JLabel(str))
                    .forEach(label -> fbkPane.add(label));
            fbkPane.repaint();
            fbkPane.revalidate();
            pack();
            }else{
                JOptionPane.showMessageDialog(null, "Threads time out !!", "Unable to Generate!", ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, result, "Unable to Generate!", ERROR_MESSAGE);
        }
    }

    //for remove menuBtn
    private void remove() {
        if (questionUnits.size() > 1) {
            cPanel.remove(questionUnits.get(questionUnits.size() - 1));
            questionUnits.remove(questionUnits.size() - 1);
            pack();
        }
    }

    //for new menuItem
    private void makeNew() {
        int choice = JOptionPane.showConfirmDialog(null, "All data will be loss,do you want to start again?",
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            cPanel.removeAll();
            fbkPane.removeAll();
            questionUnits.clear();
            feedbacks = null;
            //disattach saveBtn listener because of no feedbacks can be save
            ActionListener[] listeners = save.getActionListeners();
            for (ActionListener listener : listeners) {
                save.removeActionListener(listener);
            }
            //re-build default components
            addQuestion();
            fileName = defaultFileName;
            fbkNumTextF.setText("10");
            pack();
        }
    }

    //for help menuItem
    private void help() {
        //get and display the readMe.txt
        try (var br = new BufferedReader(new FileReader("readMe.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Help", INFORMATION_MESSAGE);
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(null, "Unable to locate readMe.txt\nNo help : D\nPlease refer to readMe", "Help", ERROR_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, "Unknown IO error\nNo help : D\nPlease refer to readMe", "Help", ERROR_MESSAGE);
        }
    }

    //for saveBtn listener
    private void fileSelect() {
        //set the fileName String by JFileChooser
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            fileName = file.getPath() + "\\" + fileName;
        }
    }

    //for addBtn
    private void addQuestion() {
        //set empty result for the first box
        String result = "";
        //check vaild for existing boxes
        if (!questionUnits.isEmpty()) {
            result = allVaildResults();
        }// add box is vaild
        if (result.isBlank()) {
            QuestionUnit newQU = new QuestionUnit(questionUnits.size());
            cPanel.add(newQU);
            questionUnits.add(newQU);
            //Limiting the max dimension of cPanel to show scroll
            if (questionUnits.size() < 3) {
                pack();
            } else {
                if (questionUnits.size() == 3) {
                    bounds = cPanel.getBounds();
                    bounds.setSize(bounds.width, (bounds.height + 10));
                    cPanel.setBounds(bounds);
                }
            }
            cPanel.repaint();
            cPanel.revalidate();
        } else {
            JOptionPane.showMessageDialog(null, result
                    + "Try to scroll up and down if you cannot see the last question box",
                    "Unable to add new question!", WARNING_MESSAGE);
        }
    }

    // cehck is all question boxes are vaild, return result msg
    private String allVaildResults() {
        //return the vaild results of each question box
        StringBuilder sb = new StringBuilder();
        for (QuestionUnit qu : questionUnits) {
            String check = qu.vaildResult();
            if (!check.isBlank()) {
                sb.append(qu.getQNumStr()).append("\n").append(check)
                        .append("\n\n");
            }
        }
        return sb.toString();
    }

    //set frame location
    private void setFrameLocation() {
        //determine the location value of the frame
        int x = bounds.x + (bounds.width - this.getWidth()) / 2;
        int y = bounds.y + (bounds.height - this.getHeight()) / 2;
        setLocation(x, y);
    }
}
