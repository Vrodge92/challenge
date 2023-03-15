package com.dws.challenge.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransferResult {
	
	private String accountFromId;

	private BigDecimal balanceAfterTransfer;

}
