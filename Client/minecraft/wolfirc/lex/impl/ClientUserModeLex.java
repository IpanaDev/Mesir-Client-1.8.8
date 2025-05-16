package wolfirc.lex.impl;

import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUserModeLex extends Lex {

	public ClientUserModeLex() {
		super(Pattern.compile(":[^ ]+ 221 [^ :]+ (.+)"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		String userMode = matcher.group(1);
		//TODO:
	}
}
