public class Vehicle {
    private int vehicleId;
    private int userId;
    private String licensePlate;
    private String model;
    private double batteryCapacity;

    public Vehicle(int vehicleId, int userId, String licensePlate, String model, double batteryCapacity) {
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.model = model;
        this.batteryCapacity = batteryCapacity;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }
}