import java.util.Scanner;;
import java.io.*;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import static javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.Node;
/*
 * *.
 */
/**
 * 3D beam pattern generator
 */
public class multipleBeams extends Application {

    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    // this is a variable I messed with.  Increase if things run outside the window.
    final double cameraDistance = 1450;
    final Xform moleculeGroup = new Xform();
    private Timeline timeline;
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0 / 24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    double frequency;
    // from input files
    String title = "";
    int nRect;
    int nElements = 0;
    double[] xpos = new double[3000];
    double[] ypos = new double[3000];
    double[] zpos = new double[3000];
    int[] icolor = new int[3000];
    double theta = 0.;
    double phi = 0.;
    int[] phi0 = new int[17000];
    int[] phi1 = new int[17000];;
    int[] th0 = new int[17000];
    int[] th1 = new int[17000];
    double[] lvl0 = new double[17000];
    double[] lvl1 = new double[17000];
    double[] lvl2 = new double[17000];
    double[] lvl3 = new double[17000];
    double maxmag = 0.;
    
    int nBeamFiles;
    String directory;
    String file_prefix;
    String[] beamFiles = new String[100];
    
    private double rtn_freq() {
        return (frequency);
    }

    private void buildScene() {
        root.getChildren().add(world);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        //        cameraXform.ry.setAngle(320.0);
        //        cameraXform.rx.setAngle(40);
        // the following two lines are good for cylinder
        cameraXform.ry.setAngle(180.0);
        cameraXform.rx.setAngle(270.0);
        // the next two lines are best for horizontal line arrays
        cameraXform.ry.setAngle(120.0);
        cameraXform.rx.setAngle(0.0);
    }
    private void getBeamFiles(String filename) {
        // first, try to glean the directory name from the filename
        String[] split_files = utils.split_dir_title(filename);
        System.out.format("%s       %s%n", split_files[0], split_files[1]);
        try{
            Scanner in = new Scanner(new FileReader(filename)); 
            this.nBeamFiles = in.nextInt();
            String dummy = in.nextLine();
            for (int ctr = 0; ctr < nBeamFiles; ctr++) {
                this.beamFiles[ctr] = split_files[0] + in.nextLine();
            }
            in.close();
        }
        catch (FileNotFoundException e)
        {
            this.nBeamFiles = -1;
        }
    }
    private void read_bp(String filename) {
        try{
            Scanner in = new Scanner(new FileReader(filename)); 
            title = in.nextLine();
            nRect = in.nextInt();
            System.out.format(" Reading beampattern file %s, title = %s, # of rectangles = %d%n",filename,title,nRect);
            if(nRect < 0) {  // flag to add array elements, too
                nRect = -nRect;
                nElements = in.nextInt();
                theta = in.nextDouble();
                phi = in.nextDouble();
                frequency = in.nextDouble();
                int array_type = in.nextInt();
            }
            for(int ctr = 0; ctr < nRect; ctr++)             {  // levels in dB 0 to -300
                phi0[ctr] = in.nextInt();
                phi1[ctr] = in.nextInt();
                th0[ctr] = in.nextInt();
                th1[ctr] = in.nextInt();
                lvl0[ctr] = in.nextDouble();   // goes with phi1, th1 - bottom left
                lvl1[ctr] = in.nextDouble();   // goes with phi2, th1 - top left
                lvl2[ctr] = in.nextDouble();   // goes with phi1, th2 - bottom right
                lvl3[ctr] = in.nextDouble();   // goes with phi2, th2 - top right
            }
            if (nElements > 0) {
                double mag;
                for (int ctr = 0; ctr < nElements; ctr++) {
                    xpos[ctr] = in.nextDouble();
                    ypos[ctr] = in.nextDouble();
                    zpos[ctr] = in.nextDouble();
                    icolor[ctr] = in.nextInt();
                    int iap = in.nextInt();
                    mag = Math.sqrt(xpos[ctr]*xpos[ctr] + ypos[ctr]*ypos[ctr] + zpos[ctr]*zpos[ctr]);
                    if (mag > maxmag) maxmag = mag;
                }
            }
            in.close();
        }
        catch (FileNotFoundException e)
        {
            nRect = 0;
        }
    }
    private Color beamColor(int fctr) {
        if (fctr==0 || fctr == 7) {
            return (Color.RED);
        }
        else if(fctr==1 || fctr == 8) {
            return (Color.ORANGE);
        }
        else if(fctr==2  || fctr == 9) {
            return (Color.YELLOW);
        }
        else if(fctr==3  || fctr==10) {
            return (Color.GREEN);
        }
        else if(fctr==4) {
            return (Color.BLUE);
        }
        else if(fctr==5) {
            return (Color.INDIGO);
        }
        else if(fctr==6) {
            return (Color.VIOLET);
        }
        return (Color.RED);
    }
    private void buildAxes() {

        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(480.0, 4, 4);
        final Box yAxis = new Box(4, 480.0, 4);
        final Box zAxis = new Box(4, 4, 480.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);

        for (int fctr = 0; fctr < nBeamFiles; fctr++) {
            read_bp(beamFiles[fctr]);

            float[] xp = new float[4];
            float[] yp = new float[4];
            float[] zp = new float[4];
            double arg_ph0;
            double arg_ph1;
            double arg_th0;
            double arg_th1;
            MeshView[] rect = new MeshView[nRect];
            for (int ctr = 0; ctr < nRect; ctr++){
                // scale to a number betwen 0 and 500
                lvl0[ctr] = (Math.max(lvl0[ctr], -30.)+30)*500./30.;
                lvl1[ctr] = (Math.max(lvl1[ctr], -30.)+30)*500./30.;
                lvl2[ctr] = (Math.max(lvl2[ctr], -30.)+30)*500./30.;
                lvl3[ctr] = (Math.max(lvl3[ctr], -30.)+30)*500./30.;
                // now, create the points arrays for this rectangle
                arg_ph0 = Math.toRadians((double)phi0[ctr]);  // bottom
                zp[0] = (float)Math.sin(arg_ph0);
                zp[2] = zp[0]*(float)lvl2[ctr];
                zp[0] = zp[0]*(float)lvl0[ctr];
                arg_ph1 = Math.toRadians((double)phi1[ctr]);  // top
                zp[1] = (float)Math.sin(arg_ph1);
                zp[3] = zp[1]*(float)lvl3[ctr];
                zp[1] = zp[1]*(float)lvl1[ctr];
                
                arg_th0 = Math.toRadians((double)th0[ctr]);
                arg_th1 = Math.toRadians((double)th1[ctr]);
                xp[0] = (float)(lvl0[ctr]*Math.cos(arg_th0)*Math.cos(arg_ph0));
                xp[1] = (float)(lvl1[ctr]*Math.cos(arg_th0)*Math.cos(arg_ph1));
                xp[2] = (float)(lvl2[ctr]*Math.cos(arg_th1)*Math.cos(arg_ph0));
                xp[3] = (float)(lvl3[ctr]*Math.cos(arg_th1)*Math.cos(arg_ph1));
                
                yp[0] = (float)(lvl0[ctr]*Math.sin(arg_th0)*Math.cos(arg_ph0));
                yp[1] = (float)(lvl1[ctr]*Math.sin(arg_th0)*Math.cos(arg_ph1));
                yp[2] = (float)(lvl2[ctr]*Math.sin(arg_th1)*Math.cos(arg_ph0));
                yp[3] = (float)(lvl3[ctr]*Math.sin(arg_th1)*Math.cos(arg_ph1));
                // finally get to define the rectangles
                rect[ctr] = new MeshView( new Shape3DRectangle(xp, yp, zp)  );
                //rect[ctr].setMaterial(new PhongMaterial(Color.RED));
                rect[ctr].setMaterial(new PhongMaterial(beamColor(fctr)));
                rect[ctr].setCullFace(CullFace.NONE);
                axisGroup.getChildren().add(rect[ctr]);
            }
        }
        if(nElements > 0) {
            Sphere element[] = new Sphere[nElements];
            for (int ctr = 0; ctr < nElements; ctr++){
                element[ctr] = new Sphere(5);
                element[ctr].setTranslateZ(zpos[ctr]/maxmag*200.);
                element[ctr].setTranslateX(xpos[ctr]/maxmag*200.);
                element[ctr].setTranslateY(ypos[ctr]/maxmag*200.);
                if (icolor[ctr]== 2){
                    element[ctr].setMaterial(redMaterial);
                }
                else if(icolor[ctr] == 1){
                    element[ctr].setMaterial(blueMaterial);
                }
                else{
                    element[ctr].setMaterial(greenMaterial);
                }
                axisGroup.getChildren().add(element[ctr]);
            }
            double arg_ph = Math.toRadians(phi);  // bottom
            double z = Math.sin(arg_ph);
            double arg_th = Math.toRadians(theta);
            double x = Math.cos(arg_th)*Math.cos(arg_ph);
            double y = Math.sin(arg_th)*Math.cos(arg_ph);
            //final Box steerArrow = new Box(x*550, y*550, z*550);

        }
        world.getChildren().addAll(axisGroup);
    }
    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * modifierFactor * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
                }
            }
        });
    }

    private void handleKeyboard(Scene scene, final Node root) {
        final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Duration currentTime;
                switch (event.getCode()) {
                    case Z:
                        cameraXform.ry.setAngle(0.0);
                        cameraXform.rx.setAngle(0.0);
                        if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z*.75;
                            camera.setTranslateZ(newZ);
                        }
                        else   {
                            double z = camera.getTranslateZ();
                            double newZ = z/.75;
                            camera.setTranslateZ(newZ);
                        }
                        cameraXform.ry.setAngle(120.0);
                        cameraXform.rx.setAngle(0.0);
                        //cameraXform2.t.setX(0.0);
                        //cameraXform2.t.setY(0.0);
                        break;
                    case O:
                        camera.setTranslateZ(-cameraDistance);
                        break;    
                    case X:
                        if (event.isControlDown()) {
                            if (axisGroup.isVisible()) {
                                axisGroup.setVisible(false);
                            } else {
                                axisGroup.setVisible(true);
                            }
                        }
                        break;
                    case S:
                        if (event.isControlDown()) {
                            if (moleculeGroup.isVisible()) {
                                moleculeGroup.setVisible(false);
                            } else {
                                moleculeGroup.setVisible(true);
                            }
                        }
                        break;
                    case SPACE:
                        if (timelinePlaying) {
                            timeline.pause();
                            timelinePlaying = false;
                        } else {
                            timeline.play();
                            timelinePlaying = true;
                        }
                        break;
                    case UP:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case DOWN:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case RIGHT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                        }
                        break;
                    case LEFT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                        }
                        break;
                }
            }
        });
    }
    
        @Override
    public void start(Stage primaryStage) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Input File Name for Multiple-beam display:");
        String inputfile = sc.nextLine();
        // read inputs from file
        getBeamFiles(inputfile);
        if(this.nBeamFiles >= 0) {
            buildScene();
            buildCamera();
            buildAxes();
            Scene scene = new Scene(root, 2048, 1024, true);
            scene.setFill(Color.GREY);
            handleKeyboard(scene, world);
            handleMouse(scene, world);
    
            primaryStage.setTitle("3D beam pattern for Frequency " + (int)frequency + " Hz");
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
    
            scene.setCamera(camera);
        }
        else {
            System.out.println(" No Input file found, or error in input file");
        }

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main() {
        String[] args = {" "};
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}