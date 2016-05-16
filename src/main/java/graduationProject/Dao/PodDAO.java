package graduationProject.Dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import graduationProject.Domain.Pod;

public interface PodDAO extends CrudRepository<Pod, String> {
    public Pod findByPodName(String podName);

    public List<Pod> findByServiceId(int serviceId);
}
