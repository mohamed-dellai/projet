package com.example.ebank.repositories;

import com.example.ebank.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation,Long> {
    List<AccountOperation> findAccountOperationByBankAccountId(String accountId);
    Page<AccountOperation> findAccountOperationByBankAccountIdOrderByOperationDateDesc(String accountId, Pageable pageable);

}
