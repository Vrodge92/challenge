package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.OverDraftException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;
	
	Account getAccount(String accountId);

	void clearAccounts();
	
	public TransferResult transferBalances(TransferRequest transfer) throws OverDraftException,AccountNotExistException;
}
