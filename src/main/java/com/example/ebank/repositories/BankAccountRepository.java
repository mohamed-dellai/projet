package com.example.ebank.repositories;

import com.example.ebank.entities.BankAccount;
import com.example.ebank.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {

    List<BankAccount> findBankAccountsByCustomer(Customer customer);
}
