package com.example.ebank.services;

import com.example.ebank.dtos.*;
import com.example.ebank.entities.*;
import com.example.ebank.enums.OperationType;
import com.example.ebank.exceptions.*;
import com.example.ebank.mappers.BankAccountMapperImpl;
import com.example.ebank.repositories.AccountOperationRepository;
import com.example.ebank.repositories.BankAccountRepository;
import com.example.ebank.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class BankAccountServiceImpl implements BankAccountService {

    public BankAccountServiceImpl(CustomerRepository customerRepository, BankAccountRepository bankAccountRepository,
			AccountOperationRepository accountOperationRepository) {
		super();
		this.customerRepository = customerRepository;
		this.bankAccountRepository = bankAccountRepository;
		this.accountOperationRepository = accountOperationRepository;
	}

	private  CustomerRepository customerRepository;
    private  BankAccountRepository bankAccountRepository;
    private  final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapperImpl dtoMapper = new BankAccountMapperImpl();

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);

        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);

        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank Account not found!"));

        if (bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else if (bankAccount instanceof CurrentAccount) {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        } else {
            // Handle other types of bank accounts if needed
            throw new BankAccountNotFoundException("Bank Account type not supported!");
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank Account not found!"));

        if (bankAccount.getBalance() < amount) {
            throw new BalanceNotSufficientException("Balance not sufficient");
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank Account not found!"));

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);
    }

    @Override
    public List<BankAccountDTO> customerBankAccountsList(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        List<BankAccount> bankAccounts = bankAccountRepository.findBankAccountsByCustomer(customer);
        return bankAccounts.stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof SavingAccount) {
                        SavingAccount savingAccount = (SavingAccount) bankAccount;
                        return dtoMapper.fromSavingBankAccount(savingAccount);
                    } else if (bankAccount instanceof CurrentAccount) {
                        CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                        return dtoMapper.fromCurrentBankAccount(currentAccount);
                    } else {
                        // Handle other types of bank accounts if needed
                        throw new IllegalArgumentException("Bank Account type not supported!");
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccounts.stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof SavingAccount) {
                        SavingAccount savingAccount = (SavingAccount) bankAccount;
                        return dtoMapper.fromSavingBankAccount(savingAccount);
                    } else if (bankAccount instanceof CurrentAccount) {
                        CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                        return dtoMapper.fromCurrentBankAccount(currentAccount);
                    } else {
                        // Handle other types of bank accounts if needed
                        throw new IllegalArgumentException("Bank Account type not supported!");
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findAccountOperationByBankAccountId(accountId);
        return accountOperations.stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        Page<AccountOperation> accountOperations = accountOperationRepository.findAccountOperationByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));

        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());

        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());

        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        return customers.stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }
}
