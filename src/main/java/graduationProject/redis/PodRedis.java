package graduationProject.redis;

import java.io.Serializable;

public class PodRedis implements Serializable {

    private static final long serialVersionUID = -1L;

    private String            podName;

    private double            cpuUsage;

    private double            memUsage;

    //ip:port
    private String            address;

    private int               serviceId;

    public PodRedis(String podName, double cpuUsage, double memUsage, String address,
                    int serviceId) {
        this.podName = podName;
        this.cpuUsage = cpuUsage;
        this.memUsage = memUsage;
        this.address = address;
        this.serviceId = serviceId;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

}
