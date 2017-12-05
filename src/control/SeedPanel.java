package control;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class SeedPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

  public static final Color OBJECT_COLOR = Color.red;
  public static final Color BACKGROUND_COLOR = Color.blue;
  
  private BufferedImage image;
  private double zoom;
  private int xOffset;
  private int yOffset;
  private Point previousMouse;
  private int buttonPressed;
  private BufferedImage seedImage;
  private int brushSize = 13;
  public SeedPanel(BufferedImage image) {
    zoom = 0.5;
    this.image = image;
    xOffset = (int) (-image.getWidth()*zoom/2 + 640);
    yOffset = (int) (-image.getHeight()*zoom/2 + 480);
    seedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.addMouseWheelListener(this);
  }
  
  public BufferedImage getSeedImage() {
    return seedImage;
  }
  
  @Override
  public void paintComponent(Graphics g) {
    g.setColor(Color.orange);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.drawImage(image, xOffset, yOffset, (int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom), null);
    g.drawImage(seedImage, xOffset, yOffset, (int)(seedImage.getWidth()*zoom), (int)(seedImage.getHeight()*zoom), null);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if( buttonPressed == MouseEvent.BUTTON2 ) {
      int deltax = e.getX() - previousMouse.x;
      int deltay = e.getY() - previousMouse.y;
      xOffset += deltax;
      yOffset += deltay;
      previousMouse = e.getPoint();
      repaint();
    }
    else {
      int brush = Math.max((int) (brushSize/zoom * 0.9), 2);
      Graphics g = seedImage.getGraphics();
      if( buttonPressed == MouseEvent.BUTTON1 ) {
        g.setColor(OBJECT_COLOR);
      }
      else {
        g.setColor(BACKGROUND_COLOR);
      }
      g.fillOval((int) ((e.getX() - xOffset) / zoom - brush/2), (int) ((e.getY() - yOffset) / zoom - brush/2), brush, brush);
      g.dispose();
      repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    
  }

  @Override
  public void mouseExited(MouseEvent e) {
    
  }

  @Override
  public void mousePressed(MouseEvent e) {
    previousMouse = e.getPoint();
    buttonPressed = e.getButton();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    previousMouse = null;
    buttonPressed = 0;
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    double oldZoom = zoom;
    if( e.getWheelRotation() == 1 ) {
      zoom = zoom * 0.9;
    }
    else {
      zoom = zoom*1.001 + 0.1;
    }
    xOffset -= 1.0*(e.getX() - xOffset) / oldZoom * (zoom - oldZoom);
    yOffset -= 1.0*(e.getY() - yOffset) / oldZoom * (zoom - oldZoom);
    repaint();
  }
  
  
}
