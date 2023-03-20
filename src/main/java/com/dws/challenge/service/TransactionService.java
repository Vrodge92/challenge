package com.dws.challenge.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

	@Getter
	private final AccountsRepository accountsRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	public TransactionService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	/**
	 * This method transfer balance from one account to another account Also we used
	 * parallel process to perform operation
	 * 
	 * @param transfer
	 * @return
	 * @throws OverDraftException
	 * @throws AccountNotExistException
	 */
	public CompletableFuture<TransferResult> transferBalances(TransferRequest transfer)
			throws OverDraftException, AccountNotExistException {
		log.info(System.currentTimeMillis() + " ::saving list of users of size {}", transfer.getAccountFromId(),
				"" + Thread.currentThread().getName());
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
		notificationService.notifyAboutTransfer(accountFrom, NotificationMessage.DEBITED_MSG + accountFrom.getBalance()
				+ " for account id :- " + accountTo.getAccountId());
		notificationService.notifyAboutTransfer(accountTo,
				NotificationMessage.CREDITED_MSG + accountTo.getBalance() + " From :- " + accountFrom.getAccountId());
		return CompletableFuture.completedFuture(result);

	}

	/**
	 * This method check balance for FROM user and if balance is over draft then
	 * throw OverDraftException and AccountNotExistException. Also this method check
	 * FROM and TO account exist or not
	 * 
	 * @param request
	 */
	public void checkBalance(TransferRequest request) {
		Account accountFrom = this.accountsRepository.getAccount(request.getAccountFromId());
		Account accountTo = this.accountsRepository.getAccount(request.getAccountToId());

		if (accountFrom == null) {
			throw new AccountNotExistException("Account with id:" + request.getAccountFromId() + " does not exist.",
					ErrorCode.ACCOUNT_ERROR);
		}
		if (accountTo == null) {
			throw new AccountNotExistException("Account with id:" + request.getAccountToId() + " does not exist.",
					ErrorCode.ACCOUNT_ERROR);
		}
		if (accountFrom.getBalance().compareTo(request.getAmount()) < 0) {
			throw new OverDraftException(
					"Account with id:" + accountFrom.getAccountId() + " does not have enough balance to transfer.",
					ErrorCode.ACCOUNT_ERROR);
		}
	}
}
