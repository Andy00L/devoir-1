package lindenmayer;

import java.awt.geom.Rectangle2D;
import java.io.FileReader;
import java.io.PrintWriter;
import org.json.JSONObject;
import org.json.JSONTokener;

public class LindenmayerApp {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar lindenmayer.jar <fichier.json> <iterations>");
            System.exit(1);
        }

        try {
            String jsonFile = args[0];
            int iterations = Integer.parseInt(args[1]);

            JSONObject config = new JSONObject(new JSONTokener(new FileReader(jsonFile)));

            LSystem lsystem = new LSystem();
            MockTurtle mockTurtle = new MockTurtle();
            lsystem.initFromJson(config, mockTurtle);

            PrintWriter out = new PrintWriter(System.out);
            EPSTurtle epsTurtle = new EPSTurtle(out);
            epsTurtle.setUnits(mockTurtle.getUnitStep(), mockTurtle.getUnitAngle());
            epsTurtle.init(mockTurtle.getPosition(), mockTurtle.getAngle());

            out.println("%!PS-Adobe-3.0 EPSF-3.0");
            out.printf("%%%%Title: (%s)\n", jsonFile);
            out.println("%%Creator: (" + LindenmayerApp.class.getName() + ")");
            out.println("%%BoundingBox: (atend)");
            out.println("%%EndComments");

            Rectangle2D bbox = lsystem.tell(epsTurtle, lsystem.getAxiom(), iterations);
            epsTurtle.finishDrawing();

            out.println("%%Trailer");
            out.printf("%%%%BoundingBox: %d %d %d %d\n",
                    (int)bbox.getMinX(), (int)bbox.getMinY(),
                    (int)bbox.getMaxX(), (int)bbox.getMaxY());
            out.println("%%EOF");

            out.close();

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}