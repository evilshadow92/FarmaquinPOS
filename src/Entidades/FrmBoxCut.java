/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Entidades;

import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author Arturo_Maciel
 */
public class FrmBoxCut extends javax.swing.JFrame {
    private String userRol;
    private int userID;
    private Connection conn;
    
    private double cashPrevious = 0.0;
    private double sellAmount = 0.0;
    private double expensesAmount = 0.0;
    private double cashTotal = 0.0;
    private double adjustment = 0.0;
    private boolean boxCutDone = false;
    private java.sql.Date date;

    public FrmBoxCut() {
        initComponents();
        openConnection();
        loadData();
    }
        public void setUserRol(String userRol){
        this.userRol = userRol;
    }
    
    public void setUserId(int userID){
        this.userID = userID;
    }
    
    private void openConnection(){        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al abrir la conexion: " + e.getMessage());
        }
    }
    
private void loadData() {
    
    //Gets previous boxCut info
    try (PreparedStatement stmt = (PreparedStatement) conn.prepareStatement("SELECT cash_current, date FROM box_cut WHERE id = (SELECT id FROM box_cut ORDER BY id DESC LIMIT 1)")) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            cashPrevious = rs.getDouble("cash_current");
            date = rs.getDate("date");
        } else {
            System.out.println("No data found.");
        }       
        
        // Check if the boxCut for the current day has been done
        Date utilDateToCompare = new Date(date.getTime());
        LocalDate localDateToCompare = utilDateToCompare.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        
        if (localDateToCompare.equals(currentDate)) {
            boxCutDone = true;
        
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al buscar efectivo y fecha previos", "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    //Gets the Sells amounts
    try (PreparedStatement stmt = (PreparedStatement) conn.prepareStatement("SELECT SUM(price_sell * quantity) sellAmount " +
                                                                            "FROM history_products " +
                                                                            "WHERE action_id = '4' " +
                                                                            "AND create_time > (SELECT date FROM box_cut ORDER BY id DESC LIMIT 1)")) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            sellAmount = rs.getDouble("sellAmount");
        } else {
            System.out.println("No data found.");
        }        
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al buscar las ventas dia", "Error", JOptionPane.ERROR_MESSAGE);
    }    

    //Gets the expenses amounts
    try (PreparedStatement stmt = (PreparedStatement) conn.prepareStatement("SELECT SUM(amount) expensesAmount " + 
                                                                            "FROM history_expenses WHERE date > (SELECT date FROM box_cut ORDER BY id DESC LIMIT 1)")) {
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            expensesAmount = rs.getDouble("expensesAmount");
        } else {
            System.out.println("No data found.");
        }        
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al buscar los gastos del dia", "Error", JOptionPane.ERROR_MESSAGE);
    }
    cashTotal = cashPrevious + sellAmount - expensesAmount;

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(); ;
    
    txtSells.setText(currencyFormat.format(sellAmount));
    txtExpenses.setText(currencyFormat.format(expensesAmount));
    txtTotalCash.setText(currencyFormat.format(cashTotal));
    txtBoxCash.setText("");
    txtComments.setText("");
    
}

