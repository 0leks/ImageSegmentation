package control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MaxFlow {
  
  private JFrame frame;
  private JPanel bottomPanel;
  private SeedPanel seedPanel;
  private JButton segmentButton;
  private JButton saveSeeds;
  private JTextField fileName;

  BufferedImage image = null;
  public MaxFlow() {
    String file = "tree2.png";
    try {
      image = ImageIO.read(new File(file));
    } catch (IOException e) {
      e.printStackTrace();
    }
    if( image == null ) {
      System.err.println("Failed to load image");
      return;
    }
//    BufferedImage gray = grayscale(image);
//    try {
//      ImageIO.write(gray, "png", new File("gray.png"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    
    frame = new JFrame("Max flow segmentation");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1280, 960);

    bottomPanel= new JPanel();
    
    segmentButton = new JButton("Segment");
    segmentButton.addActionListener((e) -> {
      boolean[][] visited = segment(image);
      BufferedImage originalImage;
      try {
        originalImage = ImageIO.read(new File(file));
        for( int y = 0; y < originalImage.getHeight(); y++ ) {
          for( int x = 0; x < originalImage.getWidth(); x++ ) {
            if( !visited[y][x] ) {
              Color color = new Color(originalImage.getRGB(x, y));
              int grayValue = (color.getBlue() + color.getGreen() + color.getRed()) / 3;
              originalImage.setRGB(x, y, new Color(grayValue, grayValue, grayValue).getRGB());
            }
          }
        }
        try {
          ImageIO.write(originalImage, "png", new File(fileName.getText() + ".png"));
        } catch (IOException e2) {
          e2.printStackTrace();
        }
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });
    bottomPanel.add(segmentButton);
    saveSeeds = new JButton("Save Seeds");
    bottomPanel.add(saveSeeds);
    fileName = new JTextField("filename");
    fileName.setPreferredSize(new Dimension(200, 50));
    bottomPanel.add(fileName);
    
    frame.add(bottomPanel, BorderLayout.SOUTH);
    
    seedPanel = new SeedPanel(image);
    frame.add(seedPanel, BorderLayout.CENTER);
    
    frame.setVisible(true);
  }
  
  private boolean[][] segment(BufferedImage gray) {
    BufferedImage seedImage = seedPanel.getSeedImage();
    int[][] seedArray = convertToArray(seedImage);
    int[][][] intensityArray = convertToArray2(gray);
    for( int y = 0; y < seedArray.length; y++ ) {
      for( int x = 0; x < seedArray[0].length; x++ ) {
        if( seedArray[y][x] == SeedPanel.OBJECT_COLOR.getRGB() ) {
          seedArray[y][x] = 1;
        }
        else if( seedArray[y][x] == SeedPanel.BACKGROUND_COLOR.getRGB() ) {
          seedArray[y][x] = 2;
        }
      }
    }
//    printSeedArray(seedArray);
//    System.err.println("\n\n\n\n\n\n");
//    printIntensityArray(intensityArray);
//    System.err.println("\n\n\n\n\n\n");
    Node[][] nodeArray = new Node[seedArray.length][seedArray[0].length];
    for( int y = 0; y < nodeArray.length; y++ ) {
      for( int x = 0; x < nodeArray[0].length; x++ ) {
        nodeArray[y][x] = new Node(x, y);
        if( y > 0 ) { 
          double flow = computeFlow(intensityArray[y][x], intensityArray[y-1][x]);
          nodeArray[y][x].edges.add(new Edge(nodeArray[y][x], nodeArray[y-1][x], flow));
          nodeArray[y-1][x].edges.add(new Edge(nodeArray[y-1][x], nodeArray[y][x], flow));
        }
        if( x > 0 ) { 
          double flow = computeFlow(intensityArray[y][x], intensityArray[y][x-1]);
          nodeArray[y][x].edges.add(new Edge(nodeArray[y][x], nodeArray[y][x-1], flow));
          nodeArray[y][x-1].edges.add(new Edge(nodeArray[y][x-1], nodeArray[y][x], flow));
        }
      }
    }
//    printNodeArray(nodeArray);
//    System.err.println("\n\n\n\n\n\n");

    
//    for( int y = 0; y < seedArray.length; y++ ) {
//      for( int x = 0; x < seedArray[0].length; x++ ) {
//        System.err.print(intensityArray[y][x] + ", ");
//      }
//      System.err.println();
//    }
//    for( int i = 0; i < 256; i += 2 ) {
//      System.err.println(i + ", " + computeFlow(0, i));
//    }
//    System.exit(0);
    Node source = new Node(-1, -1);
    Node sink = new Node(-2, -2);
    for( int y = 0; y < nodeArray.length; y++ ) {
      for( int x = 0; x < nodeArray[0].length; x++ ) {
        if( seedArray[y][x] == 1 ) {
          source.edges.add(new Edge(source, nodeArray[y][x], 2));
        }
        else if( seedArray[y][x] == 2 ) {
          nodeArray[y][x].edges.add(new Edge(nodeArray[y][x], sink, 2));
        }
      }
    }
    System.err.println("Beginning segmentation");
    boolean searching = true;
    boolean[][] visited = null;
    int maxSearch = 0;
    while(searching) {
      LinkedList<BFSNode> search = new LinkedList<BFSNode>();
      visited = new boolean[nodeArray.length][nodeArray[0].length];
      search.add(new BFSNode(source, null, null));
      BFSNode end = null;
      while( search.size() > 0 ) {
        maxSearch = Math.max(search.size(), maxSearch);
        BFSNode element = search.removeFirst();
//        System.err.println("visiting " + element.node);
        boolean addedOne = false;
        for( Edge e : element.node.edges ) {
          if( e.flowLeft > 0 ) {
            if( e.to == sink ) {
              end = element;
              search.clear();
              break; // need to super break
            }
            else {
              if( !visited[e.to.y][e.to.x] ) {
//                System.err.println("adding " + e.to + " maxflow:" + e.maxFlow + ", flow:" + e.flowLeft);
                search.addLast(new BFSNode(e.to, element, e));
                visited[e.to.y][e.to.x] = true;
                addedOne = true;
              }
            }
          }
        }
        if( !addedOne ) {
          element.parent.node.edges.remove(element.parent.edge);
          element.node.edges.clear();
//          System.err.println("Removing node edges since all 0");
        }
      }
      if( end != null ) {
        double maxFlow = 9999;
        BFSNode start = end;
        String path = "";
        do {
          path = "(" + start.node.x + "," + start.node.y + ")->" + path;
          maxFlow = Math.min(maxFlow, start.edge.flowLeft);
          start = start.parent;
        } while( start.parent != null );
//        System.err.println("path=" + path);
        path = maxFlow + "";
        start = end;
        do {
          path = "(" + start.node.x + "," + start.node.y + ")->" + path;
          if( start.edge.flowLeft == maxFlow ) {
            start.edge.flowLeft = 0;
          }
          else {
            start.edge.flowLeft -= maxFlow;
          }
          start = start.parent;
        } while( start.parent != null );
//        System.err.println("Completed augmenting path=" + path);
      }
      else {
        searching = false;
      }
    }
    System.err.println("finished segmentation, max search size=" + maxSearch);
//    printSegmentArray(visited);
    return visited;
  }
  
  public class BFSNode {
    public Node node;
    public Edge edge;
    public BFSNode parent;
    public BFSNode(Node node, BFSNode parent, Edge edge ) {
      this.node = node;
      this.parent = parent;
      this.edge = edge;
    }
  }
  
  private double computeFlow( int[] intensity1, int[] intensity2 ) {
    double sigmaSquared = (40*40);
    double distance = (intensity1[0] - intensity2[0])*(intensity1[0] - intensity2[0]) 
        + (intensity1[1] - intensity2[1])*(intensity1[1] - intensity2[1])
        + (intensity1[2] - intensity2[2])*(intensity1[2] - intensity2[2]);
    return Math.exp(- 1.0*(distance) / 2.0 / sigmaSquared );
  }
  
  private void printSegmentArray(boolean[][] array) {
    for( int y = 0; y < array.length; y++ ) {
      for( int x = 0; x < array[0].length; x++ ) {
        if( array[y][x] ) {
          System.err.print("#");
        }
        else {
          System.err.print("-");
        }
      }
      System.err.println();
    }
  }
  
  private void printSeedArray(int[][] array) {
    for( int y = 0; y < array.length; y++ ) {
      for( int x = 0; x < array[0].length; x++ ) {
        System.err.print(array[y][x]);
      }
      System.err.println();
    }
  }
  
  private void printIntensityArray(byte[][] array) {
    for( int y = 0; y < array.length; y++ ) {
      for( int x = 0; x < array[0].length; x++ ) {
        int i = array[y][x] & 0xFF;
        if( i < 64 ) {
          System.err.print("#");
        }
        else if( i <128 ) {
          System.err.print("-");
        }
        else {
          System.err.print(" ");
        }
      }
      System.err.println();
    }
  }
  private void printNodeArray(Node[][] array) {
    for( int y = 0; y < array.length; y++ ) {
      for( int x = 0; x < array[0].length; x++ ) {
        
        double flow = 0;
        for( Edge e : array[y][x].edges ) {
          flow += e.maxFlow;
        }
        flow /= 4;
        if( flow > 0.2 ) {
          System.err.print(" ");
        }
        else if( flow  > 0.05 ) {
          System.err.print("-");
        }
        else {
          System.err.print("#");
        }
      }
      System.err.println();
    }
  }
  
  private class Edge {
    public Node from;
    public Node to;
    public double maxFlow;
    public double flowLeft;
    public Edge(Node from, Node to, double maxFlow) {
      this.from = from;
      this.to = to;
      this.maxFlow = maxFlow;
      this.flowLeft = maxFlow;
    }
  }
  private class Node {
    public int x;
    public int y;
    public LinkedList<Edge> edges;
    public Node(int x, int y) {
      edges = new LinkedList<Edge>();
      this.x = x;
      this.y = y;
    }
    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }
  
  private int[][] convertToArray(BufferedImage image) {
    int[][] array = new int[image.getHeight()][image.getWidth()];
    for( int y = 0; y < image.getHeight(); y++ ) {
      for( int x = 0; x < image.getWidth(); x++ ) {
        array[y][x] = image.getRGB(x, y);
      }
    }
    return array;
  }
  private int[][][] convertToArray2(BufferedImage image) {
    int[][][] array = new int[image.getHeight()][image.getWidth()][3];
    for( int y = 0; y < image.getHeight(); y++ ) {
      for( int x = 0; x < image.getWidth(); x++ ) {
        Color color = new Color(image.getRGB(x, y));
        array[y][x][0] = color.getRed();
        array[y][x][1] = color.getGreen();
        array[y][x][2] = color.getBlue();
      }
    }
    return array;
  }
  
  private BufferedImage grayscale(BufferedImage image) {
    BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics g = gray.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return gray;
  }
  
  public static void main(String[] args) {
    new MaxFlow();
  }
}
