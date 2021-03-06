import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

// drag the mouse over the rectangle to rotate it.
public class rect3d extends Application {

    double anchorX, anchorY, anchorAngle;

    private PerspectiveCamera addCamera(Scene scene) {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
        scene.setCamera(perspectiveCamera);
        return perspectiveCamera;
    }

    public static void main() {
        String[] args = {"  "};
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        float Wid = 500;
        float Hgt = 500;
        float x[] = { -Wid/2, -Wid/2, Wid/2, Wid/2};
        float y[] = {Hgt/2, -Hgt/2, Hgt/2, -Hgt/2};
        float z[] = {Wid/2,0,0,0};
        final MeshView rect = new MeshView(
            new Shape3DRectangle(x, y, z)
//                new Shape3DRectangle(500, 500)
                        );
        rect.setMaterial(new PhongMaterial(Color.DARKGREEN));
        rect.setRotationAxis(Rotate.Y_AXIS);
        rect.setTranslateX(500);
        rect.setTranslateY(500);
        rect.setCullFace(CullFace.NONE);

        final MeshView rect2 = new MeshView(
            new Shape3DRectangle(x, y, z)
//                new Shape3DRectangle(500, 500)
                        );
// try commenting this line out to see what it's effect is . . .
        rect2.setMaterial(new PhongMaterial(Color.RED));
        rect2.setRotationAxis(Rotate.Y_AXIS);
        rect2.setTranslateX(700);
        rect2.setTranslateY(700);

        rect2.setCullFace(CullFace.NONE);

        final Group root = new Group(rect);
        root.getChildren().add(rect2);
        final Scene scene = new Scene(root, 1000, 1000, true);

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
                anchorAngle = rect.getRotate();
            }
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                rect.setRotate(anchorAngle + anchorX - event.getSceneX());
                rect2.setRotate(anchorAngle + anchorX - event.getSceneX());
            }
        });


        addCamera(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public class Shape3DRectangle extends TriangleMesh {

        public Shape3DRectangle(float[] x, float[] y, float[] z){
            float[] points = { x[0], y[0], z[0], x[1], y[1], z[1],
                               x[2], y[2], z[2], x[3], y[3], z[3] };
            float[] texCoords = { 1, 1, // idx t0
                    1, 0, // idx t1
                    0, 1 // idx t2
                   , 0, 0  // idx t3
            };
           int[] faces = {
                    2, 3, 0, 2, 1, 0,
                    2, 3, 1, 0, 3, 1
           };
            this.getPoints().setAll(points);
            this.getTexCoords().setAll(texCoords);
            this.getFaces().setAll(faces);
        }
        public Shape3DRectangle(float Width, float Height) {
            float[] points = {
                    -Width/2,  Height/2, 0, // idx p0
                    -Width/2, -Height/2, 0, // idx p1
                    Width/2,  Height/2, 0 // idx p2
                  , Width/2, -Height/2, 0  // idx p3
            };
            float[] texCoords = {
                    1, 1, // idx t0
                    1, 0, // idx t1
                    0, 1 // idx t2
                   , 0, 0  // idx t3
            };
            /**
             * points:
             * 1      3
             *  -------   texture:
             *  |\    |  1,1    1,0
             *  | \   |    -------
             *  |  \  |    |     |
             *  |   \ |    |     |
             *  |    \|    -------
             *  -------  0,1    0,0
             * 0      2
             *
             * texture[3] 0,0 maps to vertex 2
             * texture[2] 0,1 maps to vertex 0
             * texture[0] 1,1 maps to vertex 1
             * texture[1] 1,0 maps to vertex 3
             *
             * Two triangles define rectangular faces:
             * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
             * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
             */

             // if you use the co-ordinates as defined in the above comment, it will be all messed up
             //            int[] faces = {
            //                    0, 0, 1, 1, 2, 2,
            //                    0, 0, 2, 2, 3, 3
//            };

// try defining faces in a counter-clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 2, 1, 1, 0, 0,
//                    2, 2, 3, 3, 1, 1
//            };

// try defining faces in a clockwise order to see what the difference is.
           int[] faces = {
                    2, 3, 0, 2, 1, 0,
                    2, 3, 1, 0, 3, 1
           };

            this.getPoints().setAll(points);
            this.getTexCoords().setAll(texCoords);
            this.getFaces().setAll(faces);
        }
    }
} 