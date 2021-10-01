package converter;

import java.util.HashMap;
import java.util.regex.Pattern;

public enum Option {
    EXIT(java.util.regex.Pattern.compile("/exit")),
    BACK(java.util.regex.Pattern.compile("/back")),
    CONVERSION(java.util.regex.Pattern.compile("([2-9]|[1-2]\\d|3[0-6]) ([2-9]|[1-2]\\d|3[0-6])")),
    NUMBER(java.util.regex.Pattern.compile("[\\dA-Za-z]+(\\.[\\dA-Za-z]+)?")),
    ILLEGAL(java.util.regex.Pattern.compile("Illegal option"));

    private final Pattern optionPattern;
    private static final HashMap<Pattern, Option> patternToOptionName = new HashMap<>();

    static {
        for (Option option : Option.values()) {
            patternToOptionName.put(option.getOptionPattern(), option);
        }
    }

    Option(Pattern optionPattern) {
        this.optionPattern = optionPattern;
    }

    public Pattern getOptionPattern() {
        return optionPattern;
    }

    public static Option getOption(String optionValue) {
        for (Pattern pattern : patternToOptionName.keySet()) {
            if (pattern.matcher(optionValue).matches()) {
                return patternToOptionName.get(pattern);
            }
        }
        return ILLEGAL;
    }
}
