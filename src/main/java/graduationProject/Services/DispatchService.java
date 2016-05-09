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

    public void testRedis() {
        //        ValueOperations<String, testRedis> operations = this.redisTemplate.opsForValue();
        //        testRedis testRedis = new testRedis("haha", 12);
        //        if (operations.get("90") == null) {
        //            operations.set("90", testRedis);
        //            System.out.println("...");
        //        }
        //        System.out.println(operations.get("90").getUsername());
        //        this.redisTemplate.delete("90");

        ValueOperations<String, PodRedis> podRedisoperations = this.podRedisTemplate.opsForValue();
        ValueOperations<String, RequestRedis> requestRedisoperations = this.requestRedisTemplate
            .opsForValue();
        ValueOperations<String, ServiceRedis> serviceRedisoperations = this.serviceRedisTemplate
            .opsForValue();
        ValueOperations<String, List<String>> podInServiceRedisoperations = this.podInServiceRedisTemplate
            .opsForValue();

        PodRedis podRedis = new PodRedis();
        podRedis.setAddress("222.222.22");
        podRedis.setCpuUsage(1);
        podRedis.setMemUsage(1);
        podRedis.setPodName("haha");
        podRedis.setServiceId(1);

        PodRedis podRedis2 = new PodRedis();
        podRedis2.setAddress("222.222.22");
        podRedis2.setCpuUsage(1);
        podRedis2.setMemUsage(1);
        podRedis2.setPodName("haha2");
        podRedis2.setServiceId(2);

        List<String> pod = new Vector<>();
        pod.add("haha");
        pod.add("haha2");

        ServiceRedis serviceRedis = new ServiceRedis();
        serviceRedis.setServiceId(1);
        serviceRedis.setServiceName("pupu");
        serviceRedis.setServiceType("a");

        RequestRedis requestRedis = new RequestRedis();
        requestRedis.setCpuCost(1);
        requestRedis.setMemCost(1);
        requestRedis.setMethod("get");
        requestRedis.setRequestId(1);
        requestRedis.setRequestPath("/user");
        requestRedis.setServiceId(1);
        requestRedis.setTimeCost(1);

        if (podRedisoperations.get(podRedis.getPodName()) == null) {
            System.out.println("pod: " + podRedis.getPodName() + "不存在");
            podRedisoperations.set(podRedis.getPodName(), podRedis);
        }
        System.out.println("pod: " + podRedisoperations.get(podRedis.getPodName()).getPodName());

        if (podRedisoperations.get(podRedis2.getPodName()) == null) {
            System.out.println("pod: " + podRedis2.getPodName() + "不存在");
            podRedisoperations.set(podRedis2.getPodName(), podRedis2);
        }
        System.out.println("pod: " + podRedisoperations.get(podRedis2.getPodName()).getPodName());

        if (serviceRedisoperations.get(String.valueOf(serviceRedis.getServiceId())) == null) {
            System.out.println("service: " + serviceRedis.getServiceId() + "不存在");
            serviceRedisoperations.set(String.valueOf(serviceRedis.getServiceId()), serviceRedis);
        }
        System.out.println("service: " + serviceRedisoperations
            .get(String.valueOf(serviceRedis.getServiceId())).getServiceId());

        if (podInServiceRedisoperations
            .get("p" + String.valueOf(serviceRedis.getServiceId())) == null) {
            System.out.println("podInService: " + serviceRedis.getServiceId() + "不存在");
            podInServiceRedisoperations.set("p" + String.valueOf(serviceRedis.getServiceId()), pod);
        }
        System.out.println("podInService: "
                           + podInServiceRedisoperations
                               .get("p" + String.valueOf(serviceRedis.getServiceId())).get(0)
                           + "  " + podInServiceRedisoperations
                               .get("p" + String.valueOf(serviceRedis.getServiceId())).get(1));

        if (requestRedisoperations.get(requestRedis.getRequestPath()) == null) {
            System.out.println("request: " + requestRedis.getRequestPath() + "不存在");
            requestRedisoperations.set(requestRedis.getRequestPath(), requestRedis);
        }
        System.out
            .println("request: "
                     + requestRedisoperations.get(requestRedis.getRequestPath()).getRequestPath());

        requestRedisTemplate.delete(requestRedis.getRequestPath());
        podInServiceRedisTemplate.delete("p" + String.valueOf(serviceRedis.getServiceId()));
        podRedisTemplate.delete(podRedis.getPodName());
        podRedisTemplate.delete(podRedis2.getPodName());
        serviceRedisTemplate.delete(String.valueOf(serviceRedis.getServiceId()));
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

    public void dispatchRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String mode) {
        String method = httpServletRequest.getMethod();
        String requestPath = getRequestPath(httpServletRequest);
        RequestRedis requestRedis = getRequestByPath(requestPath);
        if (!method.equals(requestRedis.getMethod())) {
            System.out.println("method错误");
            return;
        }
        ServiceRedis serviceRedis = getServiceById(requestRedis.getServiceId());
        String desination = pick(requestRedis, serviceRedis, mode);
    }

    private int generateRanNum(int max) {
        Random random = new Random();
        int start = random.nextInt(max);
        return (random.nextInt(max) + start) % max;
    }

    private String pick(RequestRedis requestRedis, ServiceRedis serviceRedis, String mode) {
        //        //  mysql实现版本
        //        if (mode.equals("random")) {
        //            int max = PodInService.get(service.getServiceId()).size();
        //            int target = generateRanNum(max);
        //            return PodInService.get(service.getServiceId()).get(target);
        //        }
        //        return null;

        if (mode.equals("random")) {
            ValueOperations<String, List<String>> valueOperations = podInServiceRedisTemplate
                .opsForValue();
            int max = valueOperations.get("p" + String.valueOf(serviceRedis.getServiceId())).size();
            int target = generateRanNum(max);
            return valueOperations.get("p" + String.valueOf(serviceRedis.getServiceId()))
                .get(target);
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
                        try {
                            System.out.println("Cpu");
                            cpu();
                            System.out.println("Mem");
                            memory();
                        } catch (SigarException e) {
                            logger.error("", e);
                        }
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

    public void addPod(PodRedis podRedis) {
        //        //  mysql实现版本
        //        if (this.podsMap.containsKey(pod.getPodName()))
        //            System.out.println("已存在pod");
        //        else
        //            this.podsMap.put(pod.getPodName(), pod);
        ValueOperations<String, PodRedis> valueOperations = this.podRedisTemplate.opsForValue();
        if (valueOperations.get(podRedis.getPodName()) != null)
            System.out.println("已存在pod");
        else
            valueOperations.set(podRedis.getPodName(), podRedis);
    }

    public void deletePod(PodRedis podRedis) {
        //        //  mysql实现版本
        //        if (this.podsMap.containsKey(pod.getPodName()))
        //            this.podsMap.remove(pod.getPodName());
        ValueOperations<String, PodRedis> valueOperations = this.podRedisTemplate.opsForValue();
        if (valueOperations.get(podRedis.getPodName()) == null)
            System.out.println("不存在pod");
        else
            this.podRedisTemplate.delete(podRedis.getPodName());
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
}
