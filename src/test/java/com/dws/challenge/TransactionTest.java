package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransactionTest {

	@Autowired
	AccountsService accountsService;
	
	@Test
	public void testTransferBalance() throws Exception {

		Account account1 = new Account("Id-123");
		account1.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(account1);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account1);

		Account account2 = new Account("Id-124");
		account2.setBalance(new BigDecimal(50));
		this.accountsService.createAccount(account2);

		assertThat(this.accountsService.getAccount("Id-124")).isEqualTo(account2);

		TransferRequest transferRequest = new TransferRequest("Id-123", "Id-124", BigDecimal.valueOf(10));
		TransferResult result = accountsService.transferBalances(transferRequest);
		assertThat(this.accountsService.getAccount("Id-123").getAccountId()).isEqualTo(result.getAccountFromId());
		assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(result.getBalanceAfterTransfer());
	}
	
	@Test
	public void testOverdraftBalance() throws OverDraftException, AccountNotExistException, Exception {

		Account account1 = new Account("Id-1");
		account1.setBalance(new BigDecimal(10));
		this.accountsService.createAccount(account1);
		assertThat(this.accountsService.getAccount("Id-1")).isEqualTo(account1);

		Account account2 = new Account("Id-2");
		account2.setBalance(new BigDecimal(50));
		this.accountsService.createAccount(account2);
		assertThat(this.accountsService.getAccount("Id-2")).isEqualTo(account2);

		TransferRequest transferRequest = new TransferRequest("Id-1", "Id-2", BigDecimal.valueOf(20));
		OverDraftException thrown = Assertions.assertThrows(OverDraftException.class, () -> {
			accountsService.transferBalances(transferRequest);
		});
		assertTrue(thrown.getMessage().contentEquals(
				"Account with id:" + account1.getAccountId() + " does not have enough balance to transfer."));
	}
	
	@Test
	public void testTransferZeroBalance() throws Exception {

		Account account1 = new Account("Id-1231");
		account1.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(account1);

		assertThat(this.accountsService.getAccount("Id-1231")).isEqualTo(account1);

		Account account2 = new Account("Id-1241");
		account2.setBalance(new BigDecimal(50));
		this.accountsService.createAccount(account2);

		assertThat(this.accountsService.getAccount("Id-1241")).isEqualTo(account2);

		TransferRequest transferRequest = new TransferRequest("Id-1231", "Id-1241", BigDecimal.valueOf(0));
		TransferResult result = accountsService.transferBalances(transferRequest);
		assertThat(this.accountsService.getAccount("Id-1231").getAccountId()).isEqualTo(result.getAccountFromId());
		assertThat(this.accountsService.getAccount("Id-1231").getBalance()).isEqualTo(result.getBalanceAfterTransfer());
	}
	
	@Test
	public void testTransferBalanceWithEmptyFromId() throws Exception {

		Account account2 = new Account("Id-1242");
		account2.setBalance(new BigDecimal(50));
		this.accountsService.createAccount(account2);

		assertThat(this.accountsService.getAccount("Id-1242")).isEqualTo(account2);

		TransferRequest transferRequest = new TransferRequest("Id-1232", "Id-1242", BigDecimal.valueOf(10));
		AccountNotExistException thrown = Assertions.assertThrows(AccountNotExistException.class, ()->{
			accountsService.transferBalances(transferRequest);
		});
	
	}
	
	@Test
	public void testTransferBalanceWithEmptyToId() throws Exception {

		Account account1 = new Account("Id-1233");
		account1.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(account1);

		assertThat(this.accountsService.getAccount("Id-1233")).isEqualTo(account1);

		TransferRequest transferRequest = new TransferRequest("Id-1233", "Id-1244", BigDecimal.valueOf(10));
		AccountNotExistException thrown = Assertions.assertThrows(AccountNotExistException.class, ()->{
			accountsService.transferBalances(transferRequest);
		});
	
	}
}
