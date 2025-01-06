/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Entidades;

import com.mysql.jdbc.PreparedStatement;
import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/*@author Arturo_Maciel */

public class FrmInventoryCheck extends javax.swing.JFrame {
    private static FrmInventoryCheck instance;
    private String userRol;
    private int userID;    
    private Connection conn;
    
    public void setUserRol(String userRol){
        this.userRol = userRol;
    }
    
    public void setUserId(int userID){
        this.userID = userID;
    }
    
    public void initialize() {
        if ("ADMINISTRATOR".equals(userRol)) {
            btnAdd.setEnabled(true);
            btnRestock.setEnabled(true);
        } else {
            btnAdd.setEnabled(false);
            btnRestock.setEnabled(false);
        }
        loadData("");   
        adjustColumnWidths();
    }
    
    private void adjustDesign(){
        setLayout(new BorderLayout());
        add(jScrollPanelResults, BorderLayout.CENTER);
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelLogo, BorderLayout.WEST);
        panelSuperior.add(panelFiltro, BorderLayout.CENTER);

    // Agregar el panel superior al norte
    add(panelSuperior, BorderLayout.NORTH);
    }
    
    public FrmInventoryCheck() {
        initComponents();    
        openConnection();  
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        adjustDesign();
    }
        
    private void openConnection(){        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al abrir la conexion: " + e.getMessage());
        }
    }
    
    public static FrmInventoryCheck getInstance(){
        if (instance == null){
            instance = new FrmInventoryCheck();
        }
        return instance;
    }
    
    private void loadData(String filter){
        DefaultTableModel model = (DefaultTableModel) tblInventorySearch.getModel();
        model.setRowCount(0);
        

        String query = "SELECT p.description, laboratory, formula, pt.description, quantity, price_sell "
                     + "FROM products p LEFT JOIN section pt ON p.id_section = pt.id  "
                     + "WHERE p.description LIKE ? OR laboratory LIKE ? OR formula LIKE ? OR pt.description LIKE ?";
            
        try(PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query)){
            pstmt.setString(1, "%" + filter + "%");
            pstmt.setString(2, "%" + filter + "%");
            pstmt.setString(3, "%" + filter + "%");
            pstmt.setString(4, "%" + filter + "%");
                        
            ResultSet rs = pstmt.executeQuery();
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();            
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("p.description"),
                    rs.getString("laboratory"),
                    rs.getString("formula"),
                    rs.getString("pt.description"),
                    rs.getInt("quantity"),
                    currencyFormat.format(rs.getDouble("price_sell"))
                };
                model.addRow(row);
            }
            
       } catch (SQLException e){
            e.printStackTrace();
       }
        adjustColumnWidths(); // Ajustar el ancho de las columnas después de cargar los datos
    }

    private void adjustColumnWidths() {
        for (int column = 0; column < tblInventorySearch.getColumnCount(); column++) {
            int width = 15; // Min width

            // Obtener el ancho del encabezado
            TableCellRenderer headerRenderer = tblInventorySearch.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(tblInventorySearch, tblInventorySearch.getColumnModel().getColumn(column).getHeaderValue(), false, false, 0, column);
            width = Math.max(headerComp.getPreferredSize().width + 1, width);

            // Obtener el ancho de las celdas
            for (int row = 0; row < tblInventorySearch.getRowCount(); row++) {
                TableCellRenderer renderer = tblInventorySearch.getCellRenderer(row, column);
                Component comp = tblInventorySearch.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }

            if (width > 3000) {
                width = 300; // Max width
            }
            tblInventorySearch.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        panelFiltro = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnRestock = new javax.swing.JButton();
        panelLogo = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        jScrollPanelResults = new javax.swing.JScrollPane();
        tblInventorySearch = new javax.swing.JTable();

        jButton2.setText("jButton2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblSearch.setText("Buscar");

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnAdd.setText("Nuevo");
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });

        btnCancel.setText("Cancelar");
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });

        btnRestock.setText("Resurtir");
        btnRestock.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRestockMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelFiltroLayout = new javax.swing.GroupLayout(panelFiltro);
        panelFiltro.setLayout(panelFiltroLayout);
        panelFiltroLayout.setHorizontalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelFiltroLayout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addComponent(btnRestock, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        panelFiltroLayout.setVerticalGroup(
            panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFiltroLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(panelFiltroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnRestock)
                    .addComponent(btnCancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

        javax.swing.GroupLayout panelLogoLayout = new javax.swing.GroupLayout(panelLogo);
        panelLogo.setLayout(panelLogoLayout);
        panelLogoLayout.setHorizontalGroup(
            panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelLogoLayout.setVerticalGroup(
            panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        tblInventorySearch.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descripcion", "Laboratorio", "Formula", "Seccion", "Disponibles", "Precio"
            }
        )
        {
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        }

    );
    jScrollPanelResults.setViewportView(tblInventorySearch);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPanelResults)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(panelFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(panelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(panelFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPanelResults, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        String filter = txtSearch.getText();
        loadData(filter);
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        if (btnAdd.isEnabled()) {
            FrmInventoryMod productAdd = new FrmInventoryMod();
            productAdd.setActionType("Add");
            productAdd.setUserId(userID);
            productAdd.setUserRol(userRol);
            productAdd.setVisible(true);
            dispose();
        }        
    }//GEN-LAST:event_btnAddMouseClicked

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        // Crea y muestra el nuevo formulario en el hilo de eventos de Swing
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FrmPOS POS = new FrmPOS();
                POS.setUserRol(userRol);
                POS.initialize();
                POS.setVisible(true);
            }
        });
        // Cierra el formulario actual
        dispose();


    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnRestockMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRestockMouseClicked
        if (btnAdd.isEnabled()) {
            FrmInventoryMod productRestock = new FrmInventoryMod();
            productRestock.setActionType("Restock");
            productRestock.setUserId(userID);
            productRestock.setUserRol(userRol);
            productRestock.setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnRestockMouseClicked

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new FrmInventoryCheck().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnRestock;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPanelResults;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JPanel panelFiltro;
    private javax.swing.JPanel panelLogo;
    private javax.swing.JTable tblInventorySearch;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
