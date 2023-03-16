package com.dws.challenge.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.constant.ErrorCode;
import com.dws.challenge.constant.NotificationMessage;
import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * This method transfer balance from one account to another account Also we used
	 * synchronized block to manage thread safety
	 * 
	 * @param transfer
	 * @return
	 * @throws OverDraftException
	 * @throws AccountNotExistException
	 */
	public TransferResult transferBalances(TransferRequest transfer)
			throws OverDraftException, AccountNotExistException {
		synchronized (transfer) {
			Map<String, Account> accounts = this.accountsRepository.transferBalances(transfer);
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
			notificationService.notifyAboutTransfer(accountFrom,
					NotificationMessage.DEBITED_MSG + accountFrom.getBalance() +" for account id :- "+ accountTo.getAccountId());
			notificationService.notifyAboutTransfer(accountTo, NotificationMessage.CREDITED_MSG + accountTo.getBalance() +" From :- "+accountFrom.getAccountId());
			
			return result;
		}

	}
}
