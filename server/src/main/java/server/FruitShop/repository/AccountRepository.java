package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Page<Account> findByStatus(int status, Pageable pageable);
    Optional<Account> findByAccountPhone(String accountPhone);
    Optional<Account> findByAccountName(String accountName);

    @Query("SELECT a FROM Account a WHERE LOWER(a.accountName) LIKE LOWER(CONCAT('%', :accountName, '%'))")
    Page<Account> findByAccountNameContainingIgnoreCase(@Param("accountName") String accountName, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.roles")
    List<Account> findAllWithRoles();

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.status = :status")
    List<Account> findByStatusWithRoles(@Param("status") int status);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.roles WHERE LOWER(a.accountName) LIKE LOWER(CONCAT('%', :accountName, '%'))")
    List<Account> findByAccountNameContainingIgnoreCaseWithRoles(@Param("accountName") String accountName);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.accountId = :accountId")
    Optional<Account> findByIdWithRoles(@Param("accountId") String accountId);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.accountPhone = :accountPhone AND a.password = :password")
    Optional<Account> findByAccountPhoneAndPassword(@Param("accountPhone") String accountPhone, @Param("password") String password);
}
