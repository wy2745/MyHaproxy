package graduationProject.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import graduationProject.redis.PodRedis;
import graduationProject.redis.RequestRedis;
import graduationProject.redis.ServiceRedis;

@Service
public class DispatchService extends BaseService {

    @Autowired
    private RedisTemplate<String, ServiceRedis> serviceRedisTemplate;

    @Autowired
    private RedisTemplate<String, List<String>> podInServiceRedisTemplate;

    @Autowired
    private RedisTemplate<String, PodRedis>     podRedisTemplate;

    @Autowired
    private RedisTemplate<String, RequestRedis> requestRedisTemplate;

    private String                              servicePrefix = "Service:";

    //    @Autowired
    //    private PodDAO                                         podDAO;
    //
    //    @Autowired
    //    private RequestDAO                                     requestDAO;
    //
    //    @Autowired
    //    private ServiceDAO                                     serviceDAO;
    //
    //    private Map<Integer, graduationProject.Domain.Service> serviceMap;
    //
    //    private Map<Integer, List<String>>                     PodInService;
    //
    //    private Map<String, Pod>                               podsMap;
    //
    //    private Map<String, Request>                           requestMap;

    public void generateForTest() {
        addService(1, "pupu", "a");
        addPod("pod1", 1, 1, "http://192.168.1.162", 1);
        addPod("pod2", 1, 1, "http://192.168.1.163", 1);
        addPod("pod3", 1, 1, "http://192.168.1.164", 1);
        addPod("pod4", 1, 1, "http://192.168.1.165", 1);
        addPod("pod5", 1, 1, "http://192.168.1.166", 1);
        addPod("pod6", 1, 1, "http://192.168.1.167", 1);
        addRequest("/users/images", 1, "GET", 1, 1, 1);
        addRequest("/users", 1, "POST", 1, 1, 1);

    }

    public void deleteForTest() {
        deletePod("pod1");
        deletePod("pod2");
        deletePod("pod3");
        deletePod("pod4");
        deletePod("pod5");
        deletePod("pod6");
        deleteRequest("/users/images");
        deleteRequest("/users");
        deleteService(1);
    }

    //    @Scheduled(initialDelay = 1000, fixedRate = 300000)
    //    public void init() {
    //        Map<Integer, graduationProject.Domain.Service> serviceMap2 = new Hashtable<Integer, graduationProject.Domain.Service>();
    //        Map<Integer, List<String>> PodInService2 = new Hashtable<>();
    //        Map<String, Pod> podsMap2 = new Hashtable<>();
    //        Map<String, Request> requestMap2 = new HashMap<>();
    //
    //        Iterable<graduationProject.Domain.Service> services = serviceDAO.findAll();
    //        for (graduationProject.Domain.Service service : services)
    //            serviceMap2.put(service.getServiceId(), service);
    //
    //        Iterable<Pod> pods = podDAO.findAll();
    //        for (Pod pod : pods) {
    //            podsMap2.put(pod.getPodName(), pod);
    //            if (PodInService2.containsKey(pod.getServiceId()))
    //                PodInService2.get(pod.getServiceId()).add(pod.getPodName());
    //            else {
    //                List<String> podName = new Vector<>();
    //                podName.add(pod.getPodName());
    //                PodInService2.put(pod.getServiceId(), podName);
    //            }
    //        }
    //
    //        Iterable<Request> requests = requestDAO.findAll();
    //        for (Request request : requests)
    //            requestMap2.put(request.getRequestPath(), request);
    //
    //        serviceMap = serviceMap2;
    //        PodInService = PodInService2;
    //        podsMap = podsMap2;
    //        requestMap = requestMap2;
    //        System.out.println("初始化成功");
    //    }

