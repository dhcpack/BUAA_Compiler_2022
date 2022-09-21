package Lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Config.Config;

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
            for (Type type : Type.values()) {
                Matcher matcher = Pattern.compile(type.getPattern()).matcher(inputHandler.getCurrentLine());
                if (matcher.find()) {
                    tokenList.add(new Token(type, matcher.group(0), inputHandler.getLineNumber()));
                    inputHandler.moveForward(matcher.group(0).length());
                    break;
                }
            }
        }
        return tokenList;
    }
}
