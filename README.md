# JAWM (Just Another Word Mangler)
JAWM is a wordlist mangler that can be used in targetted dictionary attacks. It it capable of adhering to password policies such as requring a certain amount of numbers, special characters, etc. It can be used alone to generate personalized wordlists that can be used later in password attacks or it can be piped to your favorite cracking tool.

# Usage
```java -jar JAWM.jar --wordlist <wordlist> [options]```
```
usage: java -jar JAWM.jar <options>
Just Another Word Mangler
-h,--help <arg>
 -l,--minlength <minLength>    Minimum length of the password.
 -n,--numbers <amount>         Require a certain amount of numbers in each
                               password.
 -o,--output <location>        Output file name.
 -s,--special <specialChars>   Required special characters to include
                               (default 1).
 -u,--uppercase                Include an uppercase letter (does not
                               require all passwords to have an uppercase
                               letter).
 -U,--forceuppercase           Require an uppercase letter. Takes priority
                               over --uppercase.
 -v,--verbose                  Print logging messages, useful for
                               debugging issues.
 -w,--wordlist <wordlist>      The wordlist to mangle.

Example Usage: java -jar JAWM.jar -w /usr/share/wordlists/rockyou.txt
```

# Example
Example Scenario: _Our target 'Summer Corp. XYZ' has a password policy requiring passwords to be at least 8 characters long containing 4 numbers, 1 special character, and may or may not include an uppercase letter. We have obtained two SHA1 hashed passwords the company has assigned us to recover._
<br><br>The example hashes in this example are as follows:
<br>**25888fd947f373b1230525847b88ad5bb0bd6841**
<br>**37804f97bd9984f61610a4d11b1d1ff312d8e15d**

<br>Wordlist contents:<br>
![image](https://github.com/Brandonjja/JAWM/assets/24996940/690face8-3b76-492d-9e5b-917c755da513)

<br>Recovering the plaintext passwords by piping our custom wordlist to hashcat:<br>
![image](https://github.com/Brandonjja/JAWM/assets/24996940/44c98123-a6e7-4707-bd63-dc9b40ab0d19)

