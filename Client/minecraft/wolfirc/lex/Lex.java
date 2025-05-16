package wolfirc.lex;

import wolfirc.WolfIRC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Lex {

    protected Pattern regex;
    public Lex(Pattern _regex) {
        regex = _regex;
    }

    public abstract void onDataReceive(final String data, Matcher matcher, WolfIRC client);

    public Pattern regex() {
        return regex;
    }
}
