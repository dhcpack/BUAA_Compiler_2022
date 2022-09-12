import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static TokenList parse(InputHandler inputHandler) {
        TokenList tokenList = new TokenList();
        while (!inputHandler.reachEnd()) {
            // skip blanks
            if (inputHandler.skipBlanks()) break;
            if (Config.debugMode) System.out.println(inputHandler.getCurrentLine());

            // skip comments
            if (inputHandler.getForwardWord(2).equals("//")) {
                inputHandler.moveNextLine();
                continue;
            }
            if (inputHandler.getForwardWord(2).equals("/*")) {
                inputHandler.moveForward(2);
                while (!inputHandler.getForwardWord(2).equals("*/")) {
                    if (inputHandler.moveForward(1)) break;
                }
                inputHandler.moveForward(2);
                continue;
            }

            // parse
            for (Type type : Type.values()) {
                Matcher matcher = Pattern.compile(type.getPattern()).matcher(inputHandler.getCurrentLine());
                if (matcher.find()) {
                    tokenList.add(new Token(type, matcher.group(0), inputHandler.getLine()));
                    inputHandler.moveForward(matcher.group(0).length());
                    break;
                }
            }
        }
        return tokenList;
    }
}
