package com.acidopal;

import com.sun.opengl.util.Animator;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_W;
import java.awt.event.MouseListener;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.glu.GLUquadric;
import java.io.IOException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.InputStream;


/**
 * Lamp
 *
 * @author acidopal
 * @since 17 Desember 2020
 */
public class Lamp extends GLCanvas implements GLEventListener, KeyListener, MouseMotionListener  {
    private static final long serialVersionUID = 1L;
    private GLU glu;
    
   // Texture
    private Texture texture;
    private String textureFileName = "metalic.jpg";
    private String textureFileType = ".jpg";
    private float textureTop, textureBottom, textureLeft, textureRight;
    private float rotateX, rotateY;
    private int lastX, lastY;

    //key listener
    private static float angleX = 0.0f; // rotational angle for x-axis in degree
    private static float angleY = 0.0f; // rotational angle for y-axis in degree
    private static float angleZ = 0.0f;
    private static float rotateSpeedX = 0.0f; // rotational speed for x-axis
    private static float rotateSpeedY = 0.0f; // rotational speed for y-axis
    private static float rotateSpeedZ = 0.0f; // rotational speed for y-axis
    private static float rotateSpeedXIncrement = 0.05f; // adjusting x rotational speed
    private static float rotateSpeedYIncrement = 0.05f; // adjusting y rotational speed
    private static float rotateSpeedZIncrement = 0.05f; // adjusting y rotational speed

    public Lamp(GLCapabilities capabilities, int width, int height) {
        addGLEventListener(this);
        addKeyListener(this);
        setFocusable(true);
        addMouseMotionListener(this);
        requestFocus();
    }

    private static GLCapabilities createGLCapabilities() {
        GLCapabilities capabilities = new GLCapabilities();
        capabilities.setRedBits(8);
        capabilities.setBlueBits(8);
        capabilities.setGreenBits(8);
        capabilities.setAlphaBits(8);
        return capabilities;
    }

    public void init(GLAutoDrawable drawable) {
        drawable.setGL(new DebugGL(drawable.getGL()));
        final GL gl = drawable.getGL();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glShadeModel(GL.GL_SMOOTH);

        gl.glClearColor(0f, 0f, 0f, 0f);

        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

        glu = new GLU();

         try {
            texture = TextureIO.newTexture(
                    getClass().getClassLoader().getResource(textureFileName),
                    false, textureFileType);

            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

            TextureCoords textureCoords = texture.getImageTexCoords();
            textureTop = textureCoords.top();
            textureBottom = textureCoords.bottom();
            textureLeft = textureCoords.left();
            textureRight = textureCoords.right();
        } catch (GLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void display(GLAutoDrawable drawable) {
        final GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        setCamera(gl, glu, 1);

        float SHINE_ALL_DIRECTIONS = 3;
        float[] lightPos = {-30, 0, 0, SHINE_ALL_DIRECTIONS};
        float[] lightColorAmbient = {0.2f, 0.2f, 0.2f, 1f};
        float[] lightColorSpecular = {0.8f, 0.8f, 0.8f, 1f};

        // Set light parameters.
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightColorAmbient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightColorSpecular, 0);

        // Enable lighting in GL.
        gl.glEnable(GL.GL_LIGHT1);
        gl.glEnable(GL.GL_LIGHTING);

        // Set material properties.
        float[] rgba = {1f, 1f, 1f};
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 0.5f);
        
        gl.glTranslatef( 0f, 0f, -5.0f );
        gl.glRotatef( angleX, 1.0f, 0.0f, 0.0f );
        gl.glRotatef( angleY, 0.0f, 1.0f, 0.0f );
        gl.glRotatef( angleZ, 0.0f, 0.0f, 1.0f );  

        texture.enable();
        texture.bind();
        gl.glBegin( GL.GL_QUADS );
           base(gl);
           body(gl);
        gl.glEnd();
             machine(gl);
             caseLamp(gl);

        angleX += rotateSpeedX;
        angleY += rotateSpeedY;
        angleZ += rotateSpeedZ;
   
        gl.glFlush();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        final GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
    }

    private void setCamera(GL gl, GLU glu, float distance) {
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        float widthHeightRatio = (float) getWidth() / (float) getHeight();
        glu.gluPerspective(45, widthHeightRatio, 1, 1000);
        glu.gluLookAt(0, 0, distance, 0, 0, 0, 0, 1, 0);

        // Change back to model view matrix.
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    /**
     * Starts the JOGL mini demo.
     *
     * @param args Command line args.
     */
    public final static void main(String[] args) {
        GLCapabilities capabilities = createGLCapabilities();
        Lamp canvas = new Lamp(capabilities, 800, 500);
        JFrame frame = new JFrame("Lampp");
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.setSize(1080, 1080);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        canvas.requestFocus();

        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(new Runnable() {

                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        animator.start();
    }

    private void base(GL gl) {
        // Front
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.6f, -1.0f, 0.6f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.6f, -1.0f, 0.6f); 
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.6f, -0.8f, 0.6f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.6f, -0.8f, 0.6f);  

        // BACK
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-0.6f, -1.0f, -0.6f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.6f, -1.0f, -0.6f);  
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(0.6f, -0.8f, -0.6f);   
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.6f, -0.8f, -0.6f); 

