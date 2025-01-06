/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Entidades;

import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/*@author Arturo_Maciel */
public class FrmBuySell extends javax.swing.JFrame {
    private String userRol;
    private int userID;
    private Connection conn;
    private String month;
    private String year;
    private String reportType;
    
    public FrmBuySell() {
        initComponents();
        initialize();
    }
    
    private void initialize(){ 
        openConnection();
        addComboBoxListeners();
        loadData(month, year);
        adjustColumnWidths();
        
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
    
    private void adjustColumnWidths() {
         for (int column = 0; column < tblSell.getColumnCount(); column++) {
             int width = 5; // Min width

             // Obtener el ancho del encabezado
             TableCellRenderer headerRenderer = tblSell.getTableHeader().getDefaultRenderer();
             Component headerComp = headerRenderer.getTableCellRendererComponent(tblSell, tblSell.getColumnModel().getColumn(column).getHeaderValue(), false, false, 0, column);
             width = Math.max(headerComp.getPreferredSize().width + 1, width);

             // Obtener el ancho de las celdas
             for (int row = 0; row < tblSell.getRowCount(); row++) {
                 TableCellRenderer renderer = tblSell.getCellRenderer(row, column);
                 Component comp = tblSell.prepareRenderer(renderer, row, column);
                 width = Math.max(comp.getPreferredSize().width + 1, width);
             }

             if (width > 1000) {
                 width = 300; // Max width
             }
             tblSell.getColumnModel().getColumn(column).setPreferredWidth(width);
         }
        for (int row = 0; row < tblSell.getRowCount(); row++) {
        int rowHeight = tblSell.getRowHeight();

        for (int column = 0; column < tblSell.getColumnCount(); column++) {
            Component comp = tblSell.prepareRenderer(tblSell.getCellRenderer(row, column), row, column);
            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
        }

        tblSell.setRowHeight(row, rowHeight);
    }
     }
    
    private void addComboBoxListeners() {
        cmbMonth.addActionListener((java.awt.event.ActionEvent evt) -> {
            int selectedIndex = cmbMonth.getSelectedIndex();
            String selectedMonth = String.valueOf(selectedIndex);
            month = selectedMonth;
            loadData(month, year);
        });

        cmbYear.addActionListener((java.awt.event.ActionEvent evt) -> {
            String selectedYear = (String) cmbYear.getSelectedItem();
            year = selectedYear;
            loadData(month, year);
        });
        
    }
    
    private void loadData(String month, String year){
        double profit = 0.0;
        double expenses = 0.0;
        double totalProfit = 0.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();  
        DefaultTableModel mdlSell = (DefaultTableModel) tblSell.getModel();
        mdlSell.setRowCount(0);
        DefaultTableModel mdlBuy = (DefaultTableModel) tblBuy.getModel();
        mdlBuy.setRowCount(0);

        String queryBuy = "SELECT h.description, h.formula, h.laboratory, SUM(h.quantity) AS QUANTITY, " 
                            + "SUM((h.price_sell - h.price_buy) * h.quantity) AS PROFIT_SELLS "
                            + "FROM history_products h "
                            + "LEFT JOIN action_types a ON h.action_id = a.id "
                            + "LEFT JOIN employee e ON h.id_employee = e.id "
                            + "LEFT JOIN section s ON h.id_section = s.id "
                            + "WHERE a.description IN ('ADD', 'RESTOCK') ";

        if (!"TODOS".equals(year) && year != null) {
            queryBuy += " AND YEAR(h.create_time) = " + year;
        }
        if (!"0".equals(month) && month != null) {
            queryBuy += " AND MONTH(h.create_time) = " + month;
        }
        
        queryBuy += " GROUP BY h.description, h.formula, h.laboratory ORDER BY PROFIT_SELLS DESC";

        try (PreparedStatement pstmtBuy = (PreparedStatement) conn.prepareStatement(queryBuy)) {
            ResultSet rs = pstmtBuy.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("description"),
                    rs.getString("formula"),
                    rs.getString("laboratory"),
                    rs.getInt("QUANTITY"),
                    currencyFormat.format(rs.getDouble("PROFIT_SELLS"))
                };
                expenses += rs.getDouble("PROFIT_SELLS");
                mdlBuy.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String querySell = "SELECT h.description, h.formula, h.laboratory, SUM(h.quantity) AS QUANTITY, " 
                        + "SUM((h.price_sell - h.price_buy) * h.quantity) AS PROFIT_SELLS "
                        + "FROM history_products h "
                        + "LEFT JOIN action_types a ON h.action_id = a.id "
                        + "LEFT JOIN employee e ON h.id_employee = e.id "
                        + "LEFT JOIN section s ON h.id_section = s.id "
                        + "WHERE a.description IN ('SOLD') ";
            if (!"Default".equals(year) && year != null) {
        querySell += " AND YEAR(h.create_time) = " + year;
    }
    if (!"0".equals(month) && month != null) {
        querySell += " AND MONTH(h.create_time) = " + month;
    }
    
    querySell += " GROUP BY h.description, h.formula, h.laboratory ORDER BY PROFIT_SELLS DESC";
        
    try (PreparedStatement pstmtSell = (PreparedStatement) conn.prepareStatement(querySell)) {
        ResultSet rs = pstmtSell.executeQuery();

        while (rs.next()) {
            Object[] row = {
                rs.getString("description"),
                rs.getString("formula"),
                rs.getString("laboratory"),
                rs.getInt("QUANTITY"),
                currencyFormat.format(rs.getDouble("PROFIT_SELLS"))
            };
            profit += rs.getDouble("PROFIT_SELLS");
            mdlSell.addRow(row);
        }

        } catch (SQLException e) {
            e.printStackTrace();
        }
       
        totalProfit = profit - expenses;
        
        adjustColumnWidths();   
        String profitText = currencyFormat.format(profit);
        String expensesText = currencyFormat.format(expenses);
        String totalText = currencyFormat.format(totalProfit);
        txtbuy.setText(profitText);
        txtSell.setText(expensesText);
        txtTotal.setText(totalText);
    }
       
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSell = new javax.swing.JTable();
        lblMonth = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        lblYear = new javax.swing.JLabel();
        cmbYear = new javax.swing.JComboBox<>();
        lblIcon = new javax.swing.JLabel();
        lblTotalProfit = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        btnCancelar = new javax.swing.JButton();
        lblSummary = new javax.swing.JLabel();
        lblExpenses = new javax.swing.JLabel();
        txtSell = new javax.swing.JTextField();
        txtbuy = new javax.swing.JTextField();
        lblProfit = new javax.swing.JLabel();
        lblSell = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblBuy = new javax.swing.JTable();
        lblBuy = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tblSell.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descripcion", "Formula", "Laboratorio", "Cantidad", "Ganancias"
            }
        )
        {
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        }

    );
    jScrollPane1.setViewportView(tblSell);

    lblMonth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblMonth.setText("MES");

    cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"}));

    lblYear.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblYear.setText("Año");

    cmbYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "2024", "2025", "2026", "2027", "2028", "2029", "2030"}));

    lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

    lblTotalProfit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblTotalProfit.setText(" Total");

    txtTotal.setEditable(false);

    btnCancelar.setText("Cancelar");
    btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnCancelarMouseClicked(evt);
        }
    });
    btnCancelar.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCancelarActionPerformed(evt);
        }
    });

    lblSummary.setBackground(new java.awt.Color(255, 255, 255));
    lblSummary.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
    lblSummary.setText("RESUMEN");

    lblExpenses.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblExpenses.setText("Ventas");

    txtSell.setEditable(false);

    txtbuy.setEditable(false);

    lblProfit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblProfit.setText("Compras");

    lblSell.setBackground(new java.awt.Color(255, 255, 255));
    lblSell.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
    lblSell.setText("VENTAS");

    tblBuy.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Descripcion", "Formula", "Laboratorio", "Cantidad", "Ganancias"
        }
    )
    {
        public boolean isCellEditable(int row, int column) {
            return false;
        };
    }

    );
    jScrollPane3.setViewportView(tblBuy);

    lblBuy.setBackground(new java.awt.Color(255, 255, 255));
    lblBuy.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
    lblBuy.setText("COMPRAS");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jScrollPane1)
                    .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                    .addGap(9, 9, 9)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(195, 195, 195)
                                    .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(cmbYear, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(44, 44, 44)
                                    .addComponent(lblMonth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(cmbMonth, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(75, 75, 75))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(lblSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblExpenses, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblTotalProfit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(lblProfit, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtbuy, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtSell, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(43, 43, 43)
                                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(54, 54, 54)
                            .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(36, 36, 36))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblSell, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(1041, 1041, 1041))))))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap()))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(lblBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1040, Short.MAX_VALUE)))
    );

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtSell, txtTotal, txtbuy});

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblExpenses, lblProfit, lblTotalProfit});

    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(17, 17, 17)
                    .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(211, 211, 211))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblYear)
                                .addComponent(cmbYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblMonth)
                                .addComponent(cmbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(231, 231, 231))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtbuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(lblProfit)
                                            .addComponent(lblSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblExpenses)
                                        .addComponent(txtSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addComponent(btnCancelar)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTotalProfit)
                                .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(219, 219, 219)))))
            .addComponent(lblSell, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(31, Short.MAX_VALUE))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(148, 148, 148)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(248, Short.MAX_VALUE)))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(116, 116, 116)
                .addComponent(lblBuy, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(427, Short.MAX_VALUE)))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelarMouseClicked
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FrmPOS POS = new FrmPOS();
                POS.setUserRol(userRol);
                POS.initialize();
                POS.setVisible(true);
            }
        });
        dispose();
    }//GEN-LAST:event_btnCancelarMouseClicked

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarActionPerformed

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
            java.util.logging.Logger.getLogger(FrmBuySell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmBuySell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmBuySell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmBuySell.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmBuySell().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JComboBox<String> cmbMonth;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBuy;
    private javax.swing.JLabel lblExpenses;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblProfit;
    private javax.swing.JLabel lblSell;
    private javax.swing.JLabel lblSummary;
    private javax.swing.JLabel lblTotalProfit;
    private javax.swing.JLabel lblYear;
    private javax.swing.JTable tblBuy;
    private javax.swing.JTable tblSell;
    private javax.swing.JTextField txtSell;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtbuy;
    // End of variables declaration//GEN-END:variables
}
