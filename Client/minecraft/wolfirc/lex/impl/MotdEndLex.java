package wolfirc.lex.impl;

import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotdEndLex extends Lex {


    public MotdEndLex() {
        super(Pattern.compile("^:[^ ]+ 376 [^ ]+ .+$"));
    }

    @Override
    public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
        client.hasMotdEnd.set(true);
        client.onMotdEndEvent.fire();
    }
}
