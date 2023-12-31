package plc.project;

import java.util.*;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        List<Token> tokenList = new ArrayList<Token>();
        while (chars.has(0) ) {
            if (!match(" ")) {
                if (!match("[\b\n\r\t]")) {
                    tokenList.add(lexToken());
                }
            }
            chars.skip();
        }
        return tokenList;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        if (peek("[A-Za-z_]")) {
            return lexIdentifier();
        }
        else if (peek("[+-]", "\\d") || peek( "\\d")) { //match doesn't read complex regex
            return lexNumber();
        }
        else if (peek("\'")) {
            return lexCharacter();
        }
        else if (peek("\"")) {
            return lexString();
        }
        else {
            return lexOperator();
        }
    }

    //These functions examine chars one at a time
    public Token lexIdentifier() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        while (match("[A-Za-z0-9_-]")) {
            continue;
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        match("[\\+-]");
        while (match("\\d")) {
            continue;
        }
        if (!match("\\.", "\\d")) {     //if no period and digits after the last digit, it's an int
            return chars.emit(Token.Type.INTEGER);
        }
        while (match("\\d")) {  //add the other digits
            continue;
        }
        return chars.emit(Token.Type.DECIMAL);
    }

    public Token lexCharacter() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        match("\'");
        if (match("\'")) {
            if (match("\'")) {
                throw new ParseException("Single Quote Characters are Illegal", chars.index);
            }
            throw new ParseException("Missing Character", chars.index);
        }
        if (peek("[^\\n\\r]")) {
            if (peek("\\\\")) {    //if explicitly written escape seq
                lexEscape();
            }
            else {
                match(".");
            }
        }
        if (!match("\'")) {
            match(".");
            //System.out.println("Incorrect char index is: " + chars.index);
            throw new ParseException("Unterminated or Multiple-Line Character", chars.index);
        }
        return chars.emit(Token.Type.CHARACTER);
    }

    public Token lexString() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        match("\"");
        if (match("\"\"")) {
            throw new ParseException("Double Quote in the String", chars.index);
        }
        while (peek("[^\"\\n\\r]")) {
            if (peek("\\\\")) {
                lexEscape();
            }
            else {
                match(".");
            }
        }
        if (!match("\"")) {
            match(".");
            throw new ParseException("Unterminated", chars.index);
        }
        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        if (!match("\\\\", "[bnrt\\\\]")){
            match(".");
            //System.out.println("Incorrect char index is: " + chars.index + " " + chars.get(0));
            throw new ParseException("Invalid Escape Sequence", chars.index);
        }
    }

    public Token lexOperator() {
        //throw new UnsupportedOperationException(); //TODO - DONE
        boolean operator = match("[<>!=]", "=") ? true : match("."); //execute only one match
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture) - DONE
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture) - DONE
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
