package converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Objects;

public class NumberConverter {
    private final int sourceBase;
    private final int targetBase;
    private final String number;
    private final String convertedNumber;
    private final LinkedList<Character> convertedNumberBuilder = new LinkedList<>();

    public NumberConverter(String number, int sourceBase, int targetBase) {
        this.number = number;
        this.sourceBase = sourceBase;
        this.targetBase = targetBase;
        this.convertedNumber = convertNumber();
    }

    private String convertNumber() {
        if (this.sourceBase == this.targetBase) {
            return this.number;
        }

        final int BASE_10 = 10;
        final String numberBase10 = this.sourceBase != BASE_10 ? convertToBase10() : this.number;

        return convertFromBase10(numberBase10);
    }

    private String convertToBase10() {
        final String[] numberParts = this.number.split("\\.");
        final String integerPart = numberParts[0];

        final String integerPartBase10 = convertIntegerPartToBase10(integerPart);

        if (numberParts.length == 2) {
            final String decimalPart = numberParts[1];
            final String decimalPartBase10 = convertDecimalPartToBase10(decimalPart);
            return String.format("%s.%s", integerPartBase10, decimalPartBase10);
        }

        return integerPartBase10;
    }

    private String convertDecimalPartToBase10(String decimalPart) {
        final char[] digits = decimalPart.toCharArray();
        this.convertedNumberBuilder.clear();
        for (char character : digits) {
            this.convertedNumberBuilder.add(character);
        }

        final BigDecimal bigSourceBase = new BigDecimal(String.valueOf(this.sourceBase));
        BigDecimal number = BigDecimal.ZERO;
        int counter = 1;

        for (Character character : this.convertedNumberBuilder) {
            final BigDecimal digit = new BigDecimal(String.valueOf(Character.digit(character, this.sourceBase)));
            if (!digit.equals(BigDecimal.ZERO)) {
                final BigDecimal power = bigSourceBase.pow(counter);
                final BigDecimal division = digit.divide(power, 15, RoundingMode.HALF_EVEN);
                number = number.add(division);
            }
            ++counter;
        }
        return number.equals(BigDecimal.ZERO) ? "0" : number.toString().substring(2);
    }

    private String convertIntegerPartToBase10(String integerPart) {
        final char[] digits = integerPart.toCharArray();
        for (char character : digits) {
            this.convertedNumberBuilder.addFirst(character);
        }

        final BigInteger bigSourceBase = new BigInteger(String.valueOf(this.sourceBase));
        BigInteger number = BigInteger.ZERO;
        int counter = 0;

        for (Character character : this.convertedNumberBuilder) {
            final BigInteger digit = new BigInteger(String.valueOf(Character.digit(character, this.sourceBase)));
            number = number.add(digit.multiply(bigSourceBase.pow(counter)));
            ++counter;
        }
        return number.toString();
    }

    private String convertFromBase10(String numberBase10) {
        final String[] numberPartsBase10 = numberBase10.split("\\.");
        final boolean isDecimal = numberPartsBase10.length == 2;

        String integerPart = numberPartsBase10[0];
        String decimalPart = isDecimal ? numberPartsBase10[1] : null;

        final StringBuilder numberBuilder = new StringBuilder();
        if (isDecimal) {
            convertDecimalPartFromBase10(decimalPart);
            integerPart = roundNumber(integerPart);

            numberBuilder.append(".");
            for (Character digit : convertedNumberBuilder) {
                numberBuilder.append(digit);
            }

            decimalPart = numberBuilder.toString();
            numberBuilder.delete(0, numberBuilder.length());
        }

        convertIntegerPartFromBase10(integerPart);

        for (Character character : convertedNumberBuilder) {
            numberBuilder.append(character);
        }

        if (isDecimal) {
            numberBuilder.append(decimalPart);
        }

        return numberBuilder.toString();
    }