private void recordBoxCut(Double cashCurrent, String comments){
    boolean ok = false;
    
    if(cashTotal == cashCurrent){
        adjustment = 0;
        ok = true;
    }else{
        int response = JOptionPane.showConfirmDialog(this, "¿La cantidad ingresada es diferente a la esperada\n Ingresar como ajuste?", "Alerta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {   
            if (!comments.equals("")) {
                adjustment = (cashCurrent - cashTotal);
                ok = true;
            }else {
            JOptionPane.showMessageDialog(this, "Se debe agregar un comentario del ajuste", "Error", JOptionPane.ERROR_MESSAGE);        
    }              
            
        } else if (response == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(this, "No es posible realizar el corte de Caja", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    if(ok && !boxCutDone){
        
        String instertBoxCut = "INSERT INTO box_cut (id_employee, sell_amount, expenses_amount, cash_previous, cash_current, adjustment, comments) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(instertBoxCut)){
            pstmt.setInt(1, userID);
            pstmt.setDouble(2, sellAmount);
            pstmt.setDouble(3, expensesAmount);
            pstmt.setDouble(4, cashPrevious);
            pstmt.setDouble(5, cashCurrent);
            pstmt.setDouble(6, adjustment);
            pstmt.setString(7, comments);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(rootPane, "Transaccion exitosa");
            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al hacer el corte", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }else if(boxCutDone){
        JOptionPane.showMessageDialog(this, "El corte de caja de hoy ya fue realizado", "Error", JOptionPane.ERROR_MESSAGE);        
    }    
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblBoxCut = new javax.swing.JLabel();
        lblSells = new javax.swing.JLabel();
        txtSells = new javax.swing.JTextField();
        txtExpenses = new javax.swing.JTextField();
        lblExpenses = new javax.swing.JLabel();
        lblTotalCash = new javax.swing.JLabel();
        txtTotalCash = new javax.swing.JTextField();
        lblBoxCash = new javax.swing.JLabel();
        txtBoxCash = new javax.swing.JTextField();
        lblCommets = new javax.swing.JLabel();
        txtComments = new javax.swing.JTextField();
        btnAcept = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

        lblBoxCut.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        lblBoxCut.setText("Corte de Caja");

        lblSells.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblSells.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSells.setText("Ventas");

        txtSells.setEditable(false);
        txtSells.setFocusable(false);

        txtExpenses.setEditable(false);
        txtExpenses.setFocusable(false);

        lblExpenses.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblExpenses.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblExpenses.setText("Gastos");

        lblTotalCash.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblTotalCash.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalCash.setText("Efectivo Total");

        txtTotalCash.setEditable(false);
        txtTotalCash.setFocusable(false);

        lblBoxCash.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblBoxCash.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBoxCash.setText("Efectivo en Caja");

        lblCommets.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        lblCommets.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCommets.setText("Comentario");

        btnAcept.setText("Aceptar");
        btnAcept.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAceptMouseClicked(evt);
            }
        });

        btnCancel.setText("Cancelar");
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblBoxCut))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblExpenses, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                            .addComponent(lblTotalCash)
                            .addComponent(lblCommets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblBoxCash, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtComments, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtBoxCash, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtTotalCash, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtExpenses, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblSells, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSells, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnAcept, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblBoxCash, lblCommets, lblExpenses, lblSells, lblTotalCash});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblBoxCut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSells, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSells))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblExpenses)
                            .addComponent(txtExpenses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTotalCash)
                            .addComponent(txtTotalCash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBoxCash, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtBoxCash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCommets)
                            .addComponent(txtComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(btnAcept)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel)
                        .addContainerGap(21, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        FrmPOS POS = new FrmPOS();
        POS.setUserRol(userRol);
        POS.setUserId(userID);
        POS.initialize();
        POS.setVisible(true);
        dispose();
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnAceptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAceptMouseClicked
        String boxCashText = txtBoxCash.getText();
        String comments = txtComments.getText();
        
        
        if (!boxCashText.equals("")) {
            try{
                Double cashCurrent = Double.parseDouble(boxCashText) ;
                recordBoxCut(cashCurrent, comments);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese una cantidad valida.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una cantidad valida", "Error", JOptionPane.ERROR_MESSAGE);
        }   
        
    }//GEN-LAST:event_btnAceptMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmBoxCut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmBoxCut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmBoxCut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmBoxCut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmBoxCut().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcept;
    private javax.swing.JButton btnCancel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblBoxCash;
    private javax.swing.JLabel lblBoxCut;
    private javax.swing.JLabel lblCommets;
    private javax.swing.JLabel lblExpenses;
    private javax.swing.JLabel lblSells;
    private javax.swing.JLabel lblTotalCash;
    private javax.swing.JTextField txtBoxCash;
    private javax.swing.JTextField txtComments;
    private javax.swing.JTextField txtExpenses;
    private javax.swing.JTextField txtSells;
    private javax.swing.JTextField txtTotalCash;
    // End of variables declaration//GEN-END:variables
}
