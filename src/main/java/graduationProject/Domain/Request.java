package graduationProject.Domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Request")
public class Request {
    @Id
    @NotNull
    @Column(name = "requestId")
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private int    requestId;

    @Column(name = "requestPath")
    private String requestPath;

    @Column(name = "serviceId")
    private int    serviceId;

    @Column(name = "method")
    private String method;

    @Column(name = "cpuCost")
    private double cpuCost;

    @Column(name = "memCost")
    private double memCost;

    @Column(name = "timeCost")
    private double timeCost;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getCpuCost() {
        return cpuCost;
    }

    public void setCpuCost(double cpuCost) {
        this.cpuCost = cpuCost;
    }

    public double getMemCost() {
        return memCost;
    }

    public void setMemCost(double memCost) {
        this.memCost = memCost;
    }

    public double getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(double timeCost) {
        this.timeCost = timeCost;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

}