    private void convertDecimalPartFromBase10(String decimalPartBase10) {
        convertedNumberBuilder.clear();

        final char[] decimalDigits = decimalPartBase10.toCharArray();
        int i = decimalDigits.length - 1;
        while (decimalDigits[i] == '0' && i != 0) {
            --i;
        }

        final StringBuilder decimalBuilder = new StringBuilder("0.");
        for (int j = 0; j <= i; ++j) {
            decimalBuilder.append(decimalDigits[j]);
        }

        BigDecimal fractionalPart = new BigDecimal(decimalBuilder.toString());
        fractionalPart = fractionalPart.multiply(BigDecimal.valueOf(this.targetBase));
        BigDecimal integerRemainder = fractionalPart.setScale(0, RoundingMode.FLOOR);
        boolean isFinished = false;
        final int UPPER_SCALE_LIMIT = 5;

        while (!isFinished && convertedNumberBuilder.size() < UPPER_SCALE_LIMIT + 1) {
            BigDecimal subtraction = integerRemainder.subtract(fractionalPart);
            final boolean isDecimalPartZero = subtraction.compareTo(BigDecimal.ZERO) == 0;
            if (isDecimalPartZero) {
                isFinished = true;
            }

            final boolean isFractionalPartGreaterEqualsToOne = !(fractionalPart.compareTo(BigDecimal.ONE) < 0);
            if (isFractionalPartGreaterEqualsToOne) {
                fractionalPart = fractionalPart.subtract(integerRemainder);
            }

            convertedNumberBuilder.add(
                    Character.toUpperCase(
                            Character.forDigit(Integer.parseInt(integerRemainder.toString()), this.targetBase)
                    )
            );

            fractionalPart = fractionalPart.multiply(BigDecimal.valueOf(this.targetBase));
            integerRemainder = fractionalPart.setScale(0, RoundingMode.FLOOR);
        }

        while (convertedNumberBuilder.size() < UPPER_SCALE_LIMIT) {
            convertedNumberBuilder.add('0');
        }
    }

    private String roundNumber(String integerPart) {
        final int UPPER_SCALE_LIMIT = 6;
        boolean carryover = false;
        if (convertedNumberBuilder.size() == UPPER_SCALE_LIMIT) {
            int index = convertedNumberBuilder.size() - 1;
            int indexDigit = Character.digit(convertedNumberBuilder.get(index), this.targetBase);
            while (indexDigit + 1 >= this.targetBase) {
                convertedNumberBuilder.set(index, '0');
                --index;
                if (index >= 0) {
                    indexDigit = Character.digit(convertedNumberBuilder.get(index), this.targetBase);
                } else {
                    carryover = true;
                }
            }

            convertedNumberBuilder.removeLast();
            if (index == convertedNumberBuilder.size()) {
                --index;
            }
            if (indexDigit >= this.targetBase / 2) {
                convertedNumberBuilder.set(index,
                        Character.toUpperCase(Character.forDigit(++indexDigit, this.targetBase))
                );
            }
        }

        if (carryover) {
            return new BigInteger(integerPart).add(BigInteger.ONE).toString();
        }

        return integerPart;
    }

    private void convertIntegerPartFromBase10(String integerPartBase10) {
        convertedNumberBuilder.clear();

        BigInteger quotient = new BigInteger(integerPartBase10);
        BigInteger remainder = quotient.remainder(BigInteger.valueOf(this.targetBase));
        boolean isFinished = false;
        while (!isFinished) {
            if (Objects.equals(quotient, remainder)) {
                isFinished = true;
            }
            convertedNumberBuilder.addFirst(
                    Character.toUpperCase(
                            Character.forDigit(Integer.parseInt(remainder.toString()), this.targetBase)
                    )
            );

            quotient = quotient.divide(BigInteger.valueOf(this.targetBase));
            remainder = quotient.remainder(BigInteger.valueOf(this.targetBase));
        }
    }

    public String getConvertedNumber() {
        return convertedNumber;
    }
}
