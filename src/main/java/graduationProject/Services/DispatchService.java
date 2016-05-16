package graduationProject.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
import org.springframework.scheduling.annotation.Scheduled;

import graduationProject.Dao.PodDAO;
import graduationProject.Dao.RequestDAO;
import graduationProject.Dao.ServiceDAO;
import graduationProject.Domain.Pod;
import graduationProject.Domain.Request;
import graduationProject.Domain.Service;;

@org.springframework.stereotype.Service
public class DispatchService extends BaseService {

    //    @Autowired
    //    private RedisTemplate<String, ServiceRedis> serviceRedisTemplate;
    //
    //    @Autowired
    //    private RedisTemplate<String, List<String>> podInServiceRedisTemplate;
    //
    //    @Autowired
    //    private RedisTemplate<String, PodRedis>     podRedisTemplate;
    //
    //    @Autowired
    //    private RedisTemplate<String, RequestRedis> requestRedisTemplate;

    //private String                                         servicePrefix = "Service:";

    @Autowired
    private PodDAO                     podDAO;

    @Autowired
    private RequestDAO                 requestDAO;

    @Autowired
    private ServiceDAO                 serviceDAO;

    private Map<Integer, Service>      serviceMap;

    private Map<Integer, List<String>> PodInService;

    private Map<String, Pod>           podsMap;

    private Map<String, Request>       requestMap;

    private double                     choiceRate = 1 / 3;

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

    //每过30秒读取一遍数据库进行刷新，实际测试的时候，可以只对pod的状态进行刷新（因为service，pod，request相对不变）
    @Scheduled(initialDelay = 1000, fixedRate = 30000)
    public void init() {
        Map<Integer, Service> serviceMap2 = new Hashtable<Integer, Service>();
        Map<Integer, List<String>> PodInService2 = new Hashtable<>();
        Map<String, Pod> podsMap2 = new Hashtable<>();
        Map<String, Request> requestMap2 = new HashMap<>();

        Iterable<Service> services = serviceDAO.findAll();
        for (Service service : services)
            serviceMap2.put(service.getServiceId(), service);

        Iterable<Pod> pods = podDAO.findAll();
        for (Pod pod : pods) {
            podsMap2.put(pod.getPodName(), pod);
            if (PodInService2.containsKey(pod.getServiceId()))
                PodInService2.get(pod.getServiceId()).add(pod.getPodName());
            else {
                List<String> podName = new Vector<>();
                podName.add(pod.getPodName());
                PodInService2.put(pod.getServiceId(), podName);
            }
        }

        Iterable<Request> requests = requestDAO.findAll();
        for (Request request : requests)
            requestMap2.put(request.getRequestPath(), request);

        serviceMap = serviceMap2;
        PodInService = PodInService2;
        podsMap = podsMap2;
        requestMap = requestMap2;
        System.out.println("初始化成功");
    }