    private void forwardRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String url) {
        String method = httpServletRequest.getMethod();
        if (method.equals("GET")) {
            try {
                httpServletResponse.sendRedirect(url + "?" + httpServletRequest.getQueryString());
            } catch (IOException e) {
                logger.error("", e);
            }
        } else {
            httpServletResponse.setStatus(307);
            httpServletResponse.addHeader("Location", url);
        }
    }

    public void dispatchRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String mode) {
        String method = httpServletRequest.getMethod();
        String requestPath = getRequestPath(httpServletRequest);
        RequestRedis requestRedis = getRequestByPath(requestPath);
        if (!method.equals(requestRedis.getMethod())) {
            System.out.println("method错误");
            return;
        }
        //ServiceRedis serviceRedis = getServiceById(requestRedis.getServiceId());
        String desination = pick(requestRedis, requestRedis.getServiceId(), mode);
        forwardRequest(httpServletRequest, httpServletResponse,
            this.podRedisTemplate.opsForValue().get(desination).getAddress() + requestPath);
    }

    private int generateRanNum(int max) {
        Random random = new Random();
        int start = random.nextInt(max);
        return (random.nextInt(max) + start) % max;
    }

    private String pick(RequestRedis requestRedis, int serviceId, String mode) {
        //        //  mysql实现版本
        //        if (mode.equals("random")) {
        //            int max = PodInService.get(service.getServiceId()).size();
        //            int target = generateRanNum(max);
        //            return PodInService.get(service.getServiceId()).get(target);
        //        }
        //        return null;

        if (mode.equals("random")) {
            List<String> pod = getPodListByServiceId(serviceId);
            int max = pod.size();
            int target = generateRanNum(max);
            return pod.get(target);
        }
        return null;
    }

    public RequestRedis getRequestByPath(String requestPath) {
        //        //  mysql实现版本
        //        Request request = requestMap.get(requestPath);
        //        if (request == null)
        //            System.out.println("不存在对应的请求信息");
        //        return request;
        RequestRedis requestRedis = this.requestRedisTemplate.opsForValue().get(requestPath);
        if (requestRedis == null)
            System.out.println("不存在对应的请求信息");
        return requestRedis;
    }

    public ServiceRedis getServiceById(int serviceId) {
        //        //  mysql实现版本
        //        graduationProject.Domain.Service service = serviceMap.get(serviceId);
        //        if (service == null)
        //            System.out.println("不存在对应的service");
        //        return service;
        ServiceRedis serviceRedis = this.serviceRedisTemplate.opsForValue()
            .get(String.valueOf(serviceId));
        if (serviceRedis == null)
            System.out.println("不存在对应的service");
        return serviceRedis;
    }

    public PodRedis getPodByName(String podName) {
        //        //  mysql实现版本
        //        Pod pod = this.podsMap.get(podName);
        //        if (pod == null)
        //            System.out.println("pod不存在");
        //        return pod;
        PodRedis podRedis = this.podRedisTemplate.opsForValue().get(podName);
        if (podRedis == null)
            System.out.println("pod不存在");
        return podRedis;
    }

    public String getRequestPath(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getMethod().equals("GET")) {
            return httpServletRequest.getParameter("RequestPath");
        } else {
            try {
                BufferedReader reader = httpServletRequest.getReader();
                String str = reader.readLine();
                while (str != null) {
                    if (str.contains("RequestPath")) {
                        return str.split("\"")[3];
                    }
                    str = reader.readLine();

                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public List<String> getPodListByServiceId(int serviceId) {
        ValueOperations<String, List<String>> podInServiceOperations = podInServiceRedisTemplate
            .opsForValue();
        return podInServiceOperations.get(servicePrefix + String.valueOf(serviceId));
    }

    public void addService(int serviceId, String serviceName, String serviceType) {
        ServiceRedis serviceRedis = getServiceById(serviceId);
        if (serviceRedis != null) {
            System.out.println("对应的service已存在");
            return;
        }
        serviceRedis = new ServiceRedis(serviceId, serviceName, serviceType);
        ValueOperations<String, ServiceRedis> serviceRedisoperations = this.serviceRedisTemplate
            .opsForValue();
        serviceRedisoperations.set(String.valueOf(serviceId), serviceRedis);
    }

    public void deleteService(int serviceId) {
        ServiceRedis serviceRedis = getServiceById(serviceId);
        if (serviceRedis == null) {
            return;
        }
        this.serviceRedisTemplate.delete(String.valueOf(serviceId));
        this.podInServiceRedisTemplate.delete(servicePrefix + String.valueOf(serviceId));
    }

    public void addPod(String podName, double cpuUsage, double memUsage, String address,
                       int serviceId) {
        //        //  mysql实现版本
        //        if (this.podsMap.containsKey(pod.getPodName()))
        //            System.out.println("已存在pod");
        //        else
        //            this.podsMap.put(pod.getPodName(), pod);
        if (getServiceById(serviceId) == null)
            return;

        PodRedis podRedis = new PodRedis(podName, cpuUsage, memUsage, address, serviceId);
        ValueOperations<String, PodRedis> PodOperations = this.podRedisTemplate.opsForValue();
        if (PodOperations.get(podRedis.getPodName()) != null)
            System.out.println("已存在pod");
        else {
            PodOperations.set(podRedis.getPodName(), podRedis);
            addPodToService(podRedis.getPodName(), serviceId);
        }
    }

    public void deletePod(String podName) {
        //        //  mysql实现版本
        //        if (this.podsMap.containsKey(pod.getPodName()))
        //            this.podsMap.remove(pod.getPodName());
        PodRedis podRedis = getPodByName(podName);
        if (podRedis == null)
            return;
        else {
            this.podRedisTemplate.delete(podName);
            List<String> pod = getPodListByServiceId(podRedis.getServiceId());
            if (pod != null)
                getPodListByServiceId(podRedis.getServiceId()).remove(pod.indexOf(podName));
        }
    }

    public void addPodToService(String podName, int serviceId) {
        ValueOperations<String, List<String>> podInServiceOperations = podInServiceRedisTemplate
            .opsForValue();
        if (getServiceById(serviceId) == null)
            return;

        if (getPodByName(podName) == null)
            return;
        List<String> pod = podInServiceOperations.get(servicePrefix + String.valueOf(serviceId));
        if (pod == null) {
            pod = new Vector<>();
            pod.add(podName);
            podInServiceOperations.set(servicePrefix + String.valueOf(serviceId), pod);
        } else if (pod.contains(podName))
            System.out.println("该pod已在该service内");
        else
            podInServiceOperations.get(servicePrefix + String.valueOf(serviceId)).add(podName);
    }

    public void addRequest(String requestPath, int serviceId, String method, double cpuCost,
                           double memCost, double timeCost) {
        RequestRedis requestRedis = getRequestByPath(requestPath);
        ServiceRedis serviceRedis = getServiceById(serviceId);
        if (serviceRedis == null)
            return;
        ValueOperations<String, RequestRedis> requestRedisoperations = this.requestRedisTemplate
            .opsForValue();
        if (requestRedis == null) {
            requestRedis = new RequestRedis(requestPath, serviceId, method, cpuCost, memCost,
                timeCost);
            requestRedisoperations.set(requestPath, requestRedis);
        }
        return;
    }

    public void deleteRequest(String requestPath) {
        RequestRedis requestRedis = getRequestByPath(requestPath);
        if (requestRedis == null)
            return;
        this.requestRedisTemplate.delete(requestPath);
    }

    //    public void flushPod() {
    //        this.podsMap.clear();
    //        this.PodInService.clear();
    //        Iterable<Pod> podset = podDAO.findAll();
    //        for (Pod pod : podset) {
    //            this.podsMap.put(pod.getPodName(), pod);
    //            if (this.PodInService.containsKey(pod.getServiceId()))
    //                this.PodInService.get(pod.getServiceId()).add(pod.getPodName());
    //            else {
    //                List<String> podName = new Vector<>();
    //                podName.add(pod.getPodName());
    //                this.PodInService.put(pod.getServiceId(), podName);
    //            }
    //        }
    //        System.out.println("成功刷新所有pod的状态");
    //    }
    //
    //    public void flushService() {
    //        this.serviceMap.clear();
    //        Iterable<graduationProject.Domain.Service> services = serviceDAO.findAll();
    //        for (graduationProject.Domain.Service service : services) {
    //            this.serviceMap.put(service.getServiceId(), service);
    //        }
    //        System.out.println("成功刷新所有service的状态");
    //    }
    //
    //    public void flushRequestLog() {
    //        this.requestMap.clear();
    //        Iterable<Request> requests = requestDAO.findAll();
    //        for (Request request : requests) {
    //            this.requestMap.put(request.getRequestPath(), request);
    //        }
    //        System.out.println("成功刷新所有request的状态");
    //    }

    public void memory() throws SigarException {
        Sigar sigar = new Sigar();
        Mem mem = sigar.getMem();
        // 内存总量
        System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
        // 当前内存使用量
        System.out.println("当前内存使用量:    " + mem.getUsed() / 1024L + "K used");
        // 当前内存剩余量
        System.out.println("当前内存剩余量:    " + mem.getFree() / 1024L + "K free");
        Swap swap = sigar.getSwap();
        // 交换区总量
        System.out.println("交换区总量:    " + swap.getTotal() / 1024L + "K av");
        // 当前交换区使用量
        System.out.println("当前交换区使用量:    " + swap.getUsed() / 1024L + "K used");
        // 当前交换区剩余量
        System.out.println("当前交换区剩余量:    " + swap.getFree() / 1024L + "K free");
    }

    public void cpu() throws SigarException {
        Sigar sigar = new Sigar();
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpuList[] = null;
        cpuList = sigar.getCpuPercList();
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
            System.out.println("第" + (i + 1) + "块CPU信息");
            System.out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
            System.out.println("CPU生产商:    " + info.getVendor());// 获得CPU的卖主，如：Intel
            System.out.println("CPU类别:    " + info.getModel());// 获得CPU的类别，如：Celeron
            System.out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量
            printCpuPerc(cpuList[i]);
        }
    }

    private static void printCpuPerc(CpuPerc cpu) {
        System.out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        System.out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        System.out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        System.out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));//
        System.out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        System.out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率
    }

    public void testRedis() {
        ValueOperations<String, PodRedis> podValueOperations = this.podRedisTemplate.opsForValue();
        System.out.println("test for redix across machine");
        System.out.println("PodName: " + podValueOperations.get("pod1").getPodName() + ",CPU: "
                           + podValueOperations.get("pod1").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod1").getMemUsage());
        System.out.println("PodName: " + podValueOperations.get("pod2").getPodName() + ",CPU: "
                           + podValueOperations.get("pod2").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod2").getMemUsage());
        System.out.println("PodName: " + podValueOperations.get("pod3").getPodName() + ",CPU: "
                           + podValueOperations.get("pod3").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod3").getMemUsage());
        System.out.println("PodName: " + podValueOperations.get("pod4").getPodName() + ",CPU: "
                           + podValueOperations.get("pod4").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod4").getMemUsage());
        System.out.println("PodName: " + podValueOperations.get("pod5").getPodName() + ",CPU: "
                           + podValueOperations.get("pod5").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod5").getMemUsage());
        System.out.println("PodName: " + podValueOperations.get("pod6").getPodName() + ",CPU: "
                           + podValueOperations.get("pod6").getCpuUsage() + ",Mem: "
                           + podValueOperations.get("pod6").getMemUsage());
    }
}
