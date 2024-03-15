
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Maxwell
 */
public class QuestionUnit extends JPanel {
    //varibales share across methods
    private JTextField qTextF = new JTextField(40);
    private JTextArea ansTextA = new JTextArea(5, 40);
    private JTextField ratioTextF = new JTextField(10);
    private JTextField maxAnsTextF = new JTextField(5);
    private JLabel qNumLabel = new JLabel();
    //generate a question box
    public QuestionUnit(int qNum) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //Question Number
        qNumLabel.setText("Question " + (qNum + 1) + " :");
        add(qNumLabel);
        //Question entry
        JPanel qPane = new JPanel();
        JLabel qLabel = new JLabel("Questions: ");
        qPane.add(qLabel);
        qPane.add(qTextF);
        add(qPane);
        //Answer Entries
        JPanel ansPane = new JPanel();
        JLabel ansLabel = new JLabel("Answers: ");
        String guide = "Ans1\nAns2\nAns3\nAns4";
        ansTextA.setText(guide);
        ansPane.add(ansLabel);
        ansPane.add(ansTextA);
        add(ansPane);
        //Ratio and Max num of answer entries
        JPanel ratioPane = new JPanel();
        JLabel ratioLabel = new JLabel("Answer ratio: ");
        JLabel maxAnsLabel = new JLabel("   Max number of answers: ");
        maxAnsTextF.setText("1");
        ratioPane.add(ratioLabel);
        ratioPane.add(ratioTextF);
        ratioPane.add(maxAnsLabel);
        ratioPane.add(maxAnsTextF);
        add(ratioPane);
        add(new JLabel("    "));
        //tooltips
         qTextF.setToolTipText("Set the Question/tags for question here.");
         ansTextA.setToolTipText("Set answers here,one answer per line.");
         ratioTextF.setToolTipText("The answer ratio,ex: 4:2:3,    Leave Blank to use default ratio");
         maxAnsTextF.setToolTipText("If the question allowe more than one answers, state the max number.");
         //Listeners
         ansTextA.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (ansTextA.getText().equals(guide)) {
                    ansTextA.setText("");
                }
            }
        });
    }
    //return vaildation result of the box
    public String vaildResult() {
        StringBuilder sb = new StringBuilder();
        //check question
        if (qTextF.getText().isBlank()) {
            sb.append("Question cannot be empty!").append("\n");
        }//check answers
        if (ansTextA.getText().isBlank()) {
            sb.append("Answers cannot be empty!").append("\n");
        } else {//check ratio, only if answers have no problem
            String ans[] = ansTextA.getText().split("\n");
            if (!ratioTextF.getText().isBlank()) {
                String ratio[] = ratioTextF.getText().split(":");
                if (ratio.length != ans.length) {
                    sb.append("Ratio should be equals to the number of answers!\nor leave blank to use default.")
                            .append("\n").append("ex: 3 answers = 3:2:4").append("\n");
                }
            }//check max expecting answers of each feedback
            if (!maxAnsTextF.getText().isBlank()) {
                try {
                    int maxAns = Integer.parseInt(maxAnsTextF.getText().trim());
                    if (maxAns > ans.length) {
                        sb.append("Max number of answers cannot exceeds number of answers!").append("\n");
                    }
                } catch (NumberFormatException nfe) {
                    sb.append("Max number of answers must be a number!").append("\n");
                }
            }
        }
        return sb.toString();
    }
    //gather and return input data,only call after box data vaildated
    public Map gen() {
        Map<String, String> questionMap = new HashMap<>();
            questionMap.put("question", qTextF.getText().trim());
            questionMap.put("answer", ansTextA.getText().trim());
            questionMap.put("ratio", ratioTextF.getText().trim());
            questionMap.put("maxAns", maxAnsTextF.getText().trim());
        return questionMap;
    }
    //return the title String of the box
    public String getQNumStr(){
        return qNumLabel.getText();
    }
}
