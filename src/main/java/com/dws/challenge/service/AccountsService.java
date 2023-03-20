package com.dws.challenge.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.constant.ErrorCode;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	/**
	 * This method check given account id present in existing map and return Account
	 * object or if account id not available then return AccountNotExistException
	 * 
	 * @param accountId
	 * @return
	 */
	public Account getAccount(String accountId) {
		Map<String, Account> accounts = this.accountsRepository.getAccount();

		Account account = Optional.ofNullable(accounts.get(accountId))
				.orElseThrow(() -> new AccountNotExistException("Account with id:" + accountId + " does not exist.",
						ErrorCode.ACCOUNT_ERROR));

		return account;
	}

}
