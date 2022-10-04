package Lexer;

import Config.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static TokenList lex(InputHandler inputHandler) {
        TokenList tokenList = new TokenList();
        while (!inputHandler.reachEnd()) {
            // skip blanks
            if (inputHandler.skipBlanks()) break;
            if (Config.debugMode) System.out.println(inputHandler.getCurrentLine());

            // skip comments
            if (inputHandler.skipComments()) continue;

            // traverse all patterns and make regex match
            for (TokenType tokenType : TokenType.values()) {
                Matcher matcher = Pattern.compile(tokenType.getPattern()).matcher(inputHandler.getCurrentLine());
                if (matcher.find()) {
                    tokenList.add(new Token(tokenType, matcher.group(0), inputHandler.getLineNumber()));
                    inputHandler.moveForward(matcher.group(0).length());
                    break;
                }
            }
        }
        return tokenList;
    }
}
