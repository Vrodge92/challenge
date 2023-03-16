package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransactionService;

import lombok.SneakyThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransactionServiceTest {

	@Autowired
	AccountsService accountsService;

	@Autowired
	TransactionService transactionService;

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
		CompletableFuture<TransferResult> result = transactionService.transferBalances(transferRequest);
		String value = result.get().getAccountFromId();
		BigDecimal balance = result.get().getBalanceAfterTransfer();
		assertThat(this.accountsService.getAccount("Id-123").getAccountId()).isEqualTo(value);
		assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(balance);
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
		CompletableFuture<TransferResult> result = transactionService.transferBalances(transferRequest);
		String value = result.get().getAccountFromId();
		BigDecimal balance = result.get().getBalanceAfterTransfer();
		assertThat(this.accountsService.getAccount("Id-1231").getAccountId()).isEqualTo(value);
		assertThat(this.accountsService.getAccount("Id-1231").getBalance()).isEqualTo(balance);
	}

	/**
	 * This test added to check multiple asyn call using executor service to check
	 * thread safety for transferBalance method so user will get notify with there
	 * actual transaction
	 * 
	 * @throws Exception
	 */
	@Test
	@SneakyThrows
	public void testTransferBalanceWithMultipleTrread() throws Exception {
		Account account1 = new Account("Id-11230");
		account1.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account1);

		assertThat(this.accountsService.getAccount("Id-11230")).isEqualTo(account1);

		Account account2 = new Account("Id-11240");
		account2.setBalance(new BigDecimal(500));
		this.accountsService.createAccount(account2);
		assertThat(this.accountsService.getAccount("Id-11240")).isEqualTo(account2);

		Account account3 = new Account("Id-11250");
		account3.setBalance(new BigDecimal(900));
		this.accountsService.createAccount(account3);
		assertThat(this.accountsService.getAccount("Id-11250")).isEqualTo(account3);

		Account account4 = new Account("Id-11260");
		account4.setBalance(new BigDecimal(600));
		this.accountsService.createAccount(account4);
		assertThat(this.accountsService.getAccount("Id-11260")).isEqualTo(account4);

		Account account5 = new Account("Id-11270");
		account5.setBalance(new BigDecimal(200));
		this.accountsService.createAccount(account5);
		assertThat(this.accountsService.getAccount("Id-11270")).isEqualTo(account5);

		Account account6 = new Account("Id-11280");
		account6.setBalance(new BigDecimal(100));
		this.accountsService.createAccount(account6);
		assertThat(this.accountsService.getAccount("Id-11280")).isEqualTo(account6);

		Account account7 = new Account("Id-11290");
		account7.setBalance(new BigDecimal(400));
		this.accountsService.createAccount(account7);
		assertThat(this.accountsService.getAccount("Id-11290")).isEqualTo(account7);

		Account account8 = new Account("Id-11291");
		account8.setBalance(new BigDecimal(300));
		this.accountsService.createAccount(account8);
		assertThat(this.accountsService.getAccount("Id-11291")).isEqualTo(account8);

		TransferRequest transferRequest = new TransferRequest("Id-11230", "Id-11240", BigDecimal.valueOf(10));
		TransferRequest transferRequest1 = new TransferRequest("Id-11250", "Id-11260", BigDecimal.valueOf(10));
		TransferRequest transferRequest2 = new TransferRequest("Id-11270", "Id-11280", BigDecimal.valueOf(10));
		TransferRequest transferRequest3 = new TransferRequest("Id-11290", "Id-11291", BigDecimal.valueOf(10));
		int numberOfThreads = 1;

		ExecutorService service = Executors.newFixedThreadPool(10);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++) {
			service.execute(() -> {
				CompletableFuture<TransferResult> users11 = transactionService.transferBalances(transferRequest);
				CompletableFuture<TransferResult> users22 = transactionService.transferBalances(transferRequest1);
				CompletableFuture<TransferResult> users33 = transactionService.transferBalances(transferRequest2);
				CompletableFuture<TransferResult> users44 = transactionService.transferBalances(transferRequest3);
				CompletableFuture.allOf(users11, users22, users33, users44).join();
				latch.countDown();
			});
		}
	}
}
