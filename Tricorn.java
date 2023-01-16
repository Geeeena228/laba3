package fractal;

import java.awt.geom.Rectangle2D;

public class Tricorn extends FractalGenerator{
    @Override
    public void getInitialRange(Rectangle2D.Double range) {
        range.x = -2;
        range.y = -1.5;
        range.width = 3;
        range.height = 3;
    }

    @Override
    public int numIterations(double x, double y) {

        int iteration = 0;
        double zreal = 0;
        double zimaginary = 0;

        while (zreal * zreal + zimaginary * zimaginary < 4) {
            double zrealNew = zreal * zreal - zimaginary * zimaginary + x;
            double zimaginaryNew = -2 * zreal * zimaginary + y;
            zreal = zrealNew;
            zimaginary = zimaginaryNew;
            iteration += 1;
            if (iteration == MAX_ITERATIONS) {
                return -1;
            }
        }

        return iteration;
    }

    @Override
    public String toString() {
        return "Tricorn";
    }
}
