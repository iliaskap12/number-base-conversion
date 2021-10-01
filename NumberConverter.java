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
            return String.valueOf(this.number);
        }

        final int BASE10 = 10;
        final String numberBase10 = this.sourceBase != BASE10 ? convertToBase10() : String.valueOf(this.number);

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

        final StringBuilder numberBuilder = new StringBuilder();
        String decimalPart = null;
        boolean carryover = false;
        final boolean isDecimal = numberPartsBase10.length == 2;
        if (isDecimal) {
            carryover = convertDecimalPartFromBase10(numberPartsBase10[1]);

            numberBuilder.append(".");
            for (Character digit : convertedNumberBuilder) {
                numberBuilder.append(digit);
            }

            decimalPart = numberBuilder.toString();
            numberBuilder.delete(0, numberBuilder.length());
        }

        if (carryover) {
            BigInteger integerPartBase10 = new BigInteger(numberPartsBase10[0]).add(BigInteger.ONE);
            numberPartsBase10[0] = integerPartBase10.toString();
        }

        convertIntegerPartFromBase10(numberPartsBase10[0]);

        for (Character character : convertedNumberBuilder) {
            numberBuilder.append(character);
        }

        if (isDecimal) {
            numberBuilder.append(decimalPart);
        }

        return numberBuilder.toString();
    }

    private boolean convertDecimalPartFromBase10(String decimalPartBase10) {
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
        final int UPPER_SCALE_LIMIT = 6;
        while (!isFinished && convertedNumberBuilder.size() < UPPER_SCALE_LIMIT) {
            BigDecimal subtraction = integerRemainder.subtract(fractionalPart);
            if (subtraction.compareTo(BigDecimal.ZERO) == 0) {
                isFinished = true;
            }
            if (!(fractionalPart.compareTo(BigDecimal.ONE) <= 0)) {
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

        if (convertedNumberBuilder.size() == UPPER_SCALE_LIMIT) {
            int index = convertedNumberBuilder.size() - 1;
            int indexDigit = Character.digit(convertedNumberBuilder.get(index), this.targetBase);
            while (indexDigit + 1 >= this.targetBase) {
                convertedNumberBuilder.set(index, '0');
                --index;
                if (index < 0) {
                    convertedNumberBuilder.removeLast();
                    return true;
                }
                indexDigit = Character.digit(convertedNumberBuilder.get(index), this.targetBase);
            }

            convertedNumberBuilder.removeLast();
            if (index == convertedNumberBuilder.size()) {
                --index;
            }
            if (index != convertedNumberBuilder.size() - 1 || indexDigit >= this.targetBase / 2) {
                convertedNumberBuilder.set(index,
                        Character.toUpperCase(Character.forDigit(++indexDigit, this.targetBase))
                );
            }
        }

        while (convertedNumberBuilder.size() < UPPER_SCALE_LIMIT - 1) {
            convertedNumberBuilder.add('0');
        }

        return false;
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
