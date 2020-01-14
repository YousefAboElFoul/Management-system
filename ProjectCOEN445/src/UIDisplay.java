import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIDisplay {

    static JFrame jFrame;
    static JPanel jPanel1;
    static JPanel jPanel2;
    static JTextArea textArea;
    static JScrollPane scrollPane;
    static JTextField inputTextField;
    static JButton submitButton;
    static DefaultCaret caret;

    /**
     * initialises the frame
     * @param myName
     */
    public static void initOutput(String myName) {
        // initialization
        jFrame = new JFrame(myName);

        jPanel1 = new JPanel();
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS));
        jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        jPanel2 = new JPanel();
        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.LINE_AXIS));
        jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        inputTextField = new JTextField();

        submitButton = new JButton("Send");

        // configs
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea();
        textArea.setEditable(false);
        caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);


        scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 250));

        // ------

        JLabel label1 = new JLabel("Conversation Log");
        Font f1 = label1.getFont();
        label1.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));

        JLabel label2 = new JLabel("Please input your Inputs");
        Font f2 = label1.getFont();
        label2.setFont(f2.deriveFont(f2.getStyle() | Font.BOLD));

        jPanel1.add(label1, BorderLayout.CENTER);
        jPanel1.add(scrollPane);
        jPanel2.add(label2);
        jPanel2.add(Box.createHorizontalGlue());
        jPanel2.add(inputTextField);
        jPanel2.add(submitButton);

        jFrame.add(jPanel1, BorderLayout.NORTH);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.setSize(800,400);


        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputUI = UIDisplay.inputTextField.getText();

                if (myName.contains("Client"))
                    UdpClient.getJFrameInput(inputUI);
                else
                    UdpServer.getJFrameInput(inputUI);
            }
        });
    }

}
