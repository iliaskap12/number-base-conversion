package converter;

public class Main {

    public static void main(String[] args) {
        final java.util.Scanner scanner = new java.util.Scanner(System.in);

        boolean didExit = false;
        while (!didExit) {
            try {
                System.out.print("Enter two numbers in format: {source base} {target base} (To quit type /exit) > ");
                String input = scanner.nextLine();
                Option option = Option.getOption(input);

                switch (option) {
                    case CONVERSION:
                        final int sourceBase = Integer.parseInt(input.split(" ")[0]);
                        final int targetBase = Integer.parseInt(input.split(" ")[1]);

                        do {
                            System.out.printf("Enter number in base %d to convert to base %d (To go back type /back) > ",
                                    sourceBase, targetBase);

                            input = scanner.nextLine();
                            option = Option.getOption(input);

                            switch (option) {
                                case NUMBER:
                                    NumberConverter numberConverter = new NumberConverter(input, sourceBase, targetBase);
                                    System.out.printf("Conversion result: %s%n%n", numberConverter.getConvertedNumber());
                                    break;
                                case BACK:
                                    System.out.println();
                                    break;
                                default:
                                    throw new IllegalArgumentException("Illegal menu option at this level.");
                            }
                        } while (option != Option.BACK);

                        break;
                    case EXIT:
                        didExit = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Illegal menu option at this level.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
