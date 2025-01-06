package Entidades;
import com.mysql.jdbc.PreparedStatement;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

//@author Arturo_Maciel

public class FrmUserMod extends javax.swing.JFrame {
    private static FrmUserMod instance;
    private Connection conn;
    private String actionType;
    private String userRol;
    private int userID;

    public FrmUserMod() {
        openConnection();
        initComponents();
        loadShiftData();
        loadRolData();
    }
       
    public void setActionType(String actionType){
        this.actionType = actionType;
        setTitle(actionType);
        if(actionType.equals("Add")){
            enableFields();
            txtEmpId.setEnabled(false);
        }
        else if(actionType.equals("Modify")){
            disableFields();
        }
    }
    
    public void setUserRol(String userRol){
        this.userRol = userRol;
    }
    
    public void setUserId(int userID){
        this.userID = userID;
    }
    
    private void enableFields(){
        txtName.setEnabled(true);
        cmbRol.setEnabled(true);
        txtUser.setEnabled(true);
        txtPassword.setEnabled(true);
        txtPhoneNumber.setEnabled(true);
        txtAddress.setEnabled(true);
        cmbShift.setEnabled(true);  
        txtDailySalary.setEnabled(true);  
    }
    
    private void disableFields(){
        txtName.setEnabled(false);
        cmbRol.setEnabled(false);
        txtUser.setEnabled(false);
        txtPassword.setEnabled(false);
        txtPhoneNumber.setEnabled(false);
        txtAddress.setEnabled(false);
        cmbShift.setEnabled(false);  
        txtDailySalary.setEnabled(false);
    }
    
    private void clearFields(){
        cmbRol.setSelectedIndex(1);
        txtUser.setText("");
        txtPassword.setText("");
        txtPhoneNumber.setText("");
        txtAddress.setText("");
        txtDailySalary.setText("");
        cmbShift.setSelectedIndex(1);
    }
    
