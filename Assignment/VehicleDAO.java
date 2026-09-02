import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VehicleDAO {

    public boolean insertVehicle(Vehicle v) {
        String sql = "INSERT INTO vehicles (vehicle_id, user_id, license_plate, model, battery_capacity_kwh) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, v.getVehicleId());
            ps.setInt(2, v.getUserId());
            ps.setString(3, v.getLicensePlate());
            ps.setString(4, v.getModel());
            ps.setDouble(5, v.getBatteryCapacity());

            int rows = ps.executeUpdate();
            ps.close();
            con.close();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateVehicle(Vehicle v) {
        String sql = "UPDATE vehicles SET user_id = ?, license_plate = ?, model = ?, battery_capacity_kwh = ? WHERE vehicle_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, v.getUserId());
            ps.setString(2, v.getLicensePlate());
            ps.setString(3, v.getModel());
            ps.setDouble(4, v.getBatteryCapacity());
            ps.setInt(5, v.getVehicleId());

            int rows = ps.executeUpdate();
            ps.close();
            con.close();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteVehicle(int vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, vehicleId);

            int rows = ps.executeUpdate();
            ps.close();
            con.close();
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Vehicle searchVehicle(int vehicleId) {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id = ?";
        Vehicle v = null;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                v = new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getInt("user_id"),
                        rs.getString("license_plate"),
                        rs.getString("model"),
                        rs.getDouble("battery_capacity_kwh")
                );
            }
            rs.close();
            ps.close();
            con.close();
            return v;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAllVehicles() {
        String sql = "SELECT * FROM vehicles";
        StringBuilder sb = new StringBuilder();
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            sb.append(String.format("%-12s %-10s %-18s %-18s %-15s\n", "Vehicle ID", "User ID", "License Plate", "Model", "Battery (kWh)"));
            sb.append("------------------------------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-12d %-10d %-18s %-18s %-15.2f\n",
                        rs.getInt("vehicle_id"),
                        rs.getInt("user_id"),
                        rs.getString("license_plate"),
                        rs.getString("model"),
                        rs.getDouble("battery_capacity_kwh")
                ));
            }
            rs.close();
            st.close();
            con.close();
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}