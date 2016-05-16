package graduationProject.Dao;

import org.springframework.data.repository.CrudRepository;

import graduationProject.Domain.Request;

public interface RequestDAO extends CrudRepository<Request, Integer> {
    public Request findByRequestPath(String requestPath);
}
