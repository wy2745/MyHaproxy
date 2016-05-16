package graduationProject.Domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Service")
public class Service {
    @Id
    @NotNull
    @Column(name = "serviceId")
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private int    serviceId;

    @Column(name = "serviceName")
    private String serviceName;

    @Column(name = "serviceType")
    private String serviceType;

    //    private Map<String, Pod> pods;

    //    public Service(int serviceId, String serviceName, String serviceType) {
    //        this.serviceId = serviceId;
    //        this.serviceName = serviceName;
    //        this.serviceType = serviceType;
    //    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    //    public void setPods(Map<String, Pod> pods) {
    //        this.pods = pods;
    //    }
    //
    //    public void addPod(Pod pod) {
    //        if (this.pods.containsKey(pod.getPodName()))
    //            System.out.println("已存在pod");
    //        else
    //            this.pods.put(pod.getPodName(), pod);
    //    }
    //
    //    public void deletePod(Pod pod) {
    //        if (this.pods.containsKey(pod.getPodName()))
    //            this.pods.remove(pod.getPodName());
    //    }
    //
    //    public void flushPod(List<Pod> podset) {
    //        this.pods.clear();
    //        for (Pod pod : podset) {
    //            this.pods.put(pod.getPodName(), pod);
    //        }
    //        System.out.println("成功刷新service所属pod的状态");
    //    }
    //
    //    public Map<String, Pod> getPods() {
    //        return this.pods;
    //    }
}
