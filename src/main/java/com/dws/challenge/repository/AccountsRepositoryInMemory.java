package com.dws.challenge.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.dws.challenge.constant.ErrorCode;
import com.dws.challenge.constant.NotificationMessage;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.NotificationService;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Autowired
	NotificationService notificationService;

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}

	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public TransferResult transferBalances(TransferRequest transfer)
			throws OverDraftException, AccountNotExistException {
		Account accountFrom = Optional.ofNullable(accounts.get(transfer.getAccountFromId()))
				.orElseThrow(() -> new AccountNotExistException(
						"Account with id:" + transfer.getAccountFromId() + " does not exist.",
						ErrorCode.ACCOUNT_ERROR));
		Account accountTo = Optional.ofNullable(accounts.get(transfer.getAccountToId()))
				.orElseThrow(() -> new AccountNotExistException(
						"Account with id:" + transfer.getAccountFromId() + " does not exist.",
						ErrorCode.ACCOUNT_ERROR));

		if (accountFrom.getBalance().compareTo(transfer.getAmount()) < 0) {
			throw new OverDraftException(
					"Account with id:" + accountFrom.getAccountId() + " does not have enough balance to transfer.",
					ErrorCode.ACCOUNT_ERROR);
		}

		accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
		accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));

		TransferResult result = new TransferResult();
		result.setAccountFromId(transfer.getAccountFromId());
		result.setBalanceAfterTransfer(accountFrom.getBalance());

		// We need to notify user using notification service
		notificationService.notifyAboutTransfer(accountTo, NotificationMessage.CREDITED_MSG);
		return result;
	}
}
