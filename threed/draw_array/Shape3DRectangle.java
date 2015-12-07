import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;


public class Shape3DRectangle extends TriangleMesh {

    public Shape3DRectangle(float[] x, float[] y, float[] z){
        float[] points = { x[0], y[0], z[0], x[1], y[1], z[1],
                           x[2], y[2], z[2], x[3], y[3], z[3] };
        float[] texCoords = { 1, 1, // idx t0
                  1, 0, // idx t1
                    0, 1 // idx t2
                   , 0, 0  // idx t3
        };
        //int[] faces = {
        //            2, 3, 0, 2, 1, 0,
         //           2, 3, 1, 0, 3, 1
        //};
        int[] faces = {
                  2, 2, 1, 1, 0, 0,
                    2, 2, 3, 3, 1, 1
            };

        this.getPoints().setAll(points);
        this.getTexCoords().setAll(texCoords);
        this.getFaces().setAll(faces);
    }
}