        // top
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.6f, -0.8f, -0.6f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.6f, -0.8f, 0.6f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.6f, -0.8f, 0.6f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.6f, -0.8f, -0.6f);

        //bottom
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.6f, -1.0f, -0.6f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(0.6f, -1.0f, -0.6f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.6f, -1.0f, 0.6f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-0.6f, -1.0f, 0.6f);

        // RIGHT
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.6f, -1.0f, -0.6f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(0.6f, -1.0f, 0.6f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(0.6f, -0.8f, 0.6f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.6f, -0.8f, -0.6f);

        // LEFT
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.6f, -1.0f, 0.6f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-0.6f, -1.0f, -0.6f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-0.6f, -0.8f, -0.6f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.6f, -0.8f, 0.6f);
    }

    private void machine(GL gl) {
        final float cylinderRadius = 0.5f;
        final float cylinderHeight = 1f;
        final int cylinderSlices = 30;
        final int cylinderStacks = 50;
        GLUquadric body = glu.gluNewQuadric();
        glu.gluQuadricTexture(body, false);
//        glu.gluQuadricTexture(body, false);
        glu.gluQuadricDrawStyle(body, GLU.GLU_FILL);
        glu.gluQuadricNormals(body, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(body, GLU.GLU_OUTSIDE);
        gl.glTranslatef(0, 1, -cylinderHeight / 2);
        glu.gluDisk(body, 0, cylinderRadius, cylinderSlices, 2);
        glu.gluCylinder(body, cylinderRadius, cylinderRadius, cylinderHeight, cylinderSlices, cylinderStacks);
        gl.glTranslatef(0, 0, cylinderHeight);
        glu.gluDisk(body, 0, cylinderRadius, cylinderSlices, 2);
        glu.gluDeleteQuadric(body);
        gl.glTranslatef(0, 0, -cylinderHeight / 2);
    }

    private void caseLamp(GL gl) {
        final float cylinderRadius = 1f;
        final float cylinderHeight = 0.8f;
        final int cylinderSlices = 50;
        final int cylinderStacks = 10;
        GLUquadric body = glu.gluNewQuadric();
        glu.gluQuadricTexture(body, true);
        glu.gluQuadricDrawStyle(body, GLU.GLU_FILL);
        glu.gluQuadricNormals(body, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(body, GLU.GLU_OUTSIDE);
        gl.glTranslatef(0, 0, 0.1f);
        glu.gluDisk(body, 0, cylinderRadius, cylinderSlices, 2);
        glu.gluCylinder(body, cylinderRadius, cylinderRadius, cylinderHeight, cylinderSlices, cylinderStacks);
        gl.glTranslatef(0, 0, cylinderHeight);
        glu.gluDeleteQuadric(body);
    }
      
    private void body(GL gl) {
        // FRONT
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.1f, -1f, 0f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.1f, -1f, 0f); 
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.1f, 1f, 0f);  
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.1f, 1f, 0f); 

        //BACK
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-0.1f, -1f, -0.2f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.1f, -1f, -0.2f); 
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(0.1f, 1f, -0.2f); 
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.1f, 1f, -0.2f); 

        //TOP
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.1f, 1f, -0.2f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.1f, 1f, 0.01f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.1f, 1f, 0.01f);  
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.1f, 1f, -0.2f); 

        //BOTTOM
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-0.1f, -1f, -0.2f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(0.1f, -1f, -0.2f); 
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(0.1f, -1f, 0.01f);  
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-0.1f, -1f, 0.01f);

        //RIGHT
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(0.1f, -1f, -0.2f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(0.1f, -1f, 0f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(0.1f, 1f, 0f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(0.1f, 1f, -0.2f);
        
        //LEFT
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-0.1f, -1f, 1f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-0.1f, -1f, 1f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-0.1f, 1f, 1f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-0.1f, 1f, 1f);
    }

    public void mouseDragged(MouseEvent e) {
        rotateX += e.getX() - lastX;
        rotateY += e.getY() - lastY;
        lastX = e.getX();
        lastY = e.getY();
    }

    public void mouseMoved(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int keyCode = ke.getKeyCode();
        switch (keyCode) {
            case VK_UP:
                rotateSpeedX -= rotateSpeedXIncrement;
                break;
            case VK_DOWN:
                rotateSpeedX += rotateSpeedXIncrement;
                break;
            case VK_LEFT:
                rotateSpeedY -= rotateSpeedYIncrement;
                break;
            case VK_RIGHT:
                rotateSpeedY += rotateSpeedYIncrement;
                break;
            case VK_W:
                rotateSpeedZ -= rotateSpeedZIncrement;
                break;
            case VK_S:
                rotateSpeedZ += rotateSpeedZIncrement;
                break;
            case VK_SHIFT :
                rotateSpeedY = 0;
                rotateSpeedX = 0;
                rotateSpeedZ = 0;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
}