package me.brandonjja;

public enum JawmOption {

    HELP("help", "h"),
    VERBOSE("verbose", "v"),
    WORDLIST("wordlist", "w"),
    UPPERCASE("uppercase", "u"),
    FORCE_UPPERCASE("forceuppercase", "U"),
    NUMBERS("numbers", "n"),
    MINIMUM_LENGTH("minlength", "l"),
    SPECIAL_CHARACTERS("special", "s"),
    OUTPUT_FILE("output", "o");

    private final String longOption;
    private final String shortOption;

    JawmOption(String longOption, String shortOption) {
        this.longOption = longOption;
        this.shortOption = shortOption;
    }

    public String getLongOption() {
        return longOption;
    }

    public String getShortOption() {
        return shortOption;
    }
}
