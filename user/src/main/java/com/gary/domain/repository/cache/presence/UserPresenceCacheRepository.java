package com.gary.domain.repository.cache.presence;

import com.gary.web.dto.cache.presence.UserPresenceCacheDto;
import org.springframework.data.repository.CrudRepository;
import java.util.UUID;

public interface UserPresenceCacheRepository extends CrudRepository<UserPresenceCacheDto, UUID> {
    // Add custom methods if needed, e.g., find by status
}
