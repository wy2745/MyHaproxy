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

    @RequestMapping(value = "/redirect", method = { RequestMethod.GET, RequestMethod.POST })
    public void redirect(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) {
        try {
            System.out.println(System.getProperty("java.library.path"));
            dispatchService.memory();
            dispatchService.cpu();
        } catch (SigarException e1) {
            System.out.println(e1);
        }

        System.out.println(dispatchService.getAction(httpServletRequest));
        if (httpServletRequest.getMethod().equals("GET")) {
            try {
                httpServletResponse.sendRedirect(
                    "http://zui.ms/api/user/users/images?" + httpServletRequest.getQueryString());
            } catch (IOException e) {
                logger.error("", e);
            }
        } else {
            httpServletResponse.setStatus(307);
            httpServletResponse.addHeader("Location", "http://zui.ms/api/user/users/");
        }
    }
}
