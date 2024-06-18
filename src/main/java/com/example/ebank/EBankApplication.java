package com.example.ebank;

import com.example.ebank.dtos.BankAccountDTO;
import com.example.ebank.dtos.CurrentBankAccountDTO;
import com.example.ebank.dtos.CustomerDTO;
import com.example.ebank.dtos.SavingBankAccountDTO;
import com.example.ebank.entities.*;
import com.example.ebank.enums.AccountStatus;
import com.example.ebank.enums.OperationType;
import com.example.ebank.exceptions.BalanceNotSufficientException;
import com.example.ebank.exceptions.BankAccountNotFoundException;
import com.example.ebank.exceptions.CustomerNotFoundException;
import com.example.ebank.repositories.AccountOperationRepository;
import com.example.ebank.repositories.BankAccountRepository;
import com.example.ebank.repositories.CustomerRepository;
import com.example.ebank.services.BankAccountService;
import com.example.ebank.services.BankService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService){
        return args -> {
            Stream.of("eya","mariem","nour").forEach(name-> {
                CustomerDTO customer = new CustomerDTO();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customer.setPhoneNumber("21334568");
                customer.setAddress("Tunis, carthage");
                customer.setIsBlocked(false);
                bankAccountService.saveCustomer(customer);
            });

            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random()*18000, 9000,customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random()*19000,5.5, customer.getId());

                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });

            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for(BankAccountDTO bankAccount:bankAccounts) {
                for (int i = 0; i < 3; i++) {
                    String accountId;
                    if(bankAccount instanceof SavingBankAccountDTO){
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    } else {
                        accountId=((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000 + Math.random() * 12000, "Credit");
                    bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "Debit");
                }
            }
        };
    }
    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("mariem","hedi", "amir").forEach(name-> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(customer ->{
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random()*7000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(customer);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random()*7000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(customer);
                savingAccount.setInterestRate(0.5);
                bankAccountRepository.save(savingAccount);

                bankAccountRepository.findAll().forEach(acc -> {
                    for(int i = 0; i < 10; i++) {
                        AccountOperation accountOperation = new AccountOperation();
                        accountOperation.setOperationDate(new Date());
                        accountOperation.setAmount(Math.random()*15000);
                        accountOperation.setType(Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT);
                        accountOperation.setBankAccount(acc);
                        accountOperationRepository.save(accountOperation);
                    }
                });

            });
        };
    }
}
