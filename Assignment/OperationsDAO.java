import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OperationsDAO {

    public List<ChargingPoint> getAllPoints() {
        List<ChargingPoint> list = new ArrayList<>();
        String sql = "SELECT cp.point_id, cs.station_name, cs.campus_location, cp.charger_type, cp.tariff_per_kwh, cp.status " +
                "FROM charging_points cp JOIN charging_stations cs ON cp.station_id = cs.station_id";

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                list.add(new ChargingPoint(
                        rs.getInt("point_id"),
                        rs.getString("station_name"),
                        rs.getString("campus_location"),
                        rs.getString("charger_type"),
                        rs.getDouble("tariff_per_kwh"),
                        rs.getString("status")
                ));
            }
            rs.close();
            st.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public boolean reserveSlot(int userId, int pointId, Timestamp start, Timestamp end) {
        String checkSql = "SELECT COUNT(*) FROM reservations WHERE point_id = ? AND status = 'CONFIRMED' " +
                "AND ((start_time <= ? AND end_time >= ?) OR (start_time <= ? AND end_time >= ?))";
        String insertSql = "INSERT INTO reservations (user_id, point_id, start_time, end_time, status) VALUES (?, ?, ?, ?, 'CONFIRMED')";
        String updatePoint = "UPDATE charging_points SET status = 'RESERVED' WHERE point_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement chkPs = con.prepareStatement(checkSql);
            chkPs.setInt(1, pointId);
            chkPs.setTimestamp(2, start);
            chkPs.setTimestamp(3, start);
            chkPs.setTimestamp(4, end);
            chkPs.setTimestamp(5, end);
            ResultSet rs = chkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                rs.close();
                chkPs.close();
                con.close();
                return false;
            }
            rs.close();
            chkPs.close();

            PreparedStatement insPs = con.prepareStatement(insertSql);
            insPs.setInt(1, userId);
            insPs.setInt(2, pointId);
            insPs.setTimestamp(3, start);
            insPs.setTimestamp(4, end);
            insPs.executeUpdate();
            insPs.close();

            PreparedStatement upPs = con.prepareStatement(updatePoint);
            upPs.setInt(1, pointId);
            upPs.executeUpdate();
            upPs.close();

            con.close();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int startChargingSession(int pointId, int vehicleId) {
        String sessionSql = "INSERT INTO charging_sessions (point_id, vehicle_id, start_time, session_status) VALUES (?, ?, NOW(), 'ACTIVE')";
        String updatePoint = "UPDATE charging_points SET status = 'OCCUPIED' WHERE point_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, pointId);
            ps.setInt(2, vehicleId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int sessionId = -1;
            if (rs.next()) {
                sessionId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            PreparedStatement upPs = con.prepareStatement(updatePoint);
            upPs.setInt(1, pointId);
            upPs.executeUpdate();
            upPs.close();

            con.close();
            return sessionId;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public double completeCheckout(int sessionId, double energyConsumed, String paymentMode) {
        String callSql = "{CALL sp_CompleteChargingSession(?, ?, ?)}";
        String paySql = "INSERT INTO payments (session_id, amount_paid, payment_mode, payment_status) VALUES (?, ?, ?, 'SUCCESS')";

        try {
            Connection con = DBConnection.getConnection();
            CallableStatement cs = con.prepareCall(callSql);
            cs.setInt(1, sessionId);
            cs.setDouble(2, energyConsumed);
            cs.registerOutParameter(3, Types.DECIMAL);
            cs.execute();

            double totalCost = cs.getDouble(3);
            cs.close();

            PreparedStatement payPs = con.prepareStatement(paySql);
            payPs.setInt(1, sessionId);
            payPs.setDouble(2, totalCost);
            payPs.setString(3, paymentMode);
            payPs.executeUpdate();
            payPs.close();

            con.close();
            return totalCost;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUtilizationReport() {
        String sql = "SELECT cs.station_name, COUNT(cses.session_id) as total_sessions, " +
                "IFNULL(SUM(cses.energy_consumed_kwh), 0) as total_energy, " +
                "IFNULL(SUM(cses.total_cost), 0) as total_revenue " +
                "FROM charging_stations cs " +
                "LEFT JOIN charging_points cp ON cs.station_id = cp.station_id " +
                "LEFT JOIN charging_sessions cses ON cp.point_id = cses.point_id " +
                "GROUP BY cs.station_id, cs.station_name";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-25s %-15s %-20s %-15s\n", "Station", "Sessions", "Energy (kWh)", "Revenue ($)"));
        sb.append("------------------------------------------------------------------------------------\n");

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                sb.append(String.format("%-25s %-15d %-20.2f $%-14.2f\n",
                        rs.getString("station_name"),
                        rs.getInt("total_sessions"),
                        rs.getDouble("total_energy"),
                        rs.getDouble("total_revenue")
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