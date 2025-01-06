package Entidades;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

//@author Arturo_Maciel

public class FrmInventoryMod extends javax.swing.JFrame {
    private static FrmInventoryMod instance;
    private Connection conn;
    private String actionType;
    private String userRol;
    private int userID;
    private int productQTY;

    public FrmInventoryMod() {
        openConnection();
        initComponents();
        loadComboBoxData();
    }
       
    public void setActionType(String actionType){
        this.actionType = actionType;
        setTitle(actionType);
    }
    
    public void setUserRol(String userRol){
        this.userRol = userRol;
    }
    
    public void setUserId(int userID){
        this.userID = userID;
    }
    
    private void enableFields(){
        txtDescription.setEnabled(true);
        txtFormula.setEnabled(true);
        txtLaboratory.setEnabled(true);
        txtPriceBuy.setEnabled(true);
        txtPriceSell.setEnabled(true);
        txtQuantity.setEnabled(true);
        cmbType.setEnabled(true);     
        
    }
    
    private void disableFields(){
        txtDescription.setEnabled(false);
        txtFormula.setEnabled(false);
        txtLaboratory.setEnabled(false);
        txtPriceBuy.setEnabled(false);
        txtPriceSell.setEnabled(false);
        txtQuantity.setEnabled(false);
        cmbType.setEnabled(false);  
    }
    
    private void clearFields(){
        txtDescription.setText("");
        txtFormula.setText("");
        txtLaboratory.setText("");
        txtPriceBuy.setText("");
        txtPriceSell.setText("");
        txtQuantity.setText("");
    }
    
