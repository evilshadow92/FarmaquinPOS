/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Entidades;

import java.awt.event.KeyEvent;
import com.mysql.jdbc.PreparedStatement;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/*@author Arturo_Maciel */
public class FrmPOS extends javax.swing.JFrame {
    private FrmInventoryMod productModForm;
    private FrmInventoryCheck inventoryCheckForm;
    private FrmProfits reportForm;
    private FrmLogin loginForm;
    private FrmBoxCut boxCutForm;
    private Connection conn;
    private String userRol;
    private int userID;

    public void setUserRol(String userRol){
        this.userRol = userRol;
    }
    
    public void setUserId(int userID){
        this.userID = userID;
    }
    
    public void initialize() {
        if ("ADMINISTRATOR".equals(userRol)) {
            mnAddProduct.setEnabled(true);
            mnMod.setEnabled(true);
            mnRestock.setEnabled(true);
            mnProfits.setEnabled(true);
            mnExpenses.setEnabled(true);
            mnBuySell.setEnabled(true);
            mnBoxCutReport.setEnabled(true);
            mnModUser.setEnabled(true);
            mnAddUser.setEnabled(true);

            mnAddProduct.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    mnAddProductActionPerformed(evt);
                }
            });
            
            mnMod.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    mnModActionPerformed(evt);
                }
            });
            
            mnRestock.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    mnRestockActionPerformed(evt);
                }
            });
            
            mnProfits.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    mnReportActionPerformed(evt);
                }
            });
        } else {            
            mnAddProduct.setEnabled(false);
            mnMod.setEnabled(false);
            mnRestock.setEnabled(false);
            mnProfits.setEnabled(false);
            mnExpenses.setEnabled(false);
            mnBuySell.setEnabled(false);
            mnBoxCutReport.setEnabled(false);
            mnModUser.setEnabled(false);
            mnAddUser.setEnabled(false);
        }
        
        mnInventory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mnInventoryActionPerformed(evt);
            }
        });
        
        mnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                mnLoginActionPerformed(evt);
            }
        });
        txtBarCode.requestFocus();

    }
    
    public FrmPOS() {
        initComponents();
        openConnection();
        initialize();
        adjustColumnWidths();
    }
    
    private void adjustColumnWidths() {
         for (int column = 0; column < tblPOS.getColumnCount(); column++) {
             int width = 5; // Min width

             // Obtener el ancho del encabezado
             TableCellRenderer headerRenderer = tblPOS.getTableHeader().getDefaultRenderer();
             Component headerComp = headerRenderer.getTableCellRendererComponent(tblPOS, tblPOS.getColumnModel().getColumn(column).getHeaderValue(), false, false, 0, column);
             width = Math.max(headerComp.getPreferredSize().width + 1, width);

             // Obtener el ancho de las celdas
             for (int row = 0; row < tblPOS.getRowCount(); row++) {
                 TableCellRenderer renderer = tblPOS.getCellRenderer(row, column);
                 Component comp = tblPOS.prepareRenderer(renderer, row, column);
                 width = Math.max(comp.getPreferredSize().width + 1, width);
             }

             if (width > 1000) {
                 width = 300; // Max width
             }
             tblPOS.getColumnModel().getColumn(column).setPreferredWidth(width);
         }
     }
    
    private void openConnection() {
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {            
            JOptionPane.showMessageDialog(rootPane, "Error al abrir la conexion: " + e.getMessage());
        }
    }
    
    private void loadData(String barCode){
        DefaultTableModel model = (DefaultTableModel) tblPOS.getModel();
        
        double total;
        String query = "Select description, formula, quantity, price_sell "
                     + "FROM products "
                     + "WHERE barCode = ?";
        
        try(PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query)){
            pstmt.setString(1, barCode);
            
            ResultSet rs = pstmt.executeQuery();
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(); 
            
            if(rs.next()){
                String description = rs.getString("description");
                String formula = rs.getString("formula");
                double priceSell = rs.getDouble("price_sell");   
                int quantity = rs.getInt("quantity");
                int currentQuantity = 0;
                boolean found = false;
                
                //suma 1 si ya se registro y si no, crea una fila
                for(int i = 0; i < model.getRowCount(); i++){
                    if(model.getValueAt(i, 0).equals(description)){
                        currentQuantity = (int) model.getValueAt(i, 2);
                        if((currentQuantity + 1) <= quantity){
                            model.setValueAt(currentQuantity + 1, i, 2);
                            found = true;
                            break;
                        } else{
                            JOptionPane.showMessageDialog(rootPane, "Ingresar " + description + " excede las existencias: " + quantity);                            
                            found = true;
                            break;
                        }
                    }
                }
                if(!found){
                    if(quantity != 0){
                        currentQuantity = 1;
                        Object[] row = {
                            description,
                            formula,
                            currentQuantity,
                            currencyFormat.format(priceSell)
                        };
                        model.addRow(row);
                    } else{
                        JOptionPane.showMessageDialog(rootPane, "Ingresar " + description + " excede las existencias: " + quantity);
                    }
                }
                    
                
                //calcula cuanto cobrar
                total = 0.0;
                for(int i = 0; i < model.getRowCount(); i++){
                    int rowQuantity = (int) model.getValueAt(i, 2);
                    String priceString = (String) model.getValueAt(i, 3);
                    try {
                        double rowPriceSell = currencyFormat.parse(priceString).doubleValue();
                        total += rowPriceSell * rowQuantity;
                    } catch (ParseException e) {
                        JOptionPane.showMessageDialog(rootPane, "Error al parsear el precio: " + e.getMessage());
                    }
                }
                txtTotal.setText(currencyFormat.format(total));            
                
            }
            
            
        }catch (SQLException e){
            JOptionPane.showMessageDialog(rootPane, "Error al cargar el producto: " + e.getMessage());
        }
        adjustColumnWidths();
        
    }
    
    private void recordTransaction(){
        int IdEmployee = 1;
        String barCode = "";
        String laboratory = "";
        int idSection = 0;
        double priceBuy = 0.0;
        
        DefaultTableModel model = (DefaultTableModel) tblPOS.getModel();
                
        for(int i = 0; i < model.getRowCount(); i++){
            String description = (String) model.getValueAt(i, 0);
            String formula = (String) model.getValueAt(i, 1);
            int currentQuantity = (int) model.getValueAt(i, 2);
            int quantity = 0;
            int newQuantity = 0;
            String priceString = (String) model.getValueAt(i, 3);
            double priceSell = 0.0;
            
            try {
                priceSell = NumberFormat.getCurrencyInstance().parse(priceString).doubleValue();
            } catch (ParseException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al parsear el precio: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            String queryFind = "Select barcode, laboratory, id_section, price_buy, quantity "
                             + "FROM products "
                             + "WHERE description = ? AND formula = ?";
            try (PreparedStatement pstmtf = (PreparedStatement) conn.prepareStatement(queryFind)){
                pstmtf.setString(1, description);
                pstmtf.setString(2, formula);
                
                ResultSet rs = pstmtf.executeQuery();
                while (rs.next()) {
                    barCode = rs.getString("barcode");
                    laboratory = rs.getString("laboratory");
                    idSection = rs.getInt("id_section");
                    priceBuy = rs.getDouble("price_buy");
                    quantity = rs.getInt("quantity");
                }
                JOptionPane.showMessageDialog(rootPane, "Transaccion exitosa");
            } catch (SQLException e) {
                e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al buscar el producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
            newQuantity = quantity - currentQuantity;
            
            String queryUpdate = "UPDATE products SET quantity = ? WHERE barcode = ?";
            try (PreparedStatement pstmtu = (PreparedStatement) conn.prepareStatement(queryUpdate)){
                pstmtu.setInt(1, newQuantity);
                pstmtu.setString(2, barCode);
                pstmtu.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al actualizar el inventario ", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            
            String queryHistory = "INSERT INTO history_products "
                                + "(id_employee, description, formula, barcode, laboratory, id_section, quantity, price_buy, price_sell,action_id) "
                                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";      
            try (PreparedStatement pstmth = (PreparedStatement) conn.prepareStatement(queryHistory)) {
                pstmth.setInt(1, IdEmployee);
                pstmth.setString(2, description);
                pstmth.setString(3, formula);
                pstmth.setString(4, barCode);
                pstmth.setString(5, laboratory);
                pstmth.setInt(6, idSection);
                pstmth.setInt(7, currentQuantity);
                pstmth.setDouble(8, priceBuy);
                pstmth.setDouble(9, priceSell);
                pstmth.setDouble(10, 4);
                pstmth.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al registrar historial", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            
        }
        
        model.setRowCount(0);
        txtTotal.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblPOS = new javax.swing.JTable();
        btnAceptar = new javax.swing.JButton();
        txtBarCode = new javax.swing.JTextField();
        btnClear = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        lblCash = new javax.swing.JLabel();
        lblReturn = new javax.swing.JLabel();
        txtCash = new javax.swing.JTextField();
        txtChange = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnUsers = new javax.swing.JMenu();
        mnLogin = new javax.swing.JMenuItem();
        mnModUser = new javax.swing.JMenuItem();
        mnAddUser = new javax.swing.JMenuItem();
        mnProducts = new javax.swing.JMenu();
        mnInventory = new javax.swing.JMenuItem();
        mnAddProduct = new javax.swing.JMenuItem();
        mnMod = new javax.swing.JMenuItem();
        mnRestock = new javax.swing.JMenuItem();
        mnAdmin = new javax.swing.JMenu();
        mnBoxCut = new javax.swing.JMenuItem();
        mnProfits = new javax.swing.JMenuItem();
        mnBuySell = new javax.swing.JMenuItem();
        mnExpenses = new javax.swing.JMenuItem();
        mnBoxCutReport = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tblPOS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descripcion", "Formula", "Cantidad", "Precio"
            }
        )
        {
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        }
    );
    jScrollPane1.setViewportView(tblPOS);

    btnAceptar.setText("Aceptar");
    btnAceptar.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnAceptarMouseClicked(evt);
        }
    });

    txtBarCode.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            txtBarCodeKeyPressed(evt);
        }
    });

    btnClear.setText("Limpiar");
    btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            btnClearMouseClicked(evt);
        }
    });

    lblTotal.setText("TOTAL");

    txtTotal.setEditable(false);

    lblCash.setText("EFECTIVO");

    lblReturn.setText("VUELTO");

    txtCash.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            txtCashActionPerformed(evt);
        }
    });

    txtChange.setEditable(false);

    jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

    mnUsers.setText("Usuarios");

    mnLogin.setText("Login");
    mnLogin.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnLoginActionPerformed(evt);
        }
    });
    mnUsers.add(mnLogin);

    mnModUser.setText("Modificar");
    mnModUser.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnModUserActionPerformed(evt);
        }
    });
    mnUsers.add(mnModUser);

    mnAddUser.setText("Añadir");
    mnAddUser.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnAddUserActionPerformed(evt);
        }
    });
    mnUsers.add(mnAddUser);

    jMenuBar1.add(mnUsers);

    mnProducts.setText("Productos");

    mnInventory.setText("Inventario");
    mnInventory.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnInventoryActionPerformed(evt);
        }
    });
    mnProducts.add(mnInventory);

    mnAddProduct.setText("Añadir");
    mnAddProduct.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnAddProductActionPerformed(evt);
        }
    });
    mnProducts.add(mnAddProduct);

    mnMod.setText("Modificar");
    mnMod.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnModActionPerformed(evt);
        }
    });
    mnProducts.add(mnMod);

    mnRestock.setText("Resutir");
    mnRestock.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnRestockActionPerformed(evt);
        }
    });
    mnProducts.add(mnRestock);

    jMenuBar1.add(mnProducts);

    mnAdmin.setText("Administracion");

    mnBoxCut.setText("Corte de Caja");
    mnBoxCut.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnBoxCutActionPerformed(evt);
        }
    });
    mnAdmin.add(mnBoxCut);

    mnProfits.setText("Ganancias");
    mnAdmin.add(mnProfits);

    mnBuySell.setText("Compras/Ventas");
    mnBuySell.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnBuySellActionPerformed(evt);
        }
    });
    mnAdmin.add(mnBuySell);

    mnExpenses.setText("Gastos");
    mnExpenses.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnExpensesActionPerformed(evt);
        }
    });
    mnAdmin.add(mnExpenses);

    mnBoxCutReport.setText("Ver Cortes de Caja");
    mnBoxCutReport.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            mnBoxCutReportActionPerformed(evt);
        }
    });
    mnAdmin.add(mnBoxCutReport);

    jMenuBar1.add(mnAdmin);

    setJMenuBar(jMenuBar1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblTotal)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblCash, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(txtCash, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblReturn, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(txtChange, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnAceptar)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(txtBarCode, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createSequentialGroup()
                    .addGap(14, 14, 14)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
    );

    layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblCash, lblReturn, lblTotal});

    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(18, 18, 18)
                    .addComponent(txtBarCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAceptar)
                        .addComponent(btnClear))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTotal)
                        .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCash)
                        .addComponent(txtCash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblReturn)
                        .addComponent(txtChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(32, 32, 32))))
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtBarCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBarCodeKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            String barCodeText = txtBarCode.getText().trim();
            if (!"".equals(barCodeText)){
                loadData(barCodeText);
                txtBarCode.setText("");
            }
            
        }
    }//GEN-LAST:event_txtBarCodeKeyPressed

    private void btnAceptarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAceptarMouseClicked
        if(txtChange.getText() != null && !"".equals(txtChange.getText())){
            recordTransaction();
        }else{
            JOptionPane.showMessageDialog(this, "Ingresa el efectivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAceptarMouseClicked

    private void mnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnAddProductActionPerformed
        if(productModForm == null || !productModForm.isVisible()){
            productModForm = new FrmInventoryMod();
            productModForm.setActionType("Add");
            productModForm.setUserRol(userRol);
            productModForm.setUserId(userID);
            productModForm.setVisible(true);
        }else{
            productModForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnAddProductActionPerformed

    private void mnInventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnInventoryActionPerformed
        if (inventoryCheckForm == null || !inventoryCheckForm.isVisible()) {
            inventoryCheckForm = new FrmInventoryCheck();
            inventoryCheckForm.setUserRol(userRol);
            inventoryCheckForm.setUserId(userID);
            inventoryCheckForm.initialize();
            inventoryCheckForm.setVisible(true);
        }else{
            inventoryCheckForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnInventoryActionPerformed

    private void mnModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnModActionPerformed
        if(productModForm == null || !productModForm.isVisible()){
            productModForm = new FrmInventoryMod();
            productModForm.setActionType("Modify");
            productModForm.setUserRol(userRol);
            productModForm.setUserId(userID);
            productModForm.setVisible(true);
        }else{
            productModForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnModActionPerformed

    private void mnRestockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnRestockActionPerformed
        if(productModForm == null || !productModForm.isVisible()){
            productModForm = new FrmInventoryMod();
            productModForm.setActionType("Restock");
            productModForm.setUserRol(userRol);
            productModForm.setUserId(userID);
            productModForm.setVisible(true);
        }else{
            productModForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnRestockActionPerformed

    private void mnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnLoginActionPerformed
        if(loginForm == null || !loginForm.isVisible()){
            loginForm = new FrmLogin();
            loginForm.setVisible(true);
        }else{
            loginForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnLoginActionPerformed

    private void txtCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCashActionPerformed
        try {
            double total = Double.parseDouble(txtTotal.getText().trim().replace("$", ""));
            double cash = Double.parseDouble(txtCash.getText().trim().replace("$", ""));

            // Verificar si el efectivo cubre el total
            if (cash >= total) {
                double change = cash - total; // Calcular el cambio
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(); 
                txtChange.setText(currencyFormat.format(change)); // Mostrar el cambio formateado como moneda
            } else {
                JOptionPane.showMessageDialog(rootPane, "Efectivo insuficiente", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            // Capturar errores de formato en caso de que el texto no sea válido
            JOptionPane.showMessageDialog(rootPane, "Por favor ingrese valores válidos en formato de moneda.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_txtCashActionPerformed

    private void btnClearMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnClearMouseClicked
        txtTotal.setText("");
        txtCash.setText("");
        txtChange.setText("");
        DefaultTableModel model = (DefaultTableModel) tblPOS.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_btnClearMouseClicked

    private void mnBoxCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnBoxCutActionPerformed
        if(boxCutForm == null || !boxCutForm.isVisible()){
            boxCutForm = new FrmBoxCut();
            boxCutForm.setUserRol(userRol);
            boxCutForm.setUserId(userID);
            boxCutForm.setVisible(true);
        }else{
            boxCutForm.toFront();
        }
        dispose();
    }//GEN-LAST:event_mnBoxCutActionPerformed

    private void mnExpensesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnExpensesActionPerformed
        FrmExpenses Expenses = new FrmExpenses();
        Expenses.setUserId(userID);
        Expenses.setUserRol(userRol);
        Expenses.setVisible(true);
        dispose();
    }//GEN-LAST:event_mnExpensesActionPerformed

    private void mnBuySellActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnBuySellActionPerformed
        FrmBuySell BuySell = new FrmBuySell();
        BuySell.setUserId(userID);
        BuySell.setUserRol(userRol);
        BuySell.setVisible(true);
        dispose(); 
    }//GEN-LAST:event_mnBuySellActionPerformed

    private void mnBoxCutReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnBoxCutReportActionPerformed
        FrmBoxCutReport boxCutReport = new FrmBoxCutReport();
        boxCutReport.setUserId(userID);
        boxCutReport.setUserRol(userRol);
        boxCutReport.setVisible(true);
        dispose();
    }//GEN-LAST:event_mnBoxCutReportActionPerformed

    private void mnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnAddUserActionPerformed
        FrmUserMod userMod = new FrmUserMod();
        userMod.setActionType("Add");
        userMod.setUserRol(userRol);
        userMod.setUserId(userID);
        userMod.setVisible(true);

        dispose();
    }//GEN-LAST:event_mnAddUserActionPerformed

    private void mnModUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnModUserActionPerformed
        FrmUserMod userMod = new FrmUserMod();
        userMod.setActionType("Modify");
        userMod.setUserRol(userRol);
        userMod.setUserId(userID);
        userMod.setVisible(true);

        dispose();
    }//GEN-LAST:event_mnModUserActionPerformed
    private void mnReportActionPerformed(java.awt.event.ActionEvent evt) {                                         
        if(reportForm == null || !reportForm.isVisible()){
            reportForm = new FrmProfits();
            reportForm.setUserRol(userRol);
            reportForm.setUserId(userID);
            reportForm.setVisible(true);
        }else{
            reportForm.toFront();
        }
        dispose();
    }
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
            java.util.logging.Logger.getLogger(FrmPOS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmPOS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmPOS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmPOS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmPOS().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnClear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCash;
    private javax.swing.JLabel lblReturn;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JMenuItem mnAddProduct;
    private javax.swing.JMenuItem mnAddUser;
    private javax.swing.JMenu mnAdmin;
    private javax.swing.JMenuItem mnBoxCut;
    private javax.swing.JMenuItem mnBoxCutReport;
    private javax.swing.JMenuItem mnBuySell;
    private javax.swing.JMenuItem mnExpenses;
    private javax.swing.JMenuItem mnInventory;
    private javax.swing.JMenuItem mnLogin;
    private javax.swing.JMenuItem mnMod;
    private javax.swing.JMenuItem mnModUser;
    private javax.swing.JMenu mnProducts;
    private javax.swing.JMenuItem mnProfits;
    private javax.swing.JMenuItem mnRestock;
    private javax.swing.JMenu mnUsers;
    private javax.swing.JTable tblPOS;
    private javax.swing.JTextField txtBarCode;
    private javax.swing.JTextField txtCash;
    private javax.swing.JTextField txtChange;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables
}
