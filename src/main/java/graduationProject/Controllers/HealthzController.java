package graduationProject.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 
 * @author panda
 * @version $Id: HealthzController.java, v 0.1 2016年5月7日 下午3:37:37 panda Exp $
 */
@RestController
public class HealthzController extends BaseController {

    /**
     * 健康检查API
     * 
     * @return String
     */
    @RequestMapping("/healthz")
    public String healthz() {
        return "Hello World";
    }
}
