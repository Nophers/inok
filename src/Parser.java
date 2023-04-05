import java.util.List;

/**
 * A parser that parses a sequence of tokens into an abstract syntax tree (AST).
 */
public class Parser {

    /**
     * Parses the given list of tokens into an AST.
     *
     * @param tokens the list of tokens to parse
     * @return the root node of the resulting AST
     * @throws Exception if a syntax error is encountered during parsing
     */
    public static AstNode parse(List<Token> tokens) throws Exception {
        return parseProgram(tokens);
    }

    /**
     * Parses a program node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the program node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseProgram(List<Token> tokens) throws Exception {
        AstNode programNode = new AstNode(AstNodeType.PROGRAM);
        while (!tokens.isEmpty()) {
            AstNode statementNode = parseStatement(tokens);
            programNode.addChild(statementNode);
        }
        return programNode;
    }

    /**
     * Parses a statement node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the statement node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseStatement(List<Token> tokens) throws Exception {
        Token token = tokens.remove(0);
        switch (token.getType()) {
            case LET:
                return parseVariableDeclaration(tokens);
            case FUNCTION:
                return parseFunctionDeclaration(tokens);
            case DISPLAY:
                return parseDisplayStatement(tokens);
            default:
                throw new Exception("Unexpected token: " + token.getValue());
        }
    }

    /**
     * Parses a variable declaration node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the variable declaration node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseVariableDeclaration(List<Token> tokens) throws Exception {
        AstNode variableDeclarationNode = new AstNode(AstNodeType.VARIABLE_DECLARATION);
        Token identifierToken = tokens.remove(0);
        if (identifierToken.getType() != TokenType.IDENTIFIER) {
            throw new Exception("Expected identifier, but found: " + identifierToken.getValue());
        }
        variableDeclarationNode.setValue(identifierToken.getValue());
        Token equalsToken = tokens.remove(0);
        if (equalsToken.getType() != TokenType.EQUALS) {
            throw new Exception("Expected equals sign, but found: " + equalsToken.getValue());
        }
        AstNode expressionNode = parseExpression(tokens);
        variableDeclarationNode.addChild(expressionNode);
        Token semicolonToken = tokens.remove(0);
        if (semicolonToken.getType() != TokenType.SEMICOLON) {
            throw new Exception("Expected semicolon, but found: " + semicolonToken.getValue());
        }
        return variableDeclarationNode;
    }

    /**
     * Parses a function declaration node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the function declaration node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseFunctionDeclaration(List<Token> tokens) throws Exception {
        AstNode functionDeclarationNode = new AstNode(AstNodeType.FUNCTION_DECLARATION);
        Token identifierToken = tokens.remove(0);
        if (identifierToken.getType() != TokenType.IDENTIFIER) {
            throw new Exception("Expected identifier, but found: " + identifierToken.getValue());
        }
        functionDeclarationNode.setValue(identifierToken.getValue());
        Token openParenToken = tokens.remove(0);
        if (openParenToken.getType() != TokenType.OPEN_PAREN) {
            throw new Exception("Expected open parenthesis, but found: " + openParenToken.getValue());
        }
        Token closeParenToken = tokens.remove(0);
        if (closeParenToken.getType() != TokenType.CLOSE_PAREN) {
            throw new Exception("Expected close parenthesis, but found: " + closeParenToken.getValue());
        }
        Token openBraceToken = tokens.remove(0);
        if (openBraceToken.getType() != TokenType.OPEN_BRACE) {
            throw new Exception("Expected open brace, but found: " + openBraceToken.getValue());
        }
        AstNode bodyNode = parseBlock(tokens);
        functionDeclarationNode.addChild(bodyNode);
        Token closeBraceToken = tokens.remove(0);
        if (closeBraceToken.getType() != TokenType.CLOSE_BRACE) {
            throw new Exception("Expected close brace, but found: " + closeBraceToken.getValue());
        }
        return functionDeclarationNode;
    }

    /**
     * Parses a display statement node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the display statement node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseDisplayStatement(List<Token> tokens) throws Exception {
        AstNode displayStatementNode = new AstNode(AstNodeType.DISPLAY_STATEMENT);
        Token openParenToken = tokens.remove(0);
        if (openParenToken.getType() != TokenType.OPEN_PAREN) {
            throw new Exception("Expected open parenthesis, but found: " + openParenToken.getValue());
        }
        AstNode expressionNode = parseExpression(tokens);
        displayStatementNode.addChild(expressionNode);
        Token closeParenToken = tokens.remove(0);
        if (closeParenToken.getType() != TokenType.CLOSE_PAREN) {
            throw new Exception("Expected close parenthesis, but found: " + closeParenToken.getValue());
        }
        Token semicolonToken = tokens.remove(0);
        if (semicolonToken.getType() != TokenType.SEMICOLON) {
            throw new Exception("Expected semicolon, but found: " + semicolonToken.getValue());
        }
        return displayStatementNode;
    }

    /**
     * Parses an expression node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the expression node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseExpression(List<Token> tokens) throws Exception {
        AstNode expressionNode = parseTerm(tokens);
        while (!tokens.isEmpty() && isAdditiveOperator(tokens.get(0))) {
            Token operatorToken = tokens.remove(0);
            AstNode termNode = parseTerm(tokens);
            AstNode binaryExpressionNode = new AstNode(AstNodeType.BINARY_EXPRESSION);
            binaryExpressionNode.setValue(operatorToken.getValue());
            binaryExpressionNode.addChild(expressionNode);
            binaryExpressionNode.addChild(termNode);
            expressionNode = binaryExpressionNode;
        }
        return expressionNode;
    }

    /**
     * Parses a term node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the term node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseTerm(List<Token> tokens) throws Exception {
        AstNode termNode = parseFactor(tokens);
        while (!tokens.isEmpty() && isMultiplicativeOperator(tokens.get(0))) {
            Token operatorToken = tokens.remove(0);
            AstNode factorNode = parseFactor(tokens);
            AstNode binaryExpressionNode = new AstNode(AstNodeType.BINARY_EXPRESSION);
            binaryExpressionNode.setValue(operatorToken.getValue());
            binaryExpressionNode.addChild(termNode);
            binaryExpressionNode.addChild(factorNode);
            termNode = binaryExpressionNode;
        }
        return termNode;
    }

    /**
     * Parses a factor node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the factor node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseFactor(List<Token> tokens) throws Exception {
        Token token = tokens.remove(0);
        switch (token.getType()) {
            case IDENTIFIER:
                return new AstNode(null);
            case STRING_LITERAL:
                return new AstNode(null);
            case INTEGER_LITERAL:
                return new AstNode(null);
            case OPEN_PAREN:
                AstNode expressionNode = parseExpression(tokens);
                Token closeParenToken = tokens.remove(0);
                if (closeParenToken.getType() != TokenType.CLOSE_PAREN) {
                    throw new Exception("Expected close parenthesis, but found: " + closeParenToken.getValue());
                }
                return expressionNode;
            default:
                throw new Exception("Unexpected token: " + token.getValue());
        }
    }

    /**
     * Parses a block node from the given list of tokens.
     *
     * @param tokens the list of tokens to parse
     * @return the block node
     * @throws Exception if a syntax error is encountered during parsing
     */
    private static AstNode parseBlock(List<Token> tokens) throws Exception {
        AstNode blockNode = new AstNode(null);
        while (!tokens.isEmpty() && tokens.get(0).getType() != TokenType.CLOSE_BRACE) {
            AstNode statementNode = parseStatement(tokens);
            blockNode.addChild(statementNode);
        }
        return blockNode;
    }

    /**
     * Returns true if the given token is an additive operator.
     *
     * @param token the token to check
     * @return true if the token is an additive operator, false otherwise
     */
    private static boolean isAdditiveOperator(Token token) {
        TokenType type = token.getType();
        return type == TokenType.PLUS || type == TokenType.MINUS;
    }

    /**
     * Returns true if the given token is a multiplicative operator.
     *
     * @param token the token to check
     * @return true if the token is a multiplicative operator, false otherwise
     */
    private static boolean isMultiplicativeOperator(Token token) {
        TokenType type = token.getType();
        return type == TokenType.TIMES || type == TokenType.DIVIDE;
    }
}