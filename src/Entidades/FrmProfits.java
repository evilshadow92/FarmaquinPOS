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
public class FrmProfits extends javax.swing.JFrame {
    private String userRol;
    private int userID;
    private Connection conn;
    private String month;
    private String year;
    private String reportType;
    
    public FrmProfits() {
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
         for (int column = 0; column < tblReport.getColumnCount(); column++) {
             int width = 5; // Min width

             // Obtener el ancho del encabezado
             TableCellRenderer headerRenderer = tblReport.getTableHeader().getDefaultRenderer();
             Component headerComp = headerRenderer.getTableCellRendererComponent(tblReport, tblReport.getColumnModel().getColumn(column).getHeaderValue(), false, false, 0, column);
             width = Math.max(headerComp.getPreferredSize().width + 1, width);

             // Obtener el ancho de las celdas
             for (int row = 0; row < tblReport.getRowCount(); row++) {
                 TableCellRenderer renderer = tblReport.getCellRenderer(row, column);
                 Component comp = tblReport.prepareRenderer(renderer, row, column);
                 width = Math.max(comp.getPreferredSize().width + 1, width);
             }

             if (width > 1000) {
                 width = 300; // Max width
             }
             tblReport.getColumnModel().getColumn(column).setPreferredWidth(width);
         }
        for (int row = 0; row < tblReport.getRowCount(); row++) {
        int rowHeight = tblReport.getRowHeight();

        for (int column = 0; column < tblReport.getColumnCount(); column++) {
            Component comp = tblReport.prepareRenderer(tblReport.getCellRenderer(row, column), row, column);
            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
        }

        tblReport.setRowHeight(row, rowHeight);
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
        DefaultTableModel mdlProfit = (DefaultTableModel) tblReport.getModel();
        mdlProfit.setRowCount(0);
        DefaultTableModel mdlExpenses = (DefaultTableModel) tblExpenses.getModel();
        mdlExpenses.setRowCount(0);
        
        String queryExpenses = "SELECT * FROM history_expenses WHERE 1=1";

        String queryProfitProduct = "SELECT h.description, h.formula, h.laboratory, SUM(h.quantity) AS QUANTITY, " +
                                    "SUM((h.price_sell - h.price_buy) * h.quantity) AS PROFIT_SELLS " +
                                    "FROM history_products h " +
                                    "LEFT JOIN action_types a ON h.action_id = a.id " +
                                    "LEFT JOIN employee e ON h.id_employee = e.id " +
                                    "LEFT JOIN section s ON h.id_section = s.id " +
                                    "WHERE a.description = 'SOLD'";
        if (!"TODOS".equals(year) && year != null) {
            queryProfitProduct += " AND YEAR(h.create_time) = " + year;
            queryExpenses += " AND YEAR(date) = " + year;
        }
        if (!"0".equals(month) && month != null) {
            queryProfitProduct += " AND MONTH(h.create_time) = " + month;
            queryExpenses += " AND MONTH(date) = " + month;
        }
        queryProfitProduct += " GROUP BY h.description, h.formula, h.laboratory ORDER BY PROFIT_SELLS DESC";
        

        try (PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(queryProfitProduct)) {
            ResultSet rsProfit = pstmt.executeQuery();

            while (rsProfit.next()) {
                Object[] row = {
                    rsProfit.getString("description"),
                    rsProfit.getString("formula"),
                    rsProfit.getString("laboratory"),
                    rsProfit.getInt("QUANTITY"),
                    currencyFormat.format(rsProfit.getDouble("PROFIT_SELLS"))
                };
                profit += rsProfit.getDouble("PROFIT_SELLS");                
                mdlProfit.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement pstmtExpenses = (PreparedStatement) conn.prepareStatement(queryExpenses)) {
            ResultSet rsExpenses = pstmtExpenses.executeQuery();

            while (rsExpenses.next()) {
                Object[] row = {
                    rsExpenses.getString("description"),
                    currencyFormat.format(rsExpenses.getDouble("amount")),
                    rsExpenses.getString("comments")
                };          
                expenses += rsExpenses.getDouble("amount");       
                mdlExpenses.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }       
        
        totalProfit = profit - expenses;
        
        adjustColumnWidths();   
        String profitText = currencyFormat.format(profit);
        String expensesText = currencyFormat.format(expenses);
        String totalText = currencyFormat.format(totalProfit);
        txtProfit.setText(profitText);
        txtExpenses.setText(expensesText);
        txtTotalProfit.setText(totalText);
    }
       
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblReport = new javax.swing.JTable();
        lblMonth = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        lblYear = new javax.swing.JLabel();
        cmbYear = new javax.swing.JComboBox<>();
        lblIcon = new javax.swing.JLabel();
        lblTotalProfit = new javax.swing.JLabel();
        txtTotalProfit = new javax.swing.JTextField();
        btnCancelar = new javax.swing.JButton();
        lblReportType = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblExpenses = new javax.swing.JTable();
        lblExpenses = new javax.swing.JLabel();
        txtExpenses = new javax.swing.JTextField();
        txtProfit = new javax.swing.JTextField();
        lblProfit = new javax.swing.JLabel();
        lblProfits = new javax.swing.JLabel();

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

        tblReport.setModel(new javax.swing.table.DefaultTableModel(
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
    jScrollPane1.setViewportView(tblReport);

    lblMonth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblMonth.setText("MES");

    cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"}));

    lblYear.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblYear.setText("Año");

    cmbYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "2024", "2025", "2026", "2027", "2028", "2029", "2030"}));

    lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

    lblTotalProfit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblTotalProfit.setText(" Total");

    txtTotalProfit.setEditable(false);

    btnCancelar.setText("Cancelar");
    btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnCancelarMouseClicked(evt);
        }
    });

    lblReportType.setBackground(new java.awt.Color(255, 255, 255));
    lblReportType.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
    lblReportType.setText("Gastos");

    tblExpenses.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
        },
        new String [] {
            "Concepto", "Cantidad", "Comentario"
        }
    ));
    jScrollPane4.setViewportView(tblExpenses);

    lblExpenses.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblExpenses.setText("Egresos");

    txtExpenses.setEditable(false);

    txtProfit.setEditable(false);

    lblProfit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    lblProfit.setText("Ingresos");

    lblProfits.setBackground(new java.awt.Color(255, 255, 255));
    lblProfits.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
    lblProfits.setText("Ventas");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(63, 63, 63)
                    .addComponent(lblProfits, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(112, 112, 112)
                    .addComponent(lblYear, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cmbYear, 0, 104, Short.MAX_VALUE)
                    .addGap(44, 44, 44)
                    .addComponent(lblMonth, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cmbMonth, 0, 104, Short.MAX_VALUE)
                    .addGap(527, 527, 527))
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1)))
            .addContainerGap())
        .addGroup(layout.createSequentialGroup()
            .addGap(51, 51, 51)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(12, 12, 12)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(30, 30, 30)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblExpenses, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblProfit, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblTotalProfit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTotalProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtProfit, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtExpenses, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(74, 74, 74))
    );

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtExpenses, txtProfit, txtTotalProfit});

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblExpenses, lblProfit, lblTotalProfit});

    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblYear)
                .addComponent(cmbYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblMonth)
                .addComponent(cmbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblProfits, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(48, 48, 48))
                .addGroup(layout.createSequentialGroup()
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(lblReportType, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtProfit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblProfit, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblExpenses)
                                .addComponent(txtExpenses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTotalProfit)
                                .addComponent(txtTotalProfit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btnCancelar)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
            java.util.logging.Logger.getLogger(FrmProfits.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmProfits.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmProfits.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmProfits.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmProfits().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JComboBox<String> cmbMonth;
    private javax.swing.JComboBox<String> cmbYear;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblExpenses;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblProfit;
    private javax.swing.JLabel lblProfits;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblTotalProfit;
    private javax.swing.JLabel lblYear;
    private javax.swing.JTable tblExpenses;
    private javax.swing.JTable tblReport;
    private javax.swing.JTextField txtExpenses;
    private javax.swing.JTextField txtProfit;
    private javax.swing.JTextField txtTotalProfit;
    // End of variables declaration//GEN-END:variables
}