    //将当前的请求根据类型（GET或POST）进行转发
    private void forwardRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String url) {
        System.out.println("choice  the url: " + url);
        String method = httpServletRequest.getMethod();
        if (method.equals("GET")) {
            try {
                httpServletResponse.sendRedirect(url + "?" + httpServletRequest.getQueryString());
            } catch (IOException e) {
                //logger.error("", e);
            }
        } else {
            httpServletResponse.setStatus(307);
            httpServletResponse.addHeader("Location", url);
        }
    }

    //根据mode的要求选择对应的算法，取出对应的podName，利用算法进行选择，拿到合适的pod的地址
    public void dispatchRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String mode) {

        //mysql实现方案
        String method = httpServletRequest.getMethod();
        String requestPath = getRequestPath(httpServletRequest);
        Request request = getRequestByPath(requestPath);
        if (!method.equals(request.getMethod())) {
            System.out.println("method错误");
            return;
        }
        String desination = pick(request, mode);
        System.out.println("dispatch request to pod: " + desination);
        forwardRequest(httpServletRequest, httpServletResponse,
            getPodByName(desination).getAddress() + requestPath);

        //        //redis实现方案
        //        String method = httpServletRequest.getMethod();
        //        String requestPath = getRequestPath(httpServletRequest);
        //        RequestRedis requestRedis = getRequestByPath(requestPath);
        //        if (!method.equals(requestRedis.getMethod())) {
        //            System.out.println("method错误");
        //            return;
        //        }
        //        //ServiceRedis serviceRedis = getServiceById(requestRedis.getServiceId());
        //        String desination = pick(requestRedis, requestRedis.getServiceId(), mode);
        //        forwardRequest(httpServletRequest, httpServletResponse,
        //            this.podRedisTemplate.opsForValue().get(desination).getAddress() + requestPath);
    }

    //生成一个[0,max)的随机数
    private int generateRanNum(int max) {
        Random random = new Random();
        int start = random.nextInt(max);
        return (random.nextInt(max) + start) % max;
    }

    private List<Integer> generateRanNumList(int max, int choiceRate) {
        List<Integer> numLst = new Vector<>();
        Random random = new Random();
        int start = random.nextInt(max);
        int num = max * choiceRate;
        while (numLst.size() < num) {
            int target = (random.nextInt(max) + start) % max;
            if (numLst.contains(target))
                continue;
            numLst.add(target);
        }
        return numLst;
    }

    private String randomPick(int serviceId) {
        List<String> pods = PodInService.get(serviceId);
        int size = pods.size();
        int target = generateRanNum(size);
        return pods.get(target);
    }

    private String getBetterPod1(Request request, List<Integer> targetList, int serviceId) {
        List<String> pods = getPodListByServiceId(serviceId);
        double highestScore = -100000;
        String podName = "pod1";
        for (int index : targetList) {
            Pod pod = getPodByName(pods.get(index));
            double memScore = ((1 - pod.getMemUsage()) - request.getMemCost()) * 25;
            double cpuScore = ((1 - pod.getCpuUsage()) - request.getCpuCost()) * 25;
            double score = memScore + cpuScore;
            if (highestScore < score) {
                highestScore = score;
                podName = pod.getPodName();
            }
        }
        return podName;
    }

    private String choicePick(Request request, int size, int serviceId, String mode) {
        List<Integer> targetList = generateRanNumList(size, (int) choiceRate);
        String target = "pod1";
        if (mode.equals("choice1"))
            target = getBetterPod1(request, targetList, serviceId);
        //        else if (mode.equals("choice2"))
        //            target = getBetterPod2(request, targetList, serviceId);
        return target;
    }

    //根据请求的属性，和mode，选择出相应合适的pod
    private String pick(Request request, String mode) {
        //  mysql实现版本
        if (mode.equals("random"))
            return randomPick(request.getServiceId());
        // // randomPick 初始代码
        // int max = PodInService.get(serviceId).size();
        // int target = generateRanNum(max);
        // //避免这个时候pod列表被刷新了
        // return PodInService.get(serviceId).get(target);

        return null;

        //        //redis实现版本
        //        if (mode.equals("random")) {
        //            List<String> pod = getPodListByServiceId(serviceId);
        //            int max = pod.size();
        //            int target = generateRanNum(max);
        //            return pod.get(target);
        //        }
        //        return null;
    }

    public Request getRequestByPath(String requestPath) {
        //  mysql实现版本
        return this.requestMap.get(requestPath);

        //        //redis实现版本
        //        RequestRedis requestRedis = this.requestRedisTemplate.opsForValue().get(requestPath);
        //        if (requestRedis == null)
        //            System.out.println("不存在对应的请求信息");
        //        return requestRedis;
    }

    public Service getServiceById(int serviceId) {
        //  mysql实现版本
        return this.serviceMap.get(serviceId);

        //        //redis实现版本
        //        ServiceRedis serviceRedis = this.serviceRedisTemplate.opsForValue()
        //            .get(String.valueOf(serviceId));
        //        if (serviceRedis == null)
        //            System.out.println("不存在对应的service");
        //        return serviceRedis;
    }

    public Pod getPodByName(String podName) {
        //  mysql实现版本
        return this.podsMap.get(podName);

        //        //redis实现版本
        //        PodRedis podRedis = this.podRedisTemplate.opsForValue().get(podName);
        //        if (podRedis == null)
        //            System.out.println("pod不存在");
        //        return podRedis;
    }

    //获取请求的path(由于现在的设计是：请求调用redirect接口，由该接口统一进行转发，因此，需要将请求path写到传入api的参数中)
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
                //logger.error("", e);
            }
        }
        return null;
    }

    public List<String> getPodListByServiceId(int serviceId) {
        return this.PodInService.get(serviceId);

        //        //redis实现版本
        //        ValueOperations<String, List<String>> podInServiceOperations = podInServiceRedisTemplate
        //            .opsForValue();
        //        return podInServiceOperations.get(servicePrefix + String.valueOf(serviceId));
    }

    public void addService(int serviceId, String serviceName, String serviceType) {

        //mysql实现版本
        //Service service = new Service(serviceId, serviceName, serviceType);
        Service service = new Service();
        service.setServiceId(serviceId);
        service.setServiceName(serviceName);
        service.setServiceType(serviceType);
        if (getServiceById(serviceId) != null) {
            System.out.println("service已存在");
            return;
        }
        this.serviceDAO.save(service);
        flushService();

        //        //redis实现版本
        //        ServiceRedis serviceRedis = getServiceById(serviceId);
        //        if (serviceRedis != null) {
        //            System.out.println("对应的service已存在");
        //            return;
        //        }
        //        serviceRedis = new ServiceRedis(serviceId, serviceName, serviceType);
        //        ValueOperations<String, ServiceRedis> serviceRedisoperations = this.serviceRedisTemplate
        //            .opsForValue();
        //        serviceRedisoperations.set(String.valueOf(serviceId), serviceRedis);
    }

    public void deleteService(int serviceId) {

        Service service = serviceDAO.findByServiceId(serviceId);
        if (service == null)
            return;
        serviceDAO.delete(service);
        List<Pod> pods = podDAO.findByServiceId(serviceId);
        for (Pod pod : pods)
            podDAO.delete(pod);

        flushService();
        flushPod();
        //        //redis实现版本
        //        ServiceRedis serviceRedis = getServiceById(serviceId);
        //        if (serviceRedis == null) {
        //            return;
        //        }
        //        this.serviceRedisTemplate.delete(String.valueOf(serviceId));
        //        this.podInServiceRedisTemplate.delete(servicePrefix + String.valueOf(serviceId));
    }

    public void addPod(String podName, double cpuUsage, double memUsage, String address,
                       int serviceId) {
        //  mysql实现版本
        Pod pod = new Pod(podName, cpuUsage, memUsage, address, serviceId);
        if (getPodByName(podName) != null) {
            System.out.println("已存在pod");
            return;
        }
        if (getServiceById(serviceId) != null) {
            System.out.println("不存在对应的service");
            return;
        }
        podDAO.save(pod);
        flushPod();

        //        //redis实现版本
        //        if (getServiceById(serviceId) == null)
        //            return;
        //
        //        PodRedis podRedis = new PodRedis(podName, cpuUsage, memUsage, address, serviceId);
        //        ValueOperations<String, PodRedis> PodOperations = this.podRedisTemplate.opsForValue();
        //        if (PodOperations.get(podRedis.getPodName()) != null)
        //            System.out.println("已存在pod");
        //        else {
        //            PodOperations.set(podRedis.getPodName(), podRedis);
        //            addPodToService(podRedis.getPodName(), serviceId);
        //        }
    }

    public void deletePod(String podName) {
        //  mysql实现版本
        Pod pod = podDAO.findByPodName(podName);
        if (pod == null) {
            System.out.println("不存在对应的pod");
            return;
        }
        podDAO.delete(pod);
        flushPod();
        //        //redis实现版本
        //        PodRedis podRedis = getPodByName(podName);
        //        if (podRedis == null)
        //            return;
        //        else {
        //            this.podRedisTemplate.delete(podName);
        //            List<String> pod = getPodListByServiceId(podRedis.getServiceId());
        //            if (pod != null)
        //                getPodListByServiceId(podRedis.getServiceId()).remove(pod.indexOf(podName));
        //        }
    }

    //    public void addPodToService(String podName, int serviceId) {
    //
    //        //        //redis实现版本
    //        //        ValueOperations<String, List<String>> podInServiceOperations = podInServiceRedisTemplate
    //        //            .opsForValue();
    //        //        if (getServiceById(serviceId) == null)
    //        //            return;
    //        //
    //        //        if (getPodByName(podName) == null)
    //        //            return;
    //        //        List<String> pod = podInServiceOperations.get(servicePrefix + String.valueOf(serviceId));
    //        //        if (pod == null) {
    //        //            pod = new Vector<>();
    //        //            pod.add(podName);
    //        //            podInServiceOperations.set(servicePrefix + String.valueOf(serviceId), pod);
    //        //        } else if (pod.contains(podName))
    //        //            System.out.println("该pod已在该service内");
    //        //        else
    //        //            podInServiceOperations.get(servicePrefix + String.valueOf(serviceId)).add(podName);
    //    }

    public void addRequest(String requestPath, int serviceId, String method, double cpuCost,
                           double memCost, double timeCost) {

        Request request = requestDAO.findByRequestPath(requestPath);
        if (request != null || (serviceDAO.findByServiceId(serviceId) == null)) {
            System.out.println("已存在对应的request或对应的service不存在");
            return;
        }
        request = new Request(requestPath, serviceId, method, cpuCost, memCost, timeCost);
        requestDAO.save(request);
        flushRequestLog();
        //        //redis实现版本
        //        RequestRedis requestRedis = getRequestByPath(requestPath);
        //        ServiceRedis serviceRedis = getServiceById(serviceId);
        //        if (serviceRedis == null)
        //            return;
        //        ValueOperations<String, RequestRedis> requestRedisoperations = this.requestRedisTemplate
        //            .opsForValue();
        //        if (requestRedis == null) {
        //            requestRedis = new RequestRedis(requestPath, serviceId, method, cpuCost, memCost,
        //                timeCost);
        //            requestRedisoperations.set(requestPath, requestRedis);
        //        }
        //        return;
    }

    public void deleteRequest(String requestPath) {

        Request request = requestDAO.findByRequestPath(requestPath);
        if (request == null) {
            System.out.println("不存在对应的request");
            return;
        }
        requestDAO.delete(request);
        flushRequestLog();
        //        //redis实现版本
        //        RequestRedis requestRedis = getRequestByPath(requestPath);
        //        if (requestRedis == null)
        //            return;
        //        this.requestRedisTemplate.delete(requestPath);
    }

    public void flushPod() {
        //        this.podsMap.clear();
        //        this.PodInService.clear();
        Map<String, Pod> podsMaps2 = new Hashtable<>();
        Map<Integer, List<String>> PodInService2 = new Hashtable<>();
        Iterable<Pod> podset = podDAO.findAll();
        for (Pod pod : podset) {
            podsMaps2.put(pod.getPodName(), pod);
            if (PodInService2.containsKey(pod.getServiceId()))
                PodInService2.get(pod.getServiceId()).add(pod.getPodName());
            else {
                List<String> podName = new Vector<>();
                podName.add(pod.getPodName());
                PodInService2.put(pod.getServiceId(), podName);
            }
        }
        this.podsMap = podsMaps2;
        this.PodInService = PodInService2;
        System.out.println("成功刷新所有pod的状态");
    }

    public void flushService() {
        Map<Integer, Service> ServiceMap2 = new Hashtable<>();
        Iterable<Service> services = serviceDAO.findAll();
        for (Service service : services) {
            ServiceMap2.put(service.getServiceId(), service);
        }
        this.serviceMap = ServiceMap2;
        System.out.println("成功刷新所有service的状态");
    }

    public void flushRequestLog() {
        Map<String, Request> requestMap2 = new Hashtable<>();
        Iterable<Request> requests = requestDAO.findAll();
        for (Request request : requests) {
            requestMap2.put(request.getRequestPath(), request);
        }
        this.requestMap = requestMap2;
        System.out.println("成功刷新所有request的状态");
    }

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
        //                ValueOperations<String, PodRedis> podValueOperations = this.podRedisTemplate.opsForValue();
        //                System.out.println("test for redix across machine");
        //                System.out.println("PodName: " + podValueOperations.get("pod1").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod1").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod1").getMemUsage());
        //                System.out.println("PodName: " + podValueOperations.get("pod2").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod2").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod2").getMemUsage());
        //                System.out.println("PodName: " + podValueOperations.get("pod3").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod3").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod3").getMemUsage());
        //                System.out.println("PodName: " + podValueOperations.get("pod4").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod4").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod4").getMemUsage());
        //                System.out.println("PodName: " + podValueOperations.get("pod5").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod5").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod5").getMemUsage());
        //                System.out.println("PodName: " + podValueOperations.get("pod6").getPodName() + ",CPU: "
        //                                   + podValueOperations.get("pod6").getCpuUsage() + ",Mem: "
        //                                   + podValueOperations.get("pod6").getMemUsage());
    }
}
