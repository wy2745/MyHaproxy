//package graduationProject.redis;
//
//import java.io.Serializable;
//
//public class RequestRedis implements Serializable {
//
//    private static final long serialVersionUID = -1L;
//
//    private String            requestPath;
//
//    private int               serviceId;
//
//    private String            method;
//
//    private double            cpuCost;
//
//    private double            memCost;
//
//    private double            timeCost;
//
//    public RequestRedis(String requestPath, int serviceId, String method, double cpuCost,
//                        double memCost, double timeCost) {
//        this.requestPath = requestPath;
//        this.serviceId = serviceId;
//        this.method = method;
//        this.cpuCost = cpuCost;
//        this.memCost = memCost;
//        this.timeCost = timeCost;
//    }
//
//    public String getRequestPath() {
//        return requestPath;
//    }
//
//    public void setRequestPath(String requestPath) {
//        this.requestPath = requestPath;
//    }
//
//    public int getServiceId() {
//        return serviceId;
//    }
//
//    public void setServiceId(int serviceId) {
//        this.serviceId = serviceId;
//    }
//
//    public String getMethod() {
//        return method;
//    }
//
//    public void setMethod(String method) {
//        this.method = method;
//    }
//
//    public double getCpuCost() {
//        return cpuCost;
//    }
//
//    public void setCpuCost(double cpuCost) {
//        this.cpuCost = cpuCost;
//    }
//
//    public double getMemCost() {
//        return memCost;
//    }
//
//    public void setMemCost(double memCost) {
//        this.memCost = memCost;
//    }
//
//    public double getTimeCost() {
//        return timeCost;
//    }
//
//    public void setTimeCost(double timeCost) {
//        this.timeCost = timeCost;
//    }
//
//}
