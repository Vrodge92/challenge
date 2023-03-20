package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	@Order(1)
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	@Order(2)
	void createDuplicateAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		DuplicateAccountIdException thrown = Assertions.assertThrows(DuplicateAccountIdException.class, () -> {
			this.accountsService.createAccount(account);
		});
	}

	@Test
	@Order(3)
	void getAccount() throws Exception {
		Account account = new Account("Id-1234");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		Account accountDetails = this.accountsService.getAccount("Id-1234");
		assertThat(accountDetails.getAccountId()).isEqualTo(account.getAccountId());
		assertThat(accountDetails.getBalance()).isEqualTo(account.getBalance());
	}

	@Test
	@Order(4)
	void getAccountForUserNotFoundException() throws Exception {
		AccountNotExistException thrown = Assertions.assertThrows(AccountNotExistException.class, () -> {
			this.accountsService.getAccount("123");
		});
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

}