    private void openConnection(){        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al abrir la conexion: " + e.getMessage());
        }
    }
    
    private void loadComboBoxData() {
        String query = "SELECT id, description FROM section";
        try (PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
                cmbType.removeAllItems();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    cmbType.addItem(new ComboBoxItem(id, description));
                }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al cargar datos en el comboBox: " + e.getMessage());
        }
    }
    
    public void setTitle(String actionType){
        if("Add".equals(actionType)){
            lblTitle.setText("Nuevo Producto");
        }else if("Modify".equals(actionType)){
            lblTitle.setText("Modificar Producto");            
        }else if("Restock".equals(actionType)){
            lblTitle.setText("Resutir Producto");            
        }
    }
    
    private void findProduct(String barCode){
        String query = "SELECT barcode, description, formula, laboratory, quantity, price_buy, price_sell, id_section "
                     + "FROM products "
                     + "WHERE barcode = ?";
                       
        try (PreparedStatement pstmt =  (PreparedStatement) conn.prepareStatement(query)) {
                pstmt.setString(1, barCode);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    chckEdit.setEnabled(true);
                    txtDescription.setText(rs.getString("description"));
                    txtFormula.setText(rs.getString("formula"));
                    txtLaboratory.setText(rs.getString("laboratory"));
                    txtQuantity.setText(rs.getString("quantity"));
                    txtPriceBuy.setText(rs.getString("price_buy"));
                    txtPriceSell.setText(rs.getString("price_sell"));
                    if ("Restock".equals(actionType)){
                        txtQuantity.setEnabled(true);
                        txtPriceBuy.setEnabled(true);
                        txtPriceSell.setEnabled(true);
                        chckEdit.setEnabled(false);
                        txtQuantity.setText("");
                        setTitle("Resutir Producto");
                    }else{
                        setTitle("Modificar Producto");
                        disableFields();
                    }
                }else{
                    if("Add".equals(actionType)){
                        enableFields();
                        clearFields();
                        txtBarCode.setEnabled(false);
                        
                    }
                }                           
        }catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al buscar el producto", "Error", JOptionPane.ERROR_MESSAGE);
        }       
    }
    
    private class ComboBoxItem {
        private int id;
        private String description;

        public ComboBoxItem(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return description;
        }
    }
 
    public static FrmInventoryMod getInstance(){
        if (instance == null){
            instance = new FrmInventoryMod();
        }
        return instance;
    }
    
    private void loadData(String BarCode, String Description, String Formula, String Laboratory, int Quantity, double PriceBuy, double PriceSell, int IdEmployee, int IdSection){
        int actionId =0;
        
        //Añadir nuevo producto----------------------------------------------------------------------------------------------------------------------------------------------
        if("Add".equals(actionType)){
            actionId = 1;
            String insertQuery = "INSERT INTO products "
                              + "(barcode, description, formula, laboratory, quantity, price_buy, price_sell, id_employee, id_section)"
                              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtAdd =  (PreparedStatement) conn.prepareStatement(insertQuery)) {
                pstmtAdd.setString(1, BarCode);
                pstmtAdd.setString(2, Description);
                pstmtAdd.setString(3, Formula);
                pstmtAdd.setString(4, Laboratory);
                pstmtAdd.setInt(5, Quantity);
                pstmtAdd.setDouble(6, PriceBuy);
                pstmtAdd.setDouble(7, PriceSell);
                pstmtAdd.setInt(8, userID);
                pstmtAdd.setInt(9, IdSection);
                pstmtAdd.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, "Añadido");
                
                clearFields();
                txtBarCode.setEnabled(false);
                disableFields();
                
            }catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error en ejecucion", "Error", JOptionPane.ERROR_MESSAGE);
            } 
        } 
        
        //Modificar producto----------------------------------------------------------------------------------------------------------------------------------------------
        else if("Modify".equals(actionType)){
            actionId = 2;
            String modifyQuery = "UPDATE products "
                               + "SET description = ? , formula = ? , laboratory = ? , quantity = ? , price_buy = ? , price_sell = ? , id_employee = ? , id_section  = ? "
                               + "WHERE barcode = ?";
        
            try (PreparedStatement pstmtModify =  (PreparedStatement) conn.prepareStatement(modifyQuery)) {
                pstmtModify.setString(1, Description);
                pstmtModify.setString(2, Formula);
                pstmtModify.setString(3, Laboratory);
                pstmtModify.setInt(4, Quantity);
                pstmtModify.setDouble(5, PriceBuy);
                pstmtModify.setDouble(6, PriceSell);
                pstmtModify.setInt(7, userID);
                pstmtModify.setInt(8, IdSection);
                pstmtModify.setString(9, BarCode);
                pstmtModify.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, "Acualizado");
                txtBarCode.setText("");
                clearFields();
                disableFields();
                
            }catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
            }             
        } 
        
        //Resuritor producto----------------------------------------------------------------------------------------------------------------------------------------------
        else if("Restock".equals(actionType)){
            actionId = 3;   
            txtQuantity.setText("");
            String queryQTY = "SELECT quantity FROM products WHERE barcode = ?";
                    
            try (PreparedStatement pstmtQty = (PreparedStatement) conn.prepareStatement(queryQTY)) {
                pstmtQty.setString(1, BarCode);
                ResultSet rs = pstmtQty.executeQuery();
                if (rs.next()) {
                    productQTY = rs.getInt("quantity");
                }
            }catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al dar de alta", "Error", JOptionPane.ERROR_MESSAGE);
            }
            Quantity = Quantity + productQTY;
            
            String restockQuery = "UPDATE products "
                                + "SET quantity = ? , price_buy = ?, price_sell = ?, id_employee = ?, id_section = ? "
                                + "WHERE barcode = ?";
        
            try (PreparedStatement pstmtRestock =  (PreparedStatement) conn.prepareStatement(restockQuery)) {
                pstmtRestock.setInt(1, Quantity);
                pstmtRestock.setDouble(2, PriceBuy);
                pstmtRestock.setDouble(3, PriceSell);
                pstmtRestock.setInt(4, userID);
                pstmtRestock.setInt(5, IdSection);
                pstmtRestock.setString(6, BarCode);
                pstmtRestock.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, "Acualizado");
                clearFields();
                txtBarCode.setText("");
                
            }catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al resurtir", "Error", JOptionPane.ERROR_MESSAGE);
            }             
        }
        
        String queryHistory = "INSERT INTO history_products (id_employee, description, formula, barcode, laboratory, id_section, quantity, price_buy, price_sell,action_id)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        
        try (PreparedStatement pstmth =  (PreparedStatement) conn.prepareStatement(queryHistory)) {
            pstmth.setInt(1, userID);
            pstmth.setString(2, Description);
            pstmth.setString(3, Formula);
            pstmth.setString(4, BarCode);
            pstmth.setString(5, Laboratory);
            pstmth.setInt(6, IdSection);
            pstmth.setInt(7, Quantity);
            pstmth.setDouble(8, PriceBuy);
            pstmth.setDouble(9, PriceSell);
            pstmth.setInt(10, actionId);
            pstmth.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al registrar historial", "Error", JOptionPane.ERROR_MESSAGE);
        } 
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblBarCode = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        lblFormula = new javax.swing.JLabel();
        lblLaboratory = new javax.swing.JLabel();
        lblQuantity = new javax.swing.JLabel();
        lblPriceBuy = new javax.swing.JLabel();
        lblPriceSell = new javax.swing.JLabel();
        txtBarCode = new javax.swing.JTextField();
        txtDescription = new javax.swing.JTextField();
        txtFormula = new javax.swing.JTextField();
        txtLaboratory = new javax.swing.JTextField();
        txtQuantity = new javax.swing.JTextField();
        txtPriceBuy = new javax.swing.JTextField();
        txtPriceSell = new javax.swing.JTextField();
        btnAcept = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cmbType = new javax.swing.JComboBox();
        lblType = new javax.swing.JLabel();
        chckEdit = new javax.swing.JCheckBox();
        lblTitle = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblBarCode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBarCode.setText("Codigo de Barras");

        lblName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblName.setText("Descripcion");

        lblFormula.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblFormula.setText("Formula");

        lblLaboratory.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblLaboratory.setText("Laboratorio");

        lblQuantity.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblQuantity.setText("Cantidad");

        lblPriceBuy.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPriceBuy.setText("Precio Compra");

        lblPriceSell.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPriceSell.setText("Precio Venta");

        txtBarCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBarCodeKeyPressed(evt);
            }
        });

        txtDescription.setEnabled(false);

        txtFormula.setEnabled(false);

        txtLaboratory.setEnabled(false);

        txtQuantity.setEnabled(false);

        txtPriceBuy.setEnabled(false);

        txtPriceSell.setEnabled(false);

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

        cmbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbType.setEnabled(false);

        lblType.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblType.setText("Seccion");

        chckEdit.setText("Editar");
        chckEdit.setEnabled(false);
        chckEdit.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chckEditStateChanged(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N

        btnClear.setText("Limpiar");
        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnClearMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chckEdit)
                        .addGap(37, 37, 37)
                        .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAcept, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblQuantity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblPriceSell, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblPriceBuy, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(txtPriceBuy, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(txtPriceSell, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)))
                            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblBarCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblLaboratory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblFormula, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtFormula, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                        .addComponent(txtLaboratory, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                        .addComponent(cmbType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtBarCode, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cmbType, txtBarCode, txtDescription, txtFormula, txtLaboratory, txtPriceBuy, txtPriceSell, txtQuantity});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBarCode)
                    .addComponent(txtBarCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFormula)
                    .addComponent(txtFormula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLaboratory)
                    .addComponent(txtLaboratory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuantity)
                    .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPriceBuy)
                    .addComponent(txtPriceBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPriceSell)
                    .addComponent(txtPriceSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnAcept)
                    .addComponent(chckEdit)
                    .addComponent(btnClear))
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAceptMouseClicked
        String barCodeText = txtBarCode.getText().trim();
        String formula = txtFormula.getText().toUpperCase().trim();
        String laboratory = txtLaboratory.getText().toUpperCase().trim();
        String description = txtDescription.getText().toUpperCase().trim();
        String quantityText = txtQuantity.getText();
        String priceBuyText = txtPriceBuy.getText();
        String priceSellText = txtPriceSell.getText();
        ComboBoxItem selectedItem = (ComboBoxItem) cmbType.getSelectedItem();
        int IdType = selectedItem.getId();

        if (!formula.equals("") && !barCodeText.equals("") && !laboratory.equals("") && !description.equals("") && !quantityText.equals("") && !priceBuyText.equals("") && !priceSellText.equals("")) {
            try{
                String barCode = barCodeText;
                int quantity = Integer.parseInt(quantityText);
                double priceBuy = Double.parseDouble(priceBuyText);
                double priceSell = Double.parseDouble(priceSellText);
                loadData(barCode, description, formula, laboratory, quantity, priceBuy, priceSell, userID, IdType);
                txtBarCode.requestFocus();
                txtBarCode.setText("");
                txtBarCode.setEnabled(true);
                chckEdit.setSelected(false);
                chckEdit.setEnabled(false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos para Código de Barras, Cantidad, Precio de Compra y Precio de Venta.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
            
        } else {
            JOptionPane.showMessageDialog(this, "Todos los campos deben estar llenos", "Error", JOptionPane.ERROR_MESSAGE);
        }      
      
    }//GEN-LAST:event_btnAceptMouseClicked

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        
        // Abre el nuevo formulario
        FrmPOS POS = new FrmPOS();  
        POS.setUserRol(userRol);
        POS.setUserId(userID);
        POS.initialize();
        POS.setVisible(true);
        // Cierra el formulario actual
        dispose();
        
    }//GEN-LAST:event_btnCancelMouseClicked

    private void txtBarCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBarCodeKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            
            String barCodeText = txtBarCode.getText().trim();
            if (!"".equals(barCodeText)){
                findProduct(barCodeText);
            }
            
        }
    }//GEN-LAST:event_txtBarCodeKeyPressed
    
    private void chckEditStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chckEditStateChanged
        if(chckEdit.isSelected() && !"Restock".equals(actionType)){
            enableFields(); 
            actionType = "Modify";
            txtBarCode.setEnabled(false);
        }else if (!"Restock".equals(actionType)){
            disableFields();
            actionType = "Add";            
        }
        setTitle(actionType);
        
    }//GEN-LAST:event_chckEditStateChanged

    private void btnClearMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnClearMouseClicked
        clearFields();
        txtBarCode.setEnabled(true);
        disableFields();
        chckEdit.setSelected(false);
        txtBarCode.setText("");
        txtBarCode.requestFocus();        
    }//GEN-LAST:event_btnClearMouseClicked

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
            java.util.logging.Logger.getLogger(FrmInventoryMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmInventoryMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmInventoryMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmInventoryMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new FrmInventoryMod().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcept;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClear;
    private javax.swing.JCheckBox chckEdit;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel lblBarCode;
    private javax.swing.JLabel lblFormula;
    private javax.swing.JLabel lblLaboratory;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPriceBuy;
    private javax.swing.JLabel lblPriceSell;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblType;
    private javax.swing.JTextField txtBarCode;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtFormula;
    private javax.swing.JTextField txtLaboratory;
    private javax.swing.JTextField txtPriceBuy;
    private javax.swing.JTextField txtPriceSell;
    private javax.swing.JTextField txtQuantity;
    // End of variables declaration//GEN-END:variables
}
