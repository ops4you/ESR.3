/* ------------------
   Cliente
   usage: java Cliente
   adaptado dos originais pela equipa docente de ESR (nenhumas garantias)
   colocar o cliente primeiro a correr que o servidor dispara logo!
   ---------------------- */

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Cliente {

  // GUI
  // ----
  JFrame f = new JFrame("Cliente de Testes");
  JButton setupButton = new JButton("Setup");
  JButton playButton = new JButton("Play");
  JButton pauseButton = new JButton("Pause");
  JButton tearButton = new JButton("Teardown");
  JPanel mainPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JLabel iconLabel = new JLabel();
  ImageIcon icon;

  // RTP variables:
  // ----------------
  DatagramPacket rcvdp; // UDP packet received from the server (to receive)
  DatagramSocket RTPsocket; // socket to be used to send and receive UDP packet
  static int RTP_RCV_PORT = 25000; // port where the client will receive the RTP packets

  Timer cTimer; // timer used to receive data from the UDP socket
  byte[] cBuf; // buffer used to store data received from the server

  // --------------------------
  // Constructor
  // --------------------------
  @SuppressWarnings("all")
  public Cliente(String routerIP) throws Exception {
    // build GUI
    // --------------------------
    RTPsocket = new DatagramSocket(RTP_RCV_PORT);
    byte[] buf2 = new byte[256];
    buf2 = "rqst:".getBytes();
    DatagramPacket newptk = new DatagramPacket(buf2, buf2.length, InetAddress.getByName(routerIP), RTP_RCV_PORT);
    System.out.println("sending a request to:" + newptk.getAddress().getHostAddress() + " " + newptk.getPort());
    RTPsocket.send(newptk);
    // thingy to shutdown safely
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        byte[] buf2 = new byte[256];
        buf2 = "stop:".getBytes();
        DatagramPacket newptk = new DatagramPacket(buf2, buf2.length);
        try {
          RTPsocket.send(newptk);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    });
    // Frame
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // Buttons
    buttonPanel.setLayout(new GridLayout(1, 0));
    buttonPanel.add(setupButton);
    buttonPanel.add(playButton);
    buttonPanel.add(pauseButton);
    buttonPanel.add(tearButton);

    // handlers... (so dois)
    playButton.addActionListener(new playButtonListener());
    tearButton.addActionListener(new tearButtonListener());

    // Image display label
    iconLabel.setIcon(null);

    // frame layout
    mainPanel.setLayout(null);
    mainPanel.add(iconLabel);
    mainPanel.add(buttonPanel);
    iconLabel.setBounds(0, 0, 380, 280);
    buttonPanel.setBounds(0, 280, 380, 50);

    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
    f.setSize(new Dimension(390, 370));
    f.setVisible(true);

    // init para a parte do cliente
    // --------------------------
    cTimer = new Timer(20, new clientTimerListener());
    cTimer.setInitialDelay(0);
    cTimer.setCoalesce(true);
    cBuf = new byte[15000]; // allocate enough memory for the buffer used to receive data from the server

    try {
      // socket e video
      // RTPsocket = new DatagramSocket(RTP_RCV_PORT); // init RTP socket (o mesmo
      // para o cliente e servidor) socket ja foi criada
      RTPsocket.setSoTimeout(5000); // setimeout to 5s
    } catch (SocketException e) {
      System.out.println("Cliente: erro no socket: " + e.getMessage());
    }
  }

  // ------------------------------------
  // main
  // ------------------------------------
  @SuppressWarnings("all")
  public static void main(String argv[]) throws Exception {
    Cliente t = new Cliente(argv[0]);
  }

  // ------------------------------------
  // Handler for buttons
  // ------------------------------------

  // Handler for Play button
  // -----------------------
  class playButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      System.out.println("Play Button pressed !");
      // start the timers ...
      cTimer.start();
    }
  }

  // Handler for tear button
  // -----------------------
  class tearButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      System.out.println("Teardown Button pressed !");
      // stop the timer
      cTimer.stop();
      // exit
      System.exit(0);
    }
  }

  // ------------------------------------
  // Handler for timer (para cliente)
  // ------------------------------------

  class clientTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      // Construct a DatagramPacket to receive data from the UDP socket
      rcvdp = new DatagramPacket(cBuf, cBuf.length);

      try {
        // receive the DP from the socket:
        RTPsocket.receive(rcvdp);
        System.out.println("packet received");

        // create an RTPpacket object from the DP
        RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

        // print important header fields of the RTP packet received:
        System.out.println("Got RTP packet with SeqNum # " + rtp_packet.getsequencenumber() + " TimeStamp "
            + rtp_packet.gettimestamp() + " ms, of type " + rtp_packet.getpayloadtype());

        // print header bitstream:
        if (rtp_packet.getsequencenumber() % 10 == 0) {
          rtp_packet.printheader();
        }

        // get the payload bitstream from the RTPpacket object
        int payload_length = rtp_packet.getpayload_length();
        byte[] payload = new byte[payload_length];
        rtp_packet.getpayload(payload);

        // get an Image object from the payload bitstream
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(payload, 0, payload_length);

        // display the image as an ImageIcon object
        icon = new ImageIcon(image);
        iconLabel.setIcon(icon);
      } catch (InterruptedIOException iioe) {
        System.out.println("Nothing to read");
      } catch (IOException ioe) {
        System.out.println("Exception caught: " + ioe);
      }
    }
  }

}// end of Class Cliente
