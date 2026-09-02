import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class EVChargingManagementGUI extends JFrame {

    // Tab 1: Live Points
    private JTable tblPoints;
    private DefaultTableModel modelPoints;

    // Tab 2: Vehicle CRUD Components
    private JTextField txtVehId, txtUserId, txtPlate, txtModel, txtBattery;
    private ButtonGroup bg;

    // Tab 3: Operations & Checkout Components
    private JTextField txtOpPointId, txtOpVehId, txtOpSessionId, txtOpEnergy;
    private JComboBox<String> cbPayMode;

    // Tab 4: Reports
    private JTextArea txtReportArea;

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private OperationsDAO operationsDAO = new OperationsDAO();

    public EVChargingManagementGUI() {
        setTitle("Smart Campus EV Charging Management System");
        setSize(900, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JLabel lblHeader = new JLabel("SMART CAMPUS E-VEHICLE MANAGEMENT SYSTEM", JLabel.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Station Status", createStationStatusPanel());
        tabbedPane.addTab("Vehicle CRUD", createVehicleCRUDPanel());
        tabbedPane.addTab("Operations & Checkout", createOperationsPanel());
        tabbedPane.addTab("Utilization Reports", createReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        refreshPointsTable();
        setVisible(true);
    }

    private JPanel createStationStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        modelPoints = new DefaultTableModel(new String[]{"Point ID", "Station", "Location", "Charger Type", "Tariff ($/kWh)", "Status"}, 0);
        tblPoints = new JTable(modelPoints);

        JButton btnRefresh = new JButton("Refresh Status");
        btnRefresh.addActionListener(e -> refreshPointsTable());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(btnRefresh);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblPoints), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVehicleCRUDPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Vehicle Information"));

        formPanel.add(new JLabel("Vehicle ID:"));
        txtVehId = new JTextField();
        formPanel.add(txtVehId);

        formPanel.add(new JLabel("User ID:"));
        txtUserId = new JTextField();
        formPanel.add(txtUserId);

        formPanel.add(new JLabel("License Plate:"));
        txtPlate = new JTextField();
        formPanel.add(txtPlate);

        formPanel.add(new JLabel("Vehicle Model:"));
        txtModel = new JTextField();
        formPanel.add(txtModel);

        formPanel.add(new JLabel("Battery Capacity (kWh):"));
        txtBattery = new JTextField();
        formPanel.add(txtBattery);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Add");
        JButton btnSearch = new JButton("Search");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnViewAll = new JButton("View All");

        btnPanel.add(btnAdd);
        btnPanel.add(btnSearch);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnViewAll);

        JTextArea txtOutput = new JTextArea(10, 50);
        txtOutput.setEditable(false);

        btnAdd.addActionListener(e -> {
            try {
                Vehicle v = new Vehicle(
                        Integer.parseInt(txtVehId.getText().trim()),
                        Integer.parseInt(txtUserId.getText().trim()),
                        txtPlate.getText().trim(),
                        txtModel.getText().trim(),
                        Double.parseDouble(txtBattery.getText().trim())
                );
                if (vehicleDAO.insertVehicle(v)) {
                    txtOutput.setText("Success: Vehicle added to database.");
                }
            } catch (Exception ex) {
                txtOutput.setText("Error: " + ex.getMessage());
            }
        });

        btnSearch.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtVehId.getText().trim());
                Vehicle v = vehicleDAO.searchVehicle(id);
                if (v != null) {
                    txtUserId.setText(String.valueOf(v.getUserId()));
                    txtPlate.setText(v.getLicensePlate());
                    txtModel.setText(v.getModel());
                    txtBattery.setText(String.valueOf(v.getBatteryCapacity()));
                    txtOutput.setText("Vehicle found: ID " + id);
                } else {
                    txtOutput.setText("No vehicle found with ID: " + id);
                }
            } catch (Exception ex) {
                txtOutput.setText("Search Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            try {
                Vehicle v = new Vehicle(
                        Integer.parseInt(txtVehId.getText().trim()),
                        Integer.parseInt(txtUserId.getText().trim()),
                        txtPlate.getText().trim(),
                        txtModel.getText().trim(),
                        Double.parseDouble(txtBattery.getText().trim())
                );
                if (vehicleDAO.updateVehicle(v)) {
                    txtOutput.setText("Success: Vehicle details updated.");
                }
            } catch (Exception ex) {
                txtOutput.setText("Update Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtVehId.getText().trim());
                if (vehicleDAO.deleteVehicle(id)) {
                    txtOutput.setText("Success: Vehicle record deleted.");
                }
            } catch (Exception ex) {
                txtOutput.setText("Delete Error: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            txtVehId.setText("");
            txtUserId.setText("");
            txtPlate.setText("");
            txtModel.setText("");
            txtBattery.setText("");
            txtOutput.setText("");
        });

        btnViewAll.addActionListener(e -> txtOutput.setText(vehicleDAO.getAllVehicles()));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(btnPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtOutput), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Sub-panel 1: Start Session
        JPanel startPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        startPanel.setBorder(BorderFactory.createTitledBorder("Check-In / Start Session"));

        startPanel.add(new JLabel("Charging Point ID:"));
        txtOpPointId = new JTextField();
        startPanel.add(txtOpPointId);

        startPanel.add(new JLabel("Vehicle ID:"));
        txtOpVehId = new JTextField();
        startPanel.add(txtOpVehId);

        JButton btnStart = new JButton("Check-In Vehicle");
        startPanel.add(new JLabel(""));
        startPanel.add(btnStart);

        // Sub-panel 2: Check-out
        JPanel endPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        endPanel.setBorder(BorderFactory.createTitledBorder("Check-Out & Settlement"));

        endPanel.add(new JLabel("Active Session ID:"));
        txtOpSessionId = new JTextField();
        endPanel.add(txtOpSessionId);

        endPanel.add(new JLabel("Energy Consumed (kWh):"));
        txtOpEnergy = new JTextField();
        endPanel.add(txtOpEnergy);

        endPanel.add(new JLabel("Payment Mode:"));
        cbPayMode = new JComboBox<>(new String[]{"CAMPUS_CARD", "UPI", "CREDIT_DEBIT"});
        endPanel.add(cbPayMode);

        JButton btnEnd = new JButton("Complete Checkout");
        endPanel.add(new JLabel(""));
        endPanel.add(btnEnd);

        btnStart.addActionListener(e -> {
            try {
                int pId = Integer.parseInt(txtOpPointId.getText().trim());
                int vId = Integer.parseInt(txtOpVehId.getText().trim());
                int sId = operationsDAO.startChargingSession(pId, vId);
                JOptionPane.showMessageDialog(this, "Session started successfully! Session ID: " + sId);
                refreshPointsTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Start error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEnd.addActionListener(e -> {
            try {
                int sId = Integer.parseInt(txtOpSessionId.getText().trim());
                double energy = Double.parseDouble(txtOpEnergy.getText().trim());
                String mode = (String) cbPayMode.getSelectedItem();

                double cost = operationsDAO.completeCheckout(sId, energy, mode);
                JOptionPane.showMessageDialog(this, String.format("Checkout Complete!\nTotal Billed: $%.2f", cost));
                refreshPointsTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Checkout error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(startPanel);
        panel.add(endPanel);
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        txtReportArea = new JTextArea();
        txtReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReportArea.setEditable(false);

        JButton btnGenerate = new JButton("Generate Campus Energy & Revenue Report");
        btnGenerate.addActionListener(e -> txtReportArea.setText(operationsDAO.getUtilizationReport()));

        panel.add(btnGenerate, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtReportArea), BorderLayout.CENTER);
        return panel;
    }

    private void refreshPointsTable() {
        modelPoints.setRowCount(0);
        List<ChargingPoint> list = operationsDAO.getAllPoints();
        for (ChargingPoint cp : list) {
            modelPoints.addRow(new Object[]{
                    cp.getPointId(),
                    cp.getStationName(),
                    cp.getLocation(),
                    cp.getChargerType(),
                    cp.getTariffPerKwh(),
                    cp.getStatus()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EVChargingManagementGUI());
    }
}