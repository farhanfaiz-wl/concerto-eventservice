package ai.concerto.event.auth.repository;

import ai.concerto.event.auth.model.ApiToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiTokenRepository extends CrudRepository<ApiToken, String> {}
