package com.hamkkebu.authservice.repository;

import com.hamkkebu.authservice.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 엔티티의 데이터 액세스를 담당하는 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 삭제되지 않은 사용자 조회
     *
     * @param username 사용자명
     * @return User Optional
     */
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    /**
     * 이메일로 삭제되지 않은 사용자 조회
     *
     * @param email 이메일
     * @return User Optional
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * 사용자명 중복 확인 (삭제되지 않은 사용자)
     *
     * @param username 사용자명
     * @return 존재하면 true
     */
    boolean existsByUsernameAndIsDeletedFalse(String username);

    /**
     * 이메일 중복 확인 (삭제되지 않은 사용자)
     *
     * @param email 이메일
     * @return 존재하면 true
     */
    boolean existsByEmailAndIsDeletedFalse(String email);
}
