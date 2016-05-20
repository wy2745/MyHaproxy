package graduationProject.Domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Pod")
public class Pod {
    @Id
    @NotNull
    @Column(name = "podName")
    private String podName;

    @Column(name = "cpuUsage")
    private double cpuUsage;

    @Column(name = "memUsage")
    private double memUsage;

    //ip:port
    @Column(name = "address")
    private String address;

    @Column(name = "serviceId")
    private int    serviceId;

    @Column(name = "connection")
    private int    connection;

    @Column(name = "cpuAbility")
    private double cpuAbility;

    @Column(name = "memAbility")
    private double memAbility;

    //    public Pod(String podName, double cpuUsage, double memUsage, String address, int serviceId) {
    //        this.podName = podName;
    //        this.cpuUsage = cpuUsage;
    //        this.memUsage = memUsage;
    //        this.address = address;
    //        this.serviceId = serviceId;
    //    }

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

    public int getConnection() {
        return connection;
    }

    public void setConnection(int connection) {
        this.connection = connection;
    }

    public double getCpuAbility() {
        return cpuAbility;
    }

    public void setCpuAbility(double cpuAbility) {
        this.cpuAbility = cpuAbility;
    }

    public double getMemAbility() {
        return memAbility;
    }

    public void setMemAbility(double memAbility) {
        this.memAbility = memAbility;
    }

}
