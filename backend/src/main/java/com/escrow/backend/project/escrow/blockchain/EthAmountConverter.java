
package com.escrow.backend.project.escrow.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class EthAmountConverter {

    private static final int WEI_DECIMALS = 18;

    private EthAmountConverter() {
    }

    public static BigInteger toWei(BigDecimal ethAmount) {
        if (ethAmount == null || ethAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "ETH amount must be greater than zero"
            );
        }

        return ethAmount
                .setScale(WEI_DECIMALS, RoundingMode.UNNECESSARY)
                .movePointRight(WEI_DECIMALS)
                .toBigIntegerExact();
    }

    public static BigDecimal fromWei(BigInteger weiAmount) {
        if (weiAmount == null || weiAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Wei amount cannot be negative or null"
            );
        }

        return new BigDecimal(weiAmount)
                .movePointLeft(WEI_DECIMALS);
    }
}
