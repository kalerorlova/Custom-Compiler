package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("LET")){
            parseDeclarationStatement();
        }
        else if (match("IF")) {
            parseIfStatement();
        }
        else if (match("FOR")) {
            parseForStatement();
        }
        else if (match("WHILE")) {
            parseWhileStatement();
        }
        else if (match("RETURN")) {
            parseReturnStatement();
        }
        //I don't think we need to check if it's an invalid secondary expression because it goes into
        //primary expression and catches if something is amiss.
        //Is there a way to delete the duplicate code for missing semicolon for statement?
        else {
            Ast.Expr expr = parseExpression();
            if (match("=")) {
                Ast.Expr val = parseExpression();
                if (!match(";")) {
                    if (tokens.has(0)) {
                        throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
                    }
                    //the token is the final token
                    else {
                        throw new ParseException("Missing semicolon", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    }

                }
                return new Ast.Stmt.Assignment(expr, val);
            }
            else if (!match(";")) {
                if (tokens.has(0)) {
                    throw new ParseException("Missing semicolon", tokens.get(0).getIndex());
                }
                //the token is the final token
                else {
                    throw new ParseException("Missing semicolon", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
            }
            return new Ast.Stmt.Expression(expr);
        }
        return null;
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        //for all expressions except secondary & primary, go through the code and add tokens one by one
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO - DONE
        Ast.Expr left = parseEqualityExpression();
        while (peek("AND") || peek("OR")) {
            String operator = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);
            Ast.Expr right = parseEqualityExpression();
            if (!(peek("AND") || peek("OR"))) {
                return new Ast.Expr.Binary(operator, left, right);
            }
            else {
                left = new Ast.Expr.Binary(operator, left, right);
            }
        }
        return left;
    }

    /**
     * Parses the {@code equality-expression} rule.
     * THIS IS THE COMPARISON EXPRESSION
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO - DONE
        Ast.Expr left = parseAdditiveExpression();
        while (peek("<") || peek("<=") || peek(">") || peek("=>") ||
                peek("==") || peek("!=")) {
            String operator = tokens.get(0).getLiteral();
            match(Token.Type.OPERATOR);
            Ast.Expr right = parseAdditiveExpression();
            if (!(peek("<") || peek("<=") || peek(">") || peek("=>") ||
                    peek("==") || peek("!="))) {
                return new Ast.Expr.Binary(operator, left, right);
            }
            else {
                left = new Ast.Expr.Binary(operator, left, right);
            }
        }
        return left;

    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseMultiplicativeExpression();
        while (peek("+") || peek("-")) {
            String operator = tokens.get(0).getLiteral();
            match(Token.Type.OPERATOR);
            Ast.Expr right = parseMultiplicativeExpression();
            if (!(peek("+") || peek("-"))) {
                return new Ast.Expr.Binary(operator, left, right);
            }
            else {
                left = new Ast.Expr.Binary(operator, left, right);
            }
        }
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parseSecondaryExpression();
        while (peek("*") || peek("/")) {
            String operator = tokens.get(0).getLiteral();
            match(Token.Type.OPERATOR);
            Ast.Expr right = parseSecondaryExpression();
            if (!(peek("*") || peek("/"))) {
                return new Ast.Expr.Binary(operator, left, right);
            }
            else {
                left = new Ast.Expr.Binary(operator, left, right);
            }
        }
        return left;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        Ast.Expr left = parsePrimaryExpression();
        String name = "";
        //note that this has the same structure as the last else section in the parsePrimaryExpression function
        while (match(".")) {
            if (match(Token.Type.IDENTIFIER)) {
                name = tokens.get(-1).getLiteral();
            }
            else if (tokens.has(0)) {
                throw new ParseException("No identifier", tokens.get(0).getIndex());
            }
            //the token is the final token and there is no paren
            else {
                throw new ParseException("No identifier", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
            //matched on identifier, check if it's a function or access
            if (match("(")) {
                //create an empty argument list for the function
                List<Ast.Expr> args = new ArrayList<Ast.Expr>();
                if (!match(")")) {
                    args.add(parseExpression());
                    while (match(",")) {
                        if (match(")")) {
                            throw new ParseException("Trailing comma",tokens.get(0).getIndex());
                        }
                        args.add(parseExpression());
                    }
                    if (!match(")")) {
                        if (tokens.has(0)) {
                            throw new ParseException("No closing paren", tokens.get(0).getIndex());
                        }
                        //the token is the final token and there is no paren
                        else {
                            throw new ParseException("No closing paren", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                    }
                }
                if (!peek(".")) {
                    return new Ast.Expr.Function(Optional.of(left), name, args);
                }
                left = new Ast.Expr.Function(Optional.of(left), name, args);
            }
            //access
            if (!peek(".")) {
                return new Ast.Expr.Access(Optional.of(left), name);
            }
            left = new Ast.Expr.Access(Optional.of(left), name);
        }
        //return either complex or simple function/access ast expr
        return left;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO
        if (match("NIL")) {
            return new Ast.Expr.Literal(null);
        }
        else if (match("TRUE")) {
            return new Ast.Expr.Literal(true);
        }
        else if (match("FALSE")) {
            return new Ast.Expr.Literal(false);
        }
        else if (match(Token.Type.INTEGER)) {
            return new Ast.Expr.Literal(new BigInteger(tokens.get(-1).getLiteral()));
        }
        else if (match(Token.Type.DECIMAL)) {
            return new Ast.Expr.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
        }
        else if (match(Token.Type.CHARACTER)) {
            String escapeChar = tokens.get(-1).getLiteral();
            if (escapeChar.length() > 3) {
                escapeChar = escapeChar.replace("\\\\", "\\");
                escapeChar = escapeChar.replace("\\\"", "\"");
                escapeChar = escapeChar.replace("\\\'", "\'");
                escapeChar = escapeChar.replace("\\b", "\b");
                escapeChar = escapeChar.replace("\\n", "\n");
                escapeChar = escapeChar.replace("\\r", "\r");
                escapeChar = escapeChar.replace("\\t", "\t");
            }
            //is this correct for \n characters?
            return new Ast.Expr.Literal(escapeChar.charAt(1));
        }
        else if (match(Token.Type.STRING)) {
            String str = tokens.get(-1).getLiteral();
            str = str.replace("\\\\", "\\");
            str = str.replace("\\\"", "\"");
            str = str.replace("\\\'", "\'");
            str = str.replace("\\b", "\b");
            str = str.replace("\\n", "\n");
            str = str.replace("\\r", "\r");
            str = str.replace("\\t", "\t");
            return new Ast.Expr.Literal(str.substring(1, str.length()-1));
        }
        else if (match("(")) {
            //System.out.println("the index before matching on group is " + Integer.toString(tokens.get(0).getIndex()));
            Ast.Expr.Group group = new Ast.Expr.Group(parseExpression());
            //here I attempted to implement the error-checking functionality
            //System.out.println("the index before matching on paren is " + Integer.toString(tokens.get(0).getIndex()));
            if (!match(")")) {
                //something else instead of paren
                if (tokens.has(0)) {
                    throw new ParseException("No closing paren", tokens.get(0).getIndex());
                }
                //the token is the final token and there is no paren
                else {
                    throw new ParseException("No closing paren", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                }
            };
            return group;
        }
        else if (match(Token.Type.IDENTIFIER)) {
            String name = tokens.get(-1).getLiteral();
            if (match("(")) {
                //create an empty argument list for the function
                List<Ast.Expr> args = new ArrayList<Ast.Expr>();
                if (!match(")")) {
                    args.add(parseExpression());
                    while (match(",")) {
                        if (match(")")) {
                            throw new ParseException("Trailing comma",tokens.get(-1).getIndex());
                        }
                        args.add(parseExpression());
                    }
                    if (!match(")")) {
                        if (tokens.has(0)) {
                            throw new ParseException("No closing paren", tokens.get(0).getIndex());
                        }
                        //the token is the final token and there is no paren
                        else {
                            throw new ParseException("No closing paren", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                        }
                    }
                }
                return new Ast.Expr.Function(Optional.empty(), name, args);
            }
            return new Ast.Expr.Access(Optional.empty(), name);
        }
        else if (tokens.has(0)) {
            throw new ParseException("Invalid primary expression.", tokens.get(0).getIndex());
        }
        //it was a final token
        else {
            throw new ParseException("Invalid primary expression.", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture) - DONE
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            }
            else if (patterns[i] instanceof Token.Type){
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            }
            else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            }
            else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        //throw new UnsupportedOperationException(); //TODO (in lecture) - DONE
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
    }
    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}