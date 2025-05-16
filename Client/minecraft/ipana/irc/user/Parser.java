package ipana.irc.user;

public class Parser {
    private User user;

    public Parser(User user) {
        this.user = user;
    }

    public String parseProperties() {
        String[] strings = new String[UserProperties.VALUES.length];

        int i = 0;
        StringBuilder s = new StringBuilder();
        for (UserProperties property : UserProperties.VALUES) {
            strings[property.ordinal()] = property.name()+"="+user.getProperty(property);
            s.append("%s");
            i++;
            if (i < UserProperties.VALUES.length) {
                s.append(",");
            }
        }

        return String.format(s.toString(), (Object[]) strings);
    }
    public String parseProperty(UserProperties property) {
        return property.name()+"="+user.getProperty(property);
    }

    public String[] parseArgs(String raw) {
        String msg = raw.substring(1,raw.length()-1);
        return msg.split(",");
    }
}
