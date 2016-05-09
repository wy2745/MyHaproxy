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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import graduationProject.Dao.PodDAO;
import graduationProject.Dao.RequestDAO;
import graduationProject.Dao.ServiceDAO;
import graduationProject.Domain.Pod;
import graduationProject.Domain.Request;
import graduationProject.Domain.testRedis;

@Service
public class DispatchService extends BaseService {

    @Autowired
    private RedisTemplate<String, testRedis>               redisTemplate;

    @Autowired
    private RedisTemplate<String, String>                  template;

    @Autowired
    private PodDAO                                         podDAO;

    @Autowired
    private RequestDAO                                     requestDAO;

    @Autowired
    private ServiceDAO                                     serviceDAO;

    private Map<Integer, graduationProject.Domain.Service> serviceMap;

    private Map<Integer, List<String>>                     PodInService;

    private Map<String, Pod>                               podsMap;

    private Map<String, Request>                           requestMap;

    public void testRedis() {
        //        Pod pod = new Pod();
        //        pod.setAddress("haha");
        //        pod.setCpuUsage(1);
        //        pod.setMemUsage(1);
        //        pod.setPodName("123");
        //        pod.setServiceId(1);
        //        ValueOperations<String, Pod> valueOperations = redisTemplate.opsForValue();
        //        valueOperations.set(pod.getPodName(), pod);
        //
        //        System.out.println(valueOperations.get(pod.getPodName()));
        //        ValueOperations<String, String> ops = this.template.opsForValue();
        //        String key = "spring.boot.redis.test";
        //        if (!this.template.hasKey(key)) {
        //            ops.set(key, "foo");
        //            System.out.println("haha");
        //        }
        //        System.out.println("Found key " + key + ", value=" + ops.get(key));
        ValueOperations<String, testRedis> operations = this.redisTemplate.opsForValue();
        testRedis testRedis = new testRedis("haha", 12);
        if (operations.get("90") == null) {
            operations.set("90", testRedis);
            System.out.println("...");
        }
        System.out.println(operations.get("90").getUsername());
        this.redisTemplate.delete("90");
    }

    @Scheduled(initialDelay = 1000, fixedRate = 300000)
    public void init() {
        Map<Integer, graduationProject.Domain.Service> serviceMap2 = new Hashtable<Integer, graduationProject.Domain.Service>();
        Map<Integer, List<String>> PodInService2 = new Hashtable<>();
        Map<String, Pod> podsMap2 = new Hashtable<>();
        Map<String, Request> requestMap2 = new HashMap<>();

        Iterable<graduationProject.Domain.Service> services = serviceDAO.findAll();
        for (graduationProject.Domain.Service service : services)
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

    public void dispatchRequest(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, String mode) {
        String method = httpServletRequest.getMethod();
        String requestPath = getRequestPath(httpServletRequest);
        Request request = getRequestByPath(requestPath);
        if (!method.equals(request.getMethod())) {
            System.out.println("method错误");
            return;
        }
        graduationProject.Domain.Service service = getServiceById(request.getServiceId());
        String desination = pick(request, service, mode);
    }

    private int generateRanNum(int max) {
        Random random = new Random();
        int start = random.nextInt(max);
        return (random.nextInt(max) + start) % max;
    }

    private String pick(Request request, graduationProject.Domain.Service service, String mode) {
        if (mode.equals("random")) {
            int max = PodInService.get(service.getServiceId()).size();
            int target = generateRanNum(max);
            return PodInService.get(service.getServiceId()).get(target);
        }
        return null;
    }

    public Request getRequestByPath(String requestPath) {
        Request request = requestMap.get(requestPath);
        if (request == null)
            System.out.println("不存在对应的请求信息");
        return request;
    }

    public graduationProject.Domain.Service getServiceById(int serviceId) {
        graduationProject.Domain.Service service = serviceMap.get(serviceId);
        if (service == null)
            System.out.println("不存在对应的service");
        return service;
    }

    public Pod getPodByName(String podName) {
        Pod pod = this.podsMap.get(podName);
        if (pod == null)
            System.out.println("pod不存在");
        return pod;
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

    public void setPods(Map<String, Pod> pods) {
        this.podsMap = pods;
    }

    public void addPod(Pod pod) {
        if (this.podsMap.containsKey(pod.getPodName()))
            System.out.println("已存在pod");
        else
            this.podsMap.put(pod.getPodName(), pod);
    }

    public void deletePod(Pod pod) {
        if (this.podsMap.containsKey(pod.getPodName()))
            this.podsMap.remove(pod.getPodName());
    }

    public void flushPod() {
        this.podsMap.clear();
        this.PodInService.clear();
        Iterable<Pod> podset = podDAO.findAll();
        for (Pod pod : podset) {
            this.podsMap.put(pod.getPodName(), pod);
            if (this.PodInService.containsKey(pod.getServiceId()))
                this.PodInService.get(pod.getServiceId()).add(pod.getPodName());
            else {
                List<String> podName = new Vector<>();
                podName.add(pod.getPodName());
                this.PodInService.put(pod.getServiceId(), podName);
            }
        }
        System.out.println("成功刷新所有pod的状态");
    }

    public void flushService() {
        this.serviceMap.clear();
        Iterable<graduationProject.Domain.Service> services = serviceDAO.findAll();
        for (graduationProject.Domain.Service service : services) {
            this.serviceMap.put(service.getServiceId(), service);
        }
        System.out.println("成功刷新所有service的状态");
    }

    public void flushRequestLog() {
        this.requestMap.clear();
        Iterable<Request> requests = requestDAO.findAll();
        for (Request request : requests) {
            this.requestMap.put(request.getRequestPath(), request);
        }
        System.out.println("成功刷新所有request的状态");
    }

    public Map<String, Pod> getPods() {
        return this.podsMap;
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
}
