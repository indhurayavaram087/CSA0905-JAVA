public class ChargingPoint {
    private int pointId;
    private String stationName;
    private String location;
    private String chargerType;
    private double tariffPerKwh;
    private String status;

    public ChargingPoint(int pointId, String stationName, String location, String chargerType, double tariffPerKwh, String status) {
        this.pointId = pointId;
        this.stationName = stationName;
        this.location = location;
        this.chargerType = chargerType;
        this.tariffPerKwh = tariffPerKwh;
        this.status = status;
    }

    public int getPointId() { return pointId; }
    public String getStationName() { return stationName; }
    public String getLocation() { return location; }
    public String getChargerType() { return chargerType; }
    public double getTariffPerKwh() { return tariffPerKwh; }
    public String getStatus() { return status; }
}