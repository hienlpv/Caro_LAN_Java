package caro;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public final class Board extends javax.swing.JFrame {

    Player player1, player2, currentPlayer, playing;
    Image x, o;
    JButton[][] Matrix = new JButton[20][20];
    Timer timer;
    SocketManager Data = new SocketManager();
    String username;
    boolean isServer = false;
    int Count = 1;

    public Board(String username) {
        this.username = username;
        this.timer = new Timer(1000, listen);
        initComponents();
        Draw();
        createPlayer();
        Default();
    }

    void createPlayer() {

        try {
            this.x = ImageIO.read(new File("Image/X.png"));
            this.o = ImageIO.read(new File("Image/O.png"));
            Logo.setIcon(new ImageIcon(ImageIO.read(new File("Image/logo.png")).getScaledInstance(Logo.getWidth(), Logo.getHeight(), 100)));       
            
            player1 = new Player(username, x);
            player2 = new Player("player2", o);
            currentPlayer = player1;
            playing = player1;
        } catch (IOException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void Default() {

        Mark.setIcon(new ImageIcon(currentPlayer.mark.getScaledInstance(Mark.getWidth(), Mark.getHeight(), 100)));

        lbUsername.setText(currentPlayer.username);
        prcTime.setValue(0);
        timer.stop();
        prcTime.setString("20 s");
    }

    void changePlayer() {
        if (currentPlayer.equals(player1)) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
        Mark.setIcon(new ImageIcon(currentPlayer.mark.getScaledInstance(Mark.getWidth(), Mark.getHeight(), 100)));
        lbUsername.setText(currentPlayer.username);
    }

    void changePlaying() {
        if (playing.equals(player1)) {
            playing = player2;
        } else {
            playing = player1;
        }
        Mark.setIcon(new ImageIcon(playing.mark.getScaledInstance(Mark.getWidth(), Mark.getHeight(), 100)));
        if (!playing.equals(currentPlayer)) {
            lbNotification.setText("Chưa đến lượt của bạn !");
        } else {
            lbNotification.setText("Lượt của bạn !");
        }
    }

    void ButtonClick(JButton btn) {

        prcTime.setValue(0);
        timer.start();
        btn.setIcon(new ImageIcon(playing.mark.getScaledInstance(btn.getWidth(), btn.getHeight(), 100)));
        btn.setName(playing.username);
        checkWin(btn);
        changePlaying();
    }

    void ButtonClick(Point p) {
        ButtonClick(Matrix[p.x][p.y]);
    }

    void Draw() {

        JButton oldbtn = new JButton();
        oldbtn.setSize(new Dimension(0, 0));
        oldbtn.setLocation(new Point(0, 0));

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                JButton btn = new JButton();
                btn.setName("");
                btn.setSize(new Dimension(30, 30));
                btn.setLocation(new Point(oldbtn.getLocation().x + oldbtn.getSize().width, oldbtn.getLocation().y));
                btn.setVisible(true);
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!btn.getName().equals("") || !playing.equals(currentPlayer)) {
                            if (!playing.equals(currentPlayer)) {
                                lbNotification.setText("Chưa đến lượt của bạn !");
                            } else {
                                lbNotification.setText("Lượt của bạn !");
                            }
                            return;
                        }
                        ButtonClick(btn);
                        Thread t = new Thread() {
                            @Override
                            public void run() {
                                if (isServer) {
                                    Data.Send("Server", getLocate(btn));
                                    Point p = (Point) Data.Receive("Server");
                                    ButtonClick(p);
                                } else {
                                    Data.Send("Client", getLocate(btn));
                                    Point p = (Point) Data.Receive("Client");
                                    ButtonClick(p);
                                }
                            }
                        };
                        t.start();
                    }
                });

                ChessBoard.add(btn);
                Matrix[i][j] = btn;
                oldbtn = btn;
            }
            oldbtn.setSize(new Dimension(0, 0));
            oldbtn.setLocation(new Point(0, oldbtn.getLocation().y + 30));
        }
    }

    Point getLocate(JButton btn) {
        Point result = new Point();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (Matrix[i][j].equals(btn)) {
                    result.setLocation(i, j);
                    return result;
                }
            }
        }
        return result;
    }

    ActionListener listen = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            prcTime.setValue(prcTime.getValue() + 1);
            prcTime.setString(String.valueOf(20 - prcTime.getValue()) + " s");
            if (prcTime.getValue() == prcTime.getMaximum()) {
                timer.stop();
                Component[] cmp = ChessBoard.getComponents();
                for (Component cm : cmp) {
                    JButton btn = (JButton) cm;
                    if (btn.getName().equals("")) {
                        btn.doClick(); break;
                    }
                }
            }
        }
    };

    void increaseScore(Player pl) {
        try {
            caro.Data.Connect();

            Statement s = caro.Data.con.createStatement();
            ResultSet rs = s.executeQuery("Select Score from Rank where username = '" + pl.username + "'");
            rs.next();
            int Score = rs.getInt("Score") + 1;

            s.executeUpdate("Update Rank set Score = " + Score + " where username = '" + pl.username + "'");

            caro.Data.Close();
        } catch (SQLException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean checkWin(JButton btn) {
        if (checkDoc(btn) || checkNgang(btn) || checkCheoLR(btn) || checkCheoRL(btn)) {
            Count++;
            JOptionPane.showMessageDialog(null, playing.username + " WIN !");
            if (playing.equals(currentPlayer)) {
                increaseScore(playing);
            }
            Component[] cn = ChessBoard.getComponents();
            for (Component comp : cn) {
                JButton bn = (JButton) comp;
                bn.setIcon(null);
                bn.setName("");
            }
            Default();
            lbCount.setText("Ván " + Count);
            return true;
        }
        return false;
    }

    boolean checkNgang(JButton btn) {
        int left = 0, right = 0;
        int i;
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).x - i < 0) {
                break;
            }
            if (Matrix[getLocate(btn).x - i][getLocate(btn).y].getName().equals(btn.getName())) {
                left++;
            } else {
                break;
            }
        }
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).x + i > 19) {
                break;
            }
            if (Matrix[getLocate(btn).x + i][getLocate(btn).y].getName().equals(btn.getName())) {
                right++;
            } else {
                break;
            }
        }
        return (left + right) == 4;
    }

    boolean checkDoc(JButton btn) {
        int left = 0, right = 0;
        int i;
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y - i < 0) {
                break;
            }
            if (Matrix[getLocate(btn).x][getLocate(btn).y - i].getName().equals(btn.getName())) {
                left++;
            } else {
                break;
            }
        }
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y + i > 19) {
                break;
            }
            if (Matrix[getLocate(btn).x][getLocate(btn).y + i].getName().equals(btn.getName())) {
                right++;
            } else {
                break;
            }
        }
        return (left + right) == 4;
    }

    boolean checkCheoLR(JButton btn) { // "\"
        int top = 0, bottom = 0;
        int i;
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y - i < 0 || getLocate(btn).x - i < 0) {
                break;
            }
            if (Matrix[getLocate(btn).x - i][getLocate(btn).y - i].getName().equals(btn.getName())) {
                top++;
            } else {
                break;
            }
        }
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y + i > 19 || getLocate(btn).x + i > 19) {
                break;
            }
            if (Matrix[getLocate(btn).x + i][getLocate(btn).y + i].getName().equals(btn.getName())) {
                bottom++;
            } else {
                break;
            }
        }
        return (top + bottom) == 4;
    }

    boolean checkCheoRL(JButton btn) { // "/"
        int top = 0, bottom = 0;
        int i;
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y + i > 19 || getLocate(btn).x - i < 0) {
                break;
            }
            if (Matrix[getLocate(btn).x - i][getLocate(btn).y + i].getName().equals(btn.getName())) {
                top++;
            } else {
                break;
            }
        }
        for (i = 1; i < 5; i++) {
            if (getLocate(btn).y - i < 0 || getLocate(btn).x + i > 19) {
                break;
            }
            if (Matrix[getLocate(btn).x + i][getLocate(btn).y - i].getName().equals(btn.getName())) {
                bottom++;
            } else {
                break;
            }
        }
        return (top + bottom) == 4;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ChessBoard = new javax.swing.JPanel();
        Mark = new javax.swing.JLabel();
        lbUsername = new javax.swing.JLabel();
        prcTime = new javax.swing.JProgressBar();
        btnLan = new javax.swing.JButton();
        lbNotification = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        Logo = new javax.swing.JLabel();
        lbCount = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnRank = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Caro");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        setSize(new java.awt.Dimension(0, 0));

        ChessBoard.setPreferredSize(new java.awt.Dimension(570, 600));

        javax.swing.GroupLayout ChessBoardLayout = new javax.swing.GroupLayout(ChessBoard);
        ChessBoard.setLayout(ChessBoardLayout);
        ChessBoardLayout.setHorizontalGroup(
            ChessBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 570, Short.MAX_VALUE)
        );
        ChessBoardLayout.setVerticalGroup(
            ChessBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        lbUsername.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

        prcTime.setMaximum(20);
        prcTime.setString("20 s");
        prcTime.setStringPainted(true);

        btnLan.setText("LAN");
        btnLan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLanActionPerformed(evt);
            }
        });

        lbNotification.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lbNotification.setForeground(new java.awt.Color(255, 0, 0));

        jLabel1.setFont(new java.awt.Font("Arial", 3, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Ai 5 con trước thắng");

        lbCount.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N

        jMenu1.setText("View");

        mnRank.setText("Top 10 Ranked");
        mnRank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnRankActionPerformed(evt);
            }
        });
        jMenu1.add(mnRank);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ChessBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(prcTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLan, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lbNotification, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Logo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lbUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lbCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Mark, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ChessBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Logo, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lbUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbCount, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbNotification, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(prcTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLan, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(Mark, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLanActionPerformed

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Data.ConnectServer();
                    player2.username = player1.username;
                    changePlayer();
                    Data.Send("Client", player2.username);
                    player1.username = Data.Receive("Client").toString();
                    lbNotification.setText("Đã kết nối với: " + player1.username);
                    lbCount.setText("Ván " + Count);
                    Point p = (Point) Data.Receive("Client");
                    ButtonClick(p);
                } catch (IOException ex) {
                    lbNotification.setText("Đợi người chơi khác !!! ");
                    Data.CreateServer();
                    isServer = true;
                    player2.username = Data.Receive("Server").toString();
                    lbNotification.setText("Đã kết nối với: " + player2.username);
                    lbCount.setText("Ván " + Count);
                    Data.Send("Server", player1.username);
                }
            }
        };
        thread.start();

    }//GEN-LAST:event_btnLanActionPerformed

    private void mnRankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnRankActionPerformed
        Rank r = new Rank(currentPlayer.username);
        r.setVisible(true);
    }//GEN-LAST:event_mnRankActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ChessBoard;
    private javax.swing.JLabel Logo;
    private javax.swing.JLabel Mark;
    private javax.swing.JButton btnLan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JLabel lbCount;
    private javax.swing.JLabel lbNotification;
    private javax.swing.JLabel lbUsername;
    private javax.swing.JMenuItem mnRank;
    private javax.swing.JProgressBar prcTime;
    // End of variables declaration//GEN-END:variables

}
