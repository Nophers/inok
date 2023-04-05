import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * A lexer that tokenizes a string of code into a sequence of tokens.
 */
public class Lexer {

    /**
     * Tokenizes the given input string into a sequence of tokens.
     *
     * @param input the input string to tokenize
     * @return a list of tokens representing the input string
     * @throws Exception if an unknown token is encountered in the input string
     */
    public static List<Token> tokenize(String input) throws Exception {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = Regex.TOKEN_PATTERN.matcher(input);
        while (matcher.find()) {
            String tokenValue = matcher.group(1);
            TokenType tokenType = TOKEN_TYPES.getOrDefault(tokenValue, determineTokenType(tokenValue));
            if (tokenType == null) {
                throw new Exception("Unknown token: " + tokenValue);
            }
            tokens.add(new Token(tokenType, tokenValue));
        }
        return tokens;
    }

    /**
     * Determines the token type for the given token value.
     *
     * @param tokenValue the value of the token to determine the type for
     * @return the type of the token
     */
    private static TokenType determineTokenType(String tokenValue) {
        return Regex.LEXER_VARIABLE_REFERENCE_PATTERN.matcher(tokenValue).matches()
                ? TokenType.VARIABLE_REFERENCE
                : Regex.LEXER_IDENTIFIER_PATTERN.matcher(tokenValue).matches()
                        ? TokenType.IDENTIFIER
                        : Regex.LEXER_STRING_LITERAL_PATTERN.matcher(tokenValue).matches()
                                ? TokenType.STRING_LITERAL
                                : null;
    }

    /**
     * A map of token values to their corresponding token types.
     */
    private static final Map<String, TokenType> TOKEN_TYPES = Map.of(
            "let", TokenType.LET,
            "fu", TokenType.FUNCTION,
            "main", TokenType.MAIN,
            "display", TokenType.DISPLAY,
            "{", TokenType.OPEN_BRACE,
            "}", TokenType.CLOSE_BRACE,
            "(", TokenType.OPEN_PAREN,
            ")", TokenType.CLOSE_PAREN,
            ";", TokenType.SEMICOLON);

}
