package me.brandonjja;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.text.WordUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class WordMangler {

    private static final List<Character> SPECIAL_CHARACTERS = new ArrayList<>(Arrays.asList(
            '!', '?', '.', '@', '_', '-', '*', '$', '&', '%'
    ));

    private final Set<String> mangledWords = new HashSet<>();

    private Options options;
    private CommandLine commandLine;
    private int numbersToAdd = 0;
    private int minimumPasswordLength = 1;

    public WordMangler(String[] args) {
        parseArguments(args);

        if (commandLine.hasOption(JawmOption.VERBOSE.getLongOption())) {
            JawmLogger.verbose();
        }
    }

    public void mangle() {
        String fileName = commandLine.getOptionValue(JawmOption.WORDLIST.getLongOption());
        File wordlistFile = new File(fileName);
        if (!wordlistFile.exists() || !wordlistFile.canRead()) {
            JawmLogger.log(String.format("Failed to open file '%s'.", fileName));
            System.exit(-1);
        }

        // Read the initial wordlist and add all words to the mangledWord Set
        try (Scanner scanner = new Scanner(wordlistFile)) {
            boolean requireUpperCase = commandLine.hasOption(JawmOption.FORCE_UPPERCASE.getLongOption());
            boolean includeUpperCase = commandLine.hasOption(JawmOption.UPPERCASE.getLongOption());

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String lowercaseLine = line.toLowerCase();
                if (requireUpperCase) {
                    String capitalized = WordUtils.capitalize(lowercaseLine);
                    mangledWords.add(capitalized);
                    continue;
                }

                // Add the word as is, along with the lowercase version of the word if we don't require an uppercase character
                mangledWords.add(line);
                if (!line.equals(lowercaseLine)) {
                    mangledWords.add(lowercaseLine);
                }

                if (includeUpperCase) {
                    mangledWords.add(WordUtils.capitalize(lowercaseLine));
                }
            }
        } catch (FileNotFoundException ex) {
            JawmLogger.log(String.format("Failed to read file '%s'.", fileName));
            System.exit(-1);
        }

        String numbersToAdd = commandLine.getOptionValue(JawmOption.NUMBERS.getLongOption());
        if (numbersToAdd != null) {
            this.numbersToAdd = parseIntegerOption(numbersToAdd);
            appendNumbers();
        }

        if (commandLine.hasOption(JawmOption.SPECIAL_CHARACTERS.getLongOption())) {
            String specialCharacters = commandLine.getOptionValue(JawmOption.SPECIAL_CHARACTERS.getLongOption());
            appendSpecialCharacters(specialCharacters == null ? 1 : parseIntegerOption(specialCharacters));
        }

        String minimumPasswordLength = commandLine.getOptionValue(JawmOption.MINIMUM_LENGTH.getLongOption());
        if (minimumPasswordLength != null) {
            this.minimumPasswordLength = parseIntegerOption(minimumPasswordLength);
            cutShortPasswords();
        }

        JawmLogger.log(String.format("Generated %s unique passwords.", mangledWords.size()));

        String outputLocation = commandLine.getOptionValue(JawmOption.OUTPUT_FILE.getLongOption());
        if (outputLocation != null) {
            JawmLogger.log(String.format("Attempting to write generated passwords to %s.", outputLocation));
            saveToFile(outputLocation);
        } else {
            for (String word : mangledWords) {
                System.out.println(word);
            }
        }
    }

    private void appendNumbers() {
        JawmLogger.log(String.format("Adding %s numbers to each word.", numbersToAdd));

        Set<String> toAdd = new HashSet<>();
        for (String word : mangledWords) {
            int buffer = 0;
            int endBuffer = (int) (Math.pow(10, numbersToAdd - 1) - 1);
            while (buffer != endBuffer + 1) {
                for (int i = 0; i < 10; i++) {
                    if (numbersToAdd == 1) {
                        toAdd.add(word + i);
                        continue;
                    }

                    String bufferString = String.valueOf(buffer);
                    StringBuilder sb = new StringBuilder(word);
                    for (int j = 0; j < numbersToAdd - (bufferString.length() + 1); j++) {
                        sb.append("0");
                    }

                    sb.append(bufferString).append(i);
                    toAdd.add(sb.toString());
                }
                buffer++;
            }
        }

        mangledWords.clear();
        mangledWords.addAll(toAdd);
    }

    /**
     * Appends special characters to the end of each word in the current mangled wordlist.
     *
     * @param amount the number of special characters to append
     */
    private void appendSpecialCharacters(int amount) {
        JawmLogger.log(String.format("Adding %s special characters to each word.", amount));

        Set<String> toAdd = new HashSet<>(mangledWords);
        for (int i = 0; i < amount; i++) {
            for (String word : new ArrayList<>(toAdd)) {
                for (char specialCharacter : SPECIAL_CHARACTERS) {
                    toAdd.add(word + specialCharacter);
                }
            }
        }

        mangledWords.clear();
        mangledWords.addAll(toAdd);
    }

    /**
     * Removes passwords from the resulting wordlist that don't meet the minimum password length requirement if the
     * <b>-minlength</b> option was provided.
     */
    private void cutShortPasswords() {
        JawmLogger.log("Removing short passwords.");

        Set<String> toRemove = new HashSet<>();
        for (String word : mangledWords) {
            if (word.length() < minimumPasswordLength) {
                toRemove.add(word);
            }
        }

        mangledWords.removeAll(toRemove);
        JawmLogger.log(String.format("Removed %s short passwords.", toRemove.size()));
    }

    private void saveToFile(String fileLocation) {
        File file = new File(fileLocation);
        if (file.exists()) {
            JawmLogger.log(String.format("Failed to write to %s, file already exists.", fileLocation));
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String word : mangledWords) {
                bufferedWriter.write(word);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException ex) {
            JawmLogger.log(String.format("Failed saving passwords to %s: %s.", fileLocation, ex));
            return;
        }

        JawmLogger.log(String.format("Saved passwords to %s.", fileLocation));
    }

    /**
     * Attempts to parse the integer value of a provided option. If the value cannot be parsed the usage help will be
     * displayed and the program will exit with status code -1.
     *
     * @param option the value to parse
     * @return the integer value of the provided option
     */
    private int parseIntegerOption(String option) {
        try {
            return Integer.parseInt(option);
        } catch (NumberFormatException ex) {
            printHelp();
            System.exit(-1);
        }

        return -1;
    }

    private void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar JAWM.jar <options>", "Just Another Word Mangler\n\n", options, "\nExample Usage: java -jar JAWM.jar -w /usr/share/wordlists/rockyou.txt");
    }

    private void parseArguments(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt(JawmOption.HELP.getLongOption())
                .option(JawmOption.HELP.getShortOption())
                .hasArg()
                .build());

        options.addOption(Option.builder()
                .longOpt(JawmOption.VERBOSE.getLongOption())
                .option(JawmOption.VERBOSE.getShortOption())
                .desc("Print logging messages, useful for debugging issues.")
                .build());

        options.addOption(Option.builder()
                .argName("wordlist")
                .longOpt(JawmOption.WORDLIST.getLongOption())
                .option(JawmOption.WORDLIST.getShortOption())
                .desc("The wordlist to mangle.")
                .required()
                .hasArg()
                .build());

        options.addOption(Option.builder()
                .argName("uppercase")
                .longOpt(JawmOption.UPPERCASE.getLongOption())
                .option(JawmOption.UPPERCASE.getShortOption())
                .desc("Include an uppercase letter (does not require all passwords to have an uppercase letter).")
                .build());

        options.addOption(Option.builder()
                .longOpt(JawmOption.FORCE_UPPERCASE.getLongOption())
                .option(JawmOption.FORCE_UPPERCASE.getShortOption())
                .desc("Require an uppercase letter. Takes priority over --uppercase.")
                .build());

        options.addOption(Option.builder()
                .argName("amount")
                .longOpt(JawmOption.NUMBERS.getLongOption())
                .option(JawmOption.NUMBERS.getShortOption())
                .desc("Require a certain amount of numbers in each password.")
                .hasArg()
                .build());

        options.addOption(Option.builder()
                .argName("minLength")
                .longOpt(JawmOption.MINIMUM_LENGTH.getLongOption())
                .option(JawmOption.MINIMUM_LENGTH.getShortOption())
                .desc("Minimum length of the password.")
                .hasArg()
                .build());

        options.addOption(Option.builder()
                .argName("specialChars")
                .longOpt(JawmOption.SPECIAL_CHARACTERS.getLongOption())
                .option(JawmOption.SPECIAL_CHARACTERS.getShortOption())
                .desc("Required special characters to include (default 1).")
                .optionalArg(true)
                .build());

        options.addOption(Option.builder()
                .argName("location")
                .longOpt(JawmOption.OUTPUT_FILE.getLongOption())
                .option(JawmOption.OUTPUT_FILE.getShortOption())
                .desc("Output file name.")
                .hasArg()
                .build());

        this.options = options;

        CommandLineParser parser = new DefaultParser();

        try {
            this.commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            printHelp();
            System.exit(-1);
        }
    }
}
