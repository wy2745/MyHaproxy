package graduationProject.Dao;

import org.springframework.data.repository.CrudRepository;

import graduationProject.Domain.Pod;

public interface PodDAO extends CrudRepository<Pod, String> {

}
