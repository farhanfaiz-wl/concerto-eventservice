package ai.concerto.event.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import ai.concerto.event.dto.AuthCodeDeleteRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthCodeStreamPublisher {

	@Autowired private RedisTemplate redisTemplate;

	  @Value("${spring.redis.stream.auth-code.key}")
	  private String authcodeStreamKey;
	  
	  
	  public void publishAuthcodeDeleteRequest(
		      String applicationId, String userId) {
		   AuthCodeDeleteRequest authCodeDeleteRequest = AuthCodeDeleteRequest.builder().applicationId(applicationId).userId(userId).build();
		    ObjectRecord<String, AuthCodeDeleteRequest> eventRecord =
		        StreamRecords.newRecord().ofObject(authCodeDeleteRequest).withStreamKey(authcodeStreamKey);
		    redisTemplate.opsForStream().add(eventRecord);
		  }
}
