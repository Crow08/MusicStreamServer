package de.crow08.musicstreamserver.users;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
  User findByUsername(String username);
}
