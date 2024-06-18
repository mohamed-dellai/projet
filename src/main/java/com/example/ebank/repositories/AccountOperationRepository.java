package com.example.ebank.repositories;

import com.example.ebank.entities.AccountOperation;
import com.example.ebank.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountOperationRepository extends JpaRepository<AccountOperation,Long> {

}
