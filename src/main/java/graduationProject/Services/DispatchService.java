package graduationProject.Services;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

@Service
public class DispatchService extends BaseService {

    public String getAction(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getMethod().equals("GET")) {
            return httpServletRequest.getParameter("Action");
        } else {
            try {
                BufferedReader reader = httpServletRequest.getReader();
                String str = reader.readLine();
                while (str != null) {
                    if (str.contains("Action")) {
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
}
