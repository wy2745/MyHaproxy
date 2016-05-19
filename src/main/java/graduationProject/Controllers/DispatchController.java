package graduationProject.Controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import graduationProject.Services.DispatchService;

/*import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;*/

/**
 * 
 * 
 * @author panda
 * @version $Id: UserController.java, v 0.1 2016年5月7日 下午3:37:23 panda Exp $
 */
@CrossOrigin
@RestController
public class DispatchController extends BaseController {
    @Autowired
    private DispatchService dispatchService;

    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public void Generate() {
        dispatchService.generateForTest();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public void Delete() {
        dispatchService.deleteForTest();
    }

    @RequestMapping(value = "/javapath", method = RequestMethod.GET)
    public void javapath() {
        //System.out.println(System.getProperty("java.library.path"));
        try {
            dispatchService.memory();
            dispatchService.cpu();
        } catch (SigarException e) {
            logger.error("", e);
        }

    }

    @RequestMapping(value = "/testRedisInUbuntu", method = RequestMethod.GET)
    public void test() {
        dispatchService.testRedis();
    }

    @RequestMapping(value = "/testRandom", method = { RequestMethod.GET, RequestMethod.POST })
    public void teestrandom(HttpServletRequest httpServletRequest,
                            HttpServletResponse httpServletResponse) {
        //        dispatchService.deleteForTest();
        dispatchService.dispatchRequest(httpServletRequest, httpServletResponse, "random");

    }

    @RequestMapping(value = "/testChoice", method = { RequestMethod.GET, RequestMethod.POST })
    public void testchoice(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) {
        //        dispatchService.deleteForTest();
        dispatchService.dispatchRequest(httpServletRequest, httpServletResponse, "choice1");

    }

    @RequestMapping(value = "/redirect", method = { RequestMethod.GET, RequestMethod.POST })
    public void redirect(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) {
        //System.out.println(System.getProperty("java.library.path"));
        if (httpServletRequest.getMethod().equals("GET")) {
            try {
                httpServletResponse.sendRedirect(
                    "http://zui.ms/api/user/users/images?" + httpServletRequest.getQueryString());
            } catch (IOException e) {
                logger.error("", e);
            }
        } else {
            httpServletResponse.setStatus(307);
            httpServletResponse.addHeader("Location", "http://zui.ms/api/user/users");
        }
    }

    @RequestMapping(value = "/proxy", method = { RequestMethod.GET, RequestMethod.POST })
    public void proxy(HttpServletRequest httpServletRequest,
                      HttpServletResponse httpServletResponse) {
        //System.out.println(System.getProperty("java.library.path"));

        dispatchService.dispatchRequest(httpServletRequest, httpServletResponse, "random");
    }
}