    private void openConnection(){        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al abrir la conexion: " + e.getMessage());
        }
    }
    
    private void loadRolData() {
        String query = "SELECT id, description FROM rols";
        try (PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
                cmbRol.removeAllItems();
                cmbRol.addItem(new ComboBoxItem(1, "DEFAULT"));
                while (rs.next()) {
                    int id = rs.getInt("id") + 1;
                    String description = rs.getString("description");
                    cmbRol.addItem(new ComboBoxItem(id, description));
                }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al cargar datos en shifts: " + e.getMessage());
        }
    }    
    
    private void loadShiftData() {
        String query = "SELECT id, description FROM shifts";
        try (PreparedStatement pstmt = (PreparedStatement) conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
                cmbShift.removeAllItems();
                cmbShift.addItem(new ComboBoxItem(1, "DEFAULT"));

                while (rs.next()) {
                    int id = rs.getInt("id") + 1;
                    String description = rs.getString("description");
                    cmbShift.addItem(new ComboBoxItem(id, description));
                }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Error al cargar datos en shifts: " + e.getMessage());
        }
    }
    
    @Override
    public void setTitle(String actionType){
        if("Add".equals(actionType)){
            lblTitle.setText("Nuevo Empleado");
        }else if("Modify".equals(actionType)){
            lblTitle.setText("Modificar Empleado");            
        }
    }
    
    private void findEmployee(String id){
        String query = "SELECT id, name, id_rol, user, phone_number, address, shift_id, daily_payment, status "
                     + "FROM employee "
                     + "WHERE id = ?";
                       
        try (PreparedStatement pstmt =  (PreparedStatement) conn.prepareStatement(query)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    txtName.setText(rs.getString("name"));
                    cmbRol.setSelectedIndex(rs.getInt("id_rol"));
                    txtUser.setText(rs.getString("user"));
                    txtPhoneNumber.setText(rs.getString("phone_number"));
                    txtAddress.setText(rs.getString("address"));
                    cmbShift.setSelectedIndex(rs.getInt("shift_id"));
                    txtDailySalary.setText(rs.getString("daily_payment"));
                } 
                enableFields();
                txtEmpId.setEnabled(false);
        }catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al buscar el Empleado", "Error", JOptionPane.ERROR_MESSAGE);
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
 
    public static FrmUserMod getInstance(){
        if (instance == null){
            instance = new FrmUserMod();
        }
        return instance;
    }
    
    private void loadData(String name, int id_rol, String user, String password, long phone_number, String address, int shift_id, Double daily_payment){
        //Añadir nuevo producto----------------------------------------------------------------------------------------------------------------------------------------------
        if("Add".equals(actionType)){
            String insertQuery = "INSERT INTO employee "
                              + "(name, id_rol, user, password, phone_number, address, shift_id, daily_payment)"
                              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtAdd =  (PreparedStatement) conn.prepareStatement(insertQuery)) {
                pstmtAdd.setString(1, name);
                pstmtAdd.setInt(2, id_rol - 1);
                pstmtAdd.setString(3, user);
                pstmtAdd.setString(4, password);
                pstmtAdd.setLong(5, phone_number);
                pstmtAdd.setString(6, address);
                pstmtAdd.setInt(7, shift_id - 1);
                pstmtAdd.setDouble(8, daily_payment);
                pstmtAdd.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, "Añadido");
                
                txtName.setText("");
                clearFields();
                txtName.setEnabled(false);
                disableFields();
                
            }catch (SQLException e) {
                e.printStackTrace();
                String errorMessage = e.getMessage();

                if (errorMessage.contains("unique constraint") || errorMessage.contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(this, "Error: El usuario ya existe.", "Error de Restricción Única", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error en ejecución: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } 
        } 
        
        //Modificar producto----------------------------------------------------------------------------------------------------------------------------------------------
        else if("Modify".equals(actionType)){
            txtEmpId.setEnabled(true);
            String modifyQuery = "UPDATE employee SET name = ?, id_rol = ?, user = ?, phone_number = ?, address = ?, shift_id = ?, daily_payment = ?";
            
            // Verifica si se ha proporcionado una contraseña
            if (!password.isEmpty()) {
                modifyQuery += ", password = ?";
            }

            modifyQuery += " WHERE id = ?";

            try (PreparedStatement pstmtModify =  (PreparedStatement) conn.prepareStatement(modifyQuery)){
                pstmtModify.setString(1, name);
                pstmtModify.setInt(2, id_rol - 1);
                pstmtModify.setString(3, user);
                pstmtModify.setLong(4, phone_number);
                pstmtModify.setString(5, address);
                pstmtModify.setInt(6, shift_id - 1);
                pstmtModify.setDouble(7, daily_payment);
                
                
                int index = 8;
                if (!password.isEmpty()) {
                    pstmtModify.setString(index++, password);
                }

                pstmtModify.setString(index, txtEmpId.getText()); 
                pstmtModify.executeUpdate();
                
                JOptionPane.showMessageDialog(rootPane, "Acualizado");
                txtName.setText("");
                clearFields();
                disableFields();
                txtName.setEnabled(false);
            }catch (SQLException e) {
                
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar", "Error", JOptionPane.ERROR_MESSAGE);
            }             
        } 
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        lblPhoneNumber = new javax.swing.JLabel();
        lblAdress = new javax.swing.JLabel();
        lblShift = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtUser = new javax.swing.JTextField();
        txtPhoneNumber = new javax.swing.JTextField();
        txtAddress = new javax.swing.JTextField();
        btnAcept = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cmbShift = new javax.swing.JComboBox();
        lblRol = new javax.swing.JLabel();
        chckEdit = new javax.swing.JCheckBox();
        btnClear = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();
        cmbRol = new javax.swing.JComboBox();
        lblIcon = new javax.swing.JLabel();
        lblDailySalary = new javax.swing.JLabel();
        txtDailySalary = new javax.swing.JTextField();
        lblTitle = new javax.swing.JLabel();
        lblEmpId = new javax.swing.JLabel();
        txtEmpId = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblName.setText("Nombre");

        lblUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUser.setText("Usuario");

        lblPassword.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPassword.setText("Password");

        lblPhoneNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPhoneNumber.setText("Telefono");

        lblAdress.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAdress.setText("Direccion");

        lblShift.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblShift.setText("Shift");

        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtNameKeyPressed(evt);
            }
        });

        txtUser.setEnabled(false);

        txtPhoneNumber.setEnabled(false);

        txtAddress.setEnabled(false);

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

        cmbShift.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbShift.setEnabled(false);

        lblRol.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRol.setText("Rol");

        chckEdit.setText("Editar");
        chckEdit.setEnabled(false);
        chckEdit.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chckEditStateChanged(evt);
            }
        });

        btnClear.setText("Limpiar");
        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnClearMouseClicked(evt);
            }
        });

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbRol.setEnabled(false);

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/WhatsApp Image 2024-11-16 at 4.21.23 PM.jpeg"))); // NOI18N

        lblDailySalary.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDailySalary.setText("Salario Diario");

        lblTitle.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N

        lblEmpId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblEmpId.setText("No. de Empleado");

        txtEmpId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtEmpIdKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblShift, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDailySalary, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbShift, 0, 146, Short.MAX_VALUE)
                            .addComponent(txtDailySalary, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblPhoneNumber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblAdress, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPhoneNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                    .addComponent(lblRol, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                    .addComponent(lblEmpId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtUser, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                                    .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                                    .addComponent(cmbRol, 0, 146, Short.MAX_VALUE)
                                    .addComponent(txtEmpId)))
                            .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAcept, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chckEdit))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cmbRol, cmbShift, txtAddress, txtName, txtPassword, txtPhoneNumber, txtUser});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblAdress, lblDailySalary, lblName, lblPassword, lblPhoneNumber, lblRol, lblShift, lblUser});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 37, Short.MAX_VALUE)
                        .addComponent(lblIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnAcept)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)
                        .addGap(18, 18, 18)
                        .addComponent(btnClear)
                        .addGap(18, 54, Short.MAX_VALUE)
                        .addComponent(chckEdit))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEmpId)
                            .addComponent(txtEmpId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblName)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRol)
                            .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUser)
                            .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPassword)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPhoneNumber)
                            .addComponent(txtPhoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblAdress)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbShift, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblShift))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDailySalary)
                            .addComponent(txtDailySalary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(22, 22, 22))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cmbRol, cmbShift, txtAddress, txtName, txtPassword, txtPhoneNumber, txtUser});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAceptMouseClicked
        String name = txtName.getText().toUpperCase().trim();
        String user = txtUser.getText().toUpperCase().trim();
        String password = txtPassword.getText().trim();
        String phoneNumberText = txtPhoneNumber.getText().trim();
        String address = txtAddress.getText().trim();
        String dailySalaryText = txtDailySalary.getText();
        ComboBoxItem selectedRol = (ComboBoxItem) cmbRol.getSelectedItem();
        ComboBoxItem selectedShift = (ComboBoxItem) cmbShift.getSelectedItem();
        int idRol = selectedRol.getId();
        int idShift = selectedShift.getId();
        
        if(actionType.equals("Modify")&& password.equals("")){
            password = "ignore";
        }

        if (!name.equals("") && !user.equals("") && !password.equals("") && !phoneNumberText.equals("") && !address.equals("") && !dailySalaryText.equals("") && idRol != 1 && idShift != 1) {
            try{
                if (password.equals("ignore")) {
                    password = "";
                }
                double dailySalary = Double.parseDouble(dailySalaryText);
                long phoneNumber = Long.parseLong(phoneNumberText);
                loadData(name, idRol, user, password, phoneNumber, address, idShift, dailySalary);
                txtName.requestFocus();
                txtName.setEnabled(true);
                chckEdit.setSelected(false);
                chckEdit.setEnabled(false);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese valores validos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
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

    private void txtNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyPressed
       /* if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            
            String barCodeText = txtName.getText().trim();
            if (!"".equals(barCodeText)){
                findProduct(barCodeText);
            }
            
        }*/
    }//GEN-LAST:event_txtNameKeyPressed
    
    private void chckEditStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chckEditStateChanged
        if(chckEdit.isSelected()){
            disableFields(); 
            actionType = "Modify";
            txtEmpId.setEnabled(true);
        }
        setTitle(actionType);
        
    }//GEN-LAST:event_chckEditStateChanged

    private void btnClearMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnClearMouseClicked
        clearFields();
        txtName.setEnabled(true);
        disableFields();
        chckEdit.setSelected(false);
        txtName.setText("");
        txtName.requestFocus();        
    }//GEN-LAST:event_btnClearMouseClicked

    private void txtEmpIdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmpIdKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            String empId= txtEmpId.getText().trim();
            if (!"".equals(empId)){
                findEmployee(empId);
            }
            
        }
    }//GEN-LAST:event_txtEmpIdKeyPressed

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
            java.util.logging.Logger.getLogger(FrmUserMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmUserMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmUserMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmUserMod.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
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
            new FrmUserMod().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcept;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClear;
    private javax.swing.JCheckBox chckEdit;
    private javax.swing.JComboBox cmbRol;
    private javax.swing.JComboBox cmbShift;
    private javax.swing.JLabel lblAdress;
    private javax.swing.JLabel lblDailySalary;
    private javax.swing.JLabel lblEmpId;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPhoneNumber;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblShift;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUser;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtDailySalary;
    private javax.swing.JTextField txtEmpId;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPhoneNumber;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
