package fractal;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FractalExplorer {

    private final int displaySize;

    private final JImageDisplay display;

    private FractalGenerator fractal;

    JButton resetButton;

    JButton saveButton;

    JComboBox<FractalGenerator> myComboBox;

    int rowsRemaining;

    private final Rectangle2D.Double range;

    public FractalExplorer(int size) {

        displaySize = size;

        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        display = new JImageDisplay(displaySize, displaySize);
    
    }

    public void createAndShowGUI() {
        display.setLayout(new BorderLayout());
        JFrame myFrame = new JFrame("Fractal Explorer");
        myFrame.add(display, BorderLayout.CENTER);

        resetButton = new JButton("Reset");
        saveButton = new JButton("Save");

        myComboBox = new JComboBox<>();
        myComboBox.addItem(new Mandelbrot());
        myComboBox.addItem(new Tricorn());
        myComboBox.addItem(new BurningShip());

        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myFrame.add(myPanel, BorderLayout.NORTH);

        ButtonHandler buttonHandler = new ButtonHandler();
        MouseHandler click = new MouseHandler();

        resetButton.addActionListener(buttonHandler);
        saveButton.addActionListener(buttonHandler);
        myComboBox.addActionListener(buttonHandler);
        display.addMouseListener(click);


        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myFrame.add(myBottomPanel, BorderLayout.SOUTH);


        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.pack();
        myFrame.setVisible(true);
        myFrame.setResizable(false);
    }

    private void drawFractal() {
        enableUI(false);
        rowsRemaining = displaySize;
        for (int i = 0; i < displaySize; i++) {
            FractalWorker worker = new FractalWorker(i);
            worker.execute();
        }
    }

    private class ButtonHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String action  = e.getActionCommand();
            if (e.getSource() instanceof JComboBox) {
                JComboBox<FractalGenerator> mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem(); // меняем текущий фрактал на тот который пришел
                assert fractal != null;
                fractal.getInitialRange(range);
                drawFractal();
            } else if (action.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            } else if (action.equals("Save")) {
                JFileChooser myFileChooser = new JFileChooser();
                FileFilter extensionFilter =
                        new FileNameExtensionFilter("PNG Images", "png");
                myFileChooser.setFileFilter(extensionFilter);

                myFileChooser.setAcceptAllFileFilterUsed(false);

                int userSelection = myFileChooser.showSaveDialog(display);

                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    File file = myFileChooser.getSelectedFile();

                    try {
                        BufferedImage displayImage = display.getImage();
                        ImageIO.write(displayImage, "png", file);
                    }

                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(display,
                                exception.getMessage(), "Cannot Save Image",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (rowsRemaining ==0){  // в момент отрисовки фрактала кнопки блок
                int x = e.getX();
                double xCoord = FractalGenerator.getCoord(range.x,
                        range.x + range.width, displaySize, x);

                int y = e.getY();
                double yCoord = FractalGenerator.getCoord(range.y,
                        range.y + range.height, displaySize, y);


                fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);


                drawFractal();
            }
        }
    }

    private class FractalWorker extends SwingWorker<Object, Object> {

        private  int y;
        private int[] line;

        public FractalWorker(int y) {
            this.y = y;
        }

        @Override
        protected Object doInBackground() throws Exception {
            line = new int[displaySize];
            for (int x=0; x<displaySize; x++){

                    double xCoord = FractalGenerator.getCoord(range.x,
                            range.x + range.width, displaySize, x);
                    double yCoord = FractalGenerator.getCoord(range.y,
                            range.y + range.height, displaySize, y);

                    int iteration = fractal.numIterations(xCoord, yCoord);

                    if (iteration == -1){
                        line[x] = 0;
                    }

                    else {

                        float hue = 0.7f + iteration / 200f;
                        int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);

                        line[x] = rgbColor;
                    }
            }
            return null;
        }

        @Override
        protected void done() {
            for (int i = 0; i < displaySize; i++) {
                display.drawPixel(i, y, line[i]);
            }
            display.repaint(0,0, y, displaySize, 1);
            rowsRemaining --;
            if (rowsRemaining == 0){
                enableUI(true);
            }
            super.done();
        }

    }


    private void enableUI(boolean value){
        resetButton.setEnabled(value);
        saveButton.setEnabled(value);
        myComboBox.setEnabled(value);
    }
    public static void main(String[] args) {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}