package com.example.CJLInvestimentos.repositories;

import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.empresa WHERE u.id = :id")
    Optional<User> findByIdWithEmpresa(Long id);
    List<User> findByEmpresaId(Long empresaId);
    List<User> findByEmpresaIdAndRole(Long empresaId, Role role);
    long countByEmpresaId(Long empresaId);
    long countByEmpresaIdAndRole(Long empresaId, Role role);
    long countByRole(Role role);

    Optional<User> findByTokenConvite(String tokenConvite);
    Optional<User> findByCpf(String cpf);
}
