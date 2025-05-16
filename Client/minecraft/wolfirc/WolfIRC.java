package wolfirc;

import pisi.unitedmeows.yystal.clazz.event;
import pisi.unitedmeows.yystal.clazz.prop;
import pisi.unitedmeows.yystal.networking.IPAddress;
import pisi.unitedmeows.yystal.networking.client.YTcpClient;
import pisi.unitedmeows.yystal.networking.client.extension.impl.CTcpLineRead;
import pisi.unitedmeows.yystal.networking.events.CDataReceivedEvent;
import pisi.unitedmeows.yystal.utils.kThread;
import wolfirc.events.*;
import wolfirc.lex.Lex;
import wolfirc.lex.impl.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;

public class WolfIRC {

	protected YTcpClient client;
	protected String host;
	protected int port;
	protected String username;
	protected String userInfo;
	protected static List<Lex> lexes /* all my exes live in texas */ = new ArrayList<Lex>();
	protected Queue<String> dataQueue;
	protected int queueDelay = 50;

	public event<WOnPing> onPingEvent = new event<>();
	public event<WOnMotdEnd> onMotdEndEvent = new event<>();
	public event<WOnRawData> onRawDataEvent = new event<>();
	public event<WOnChannelJoin> onChannelJoinEvent = new event<>();
	public event<WOnChannelMessage> onChannelMessage = new event<>();
	public event<WOnUserNickChange> onUserNickChangeEvent = new event<>();
	public event<WOnUserJoinChannel> onUserJoinChannelEvent = new event<>();
	public event<WOnUserQuit> onUserQuitEvent = new event<>();
    public event<WOnUserLeave> onUserLeaveEvent = new event<>();

	public final HashMap<String, IRCChannel> joinedRooms;

	public prop<Boolean> hasMotdEnd = new prop<Boolean>(false) {
		@Deprecated
		@Override
		public void set(Boolean newValue) {
			value = newValue;
		}

		@Override
		public Boolean get() {
			return value;
		}
	};

	private boolean useSendQueue;
	private Thread sendThread;

	static {
		lexes.add(new PingLex());
		lexes.add(new MotdEndLex());
		//lexes.add(new RoomJoinLex());
		lexes.add(new ChannelNamesLex());
		lexes.add(new ChannelMessageLex());
		lexes.add(new UserChannelJoinLex());
		lexes.add(new UserNickChangeLex());
		lexes.add(new UserQuitLex());
        lexes.add(new UserLeaveLex());
	}

	public WolfIRC(final String _username, final String _userInfo) {
		client = new YTcpClient();
		username = _username;
		userInfo = _userInfo;
		joinedRooms = new HashMap<>();
		dataQueue = new ArrayDeque<>();
	}

	public WolfIRC(final String _username) {
		this(_username, "WolfIRC Bot");
	}

	public void connect(String _host) {
		connect(_host, 6667);
	}

	public void connect(String _host, int _port) {
		port = _port;
		host = _host;

		/* sets the tcp client for reading raw string lines */
		client.registerExtension(new CTcpLineRead());

		/* tries to connect the irc server */
		client.connect(IPAddress.parse(_host), port);
		/* sends user info */
		sendRawLine("NICK " + username);
		sendRawLine("USER " + username + " 8 * :" + userInfo);
		/* listens for data */
		client.dataReceivedEvent.bind(new CDataReceivedEvent() {
			@Override
			public void onDataReceived(byte[] bytes) {
				final String received = new String(bytes);
				for (Lex lex : lexes) {
					Matcher matcher = lex.regex().matcher(received);
					if (matcher.find()) {
						lex.onDataReceive(received, matcher, instance());
					}
				}
				onRawDataEvent.fire(received);
			}
		});


		if (isUsingSendQueue()) {
			sendThread = new Thread(this::_writeQueue);
			sendThread.start();
		}
	}

	protected void _writeQueue() {
		while (isConnected()) {
			if (!dataQueue.isEmpty()) {
				String writeData = dataQueue.poll();
				sendDirectLine(writeData);
			}
			kThread.sleep(queueDelay);
		}
	}

	public void close() {
		client.close();
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public void sendRaw(String data) {
		client.send(data.getBytes(StandardCharsets.UTF_8));
	}

	public void sendDirectLine(String data) {
		client.send((data + "\n").getBytes(StandardCharsets.UTF_8));
	}

	public void setMode(String mode) {
		if (!mode.startsWith("+")) {
			mode = "+" + mode;
		}
		sendRawLine("MODE " + mode);
	}

	public void leaveChannel(String channelName) {
		if (!channelName.startsWith("#")) {
			channelName = "#" + channelName;
		}
		sendRawLine("PART " + channelName);
	}

	public void sendPM(String user, String message) {
		sendDirectLine(String.format("PRIVMSG %s :%s", user, message));
	}

	@Deprecated
	public void register(String password) {
		sendPM("NICKSERV", String.format("register %s", password));
	}

	public void register(String password, String email) {
		sendPM("NICKSERV", String.format("register %s %s", password, email));
	}

	public void login(String password) {
		sendPM("NICKSERV", String.format("identify %s", password));
	}

	public void leaveChannel(String channelName, String reason) {
		if (!channelName.startsWith("#")) {
			channelName = "#" + channelName;
		}
		sendRawLine("PART " + channelName + " " + reason);
	}

	public void sendMessage(String message, String channel) {
		if (!channel.startsWith("#")) {
			channel = "#" + channel;
		}
		sendRawLine("PRIVMSG " + channel + " :" + message);
	}

	public void joinChannel(String channel) {
		if (channel.startsWith("#"))
			channel = channel.substring(1);

		if (hasMotdEnd.get()) {
			sendRawLine("JOIN #" + channel);
		} else {
			final String channelName = channel;
			prop<Integer> eventId = new prop<>();
			eventId.set(onMotdEndEvent.bind(new WOnMotdEnd() {
				@Override
				public void onMotdEnd() {
					sendRawLine("JOIN #" + channelName);
					onMotdEndEvent.free(eventId.get());
				}
			}));
		}
	}



	public void changeUsername(String _username) {
		sendRawLine("NICK " + _username);

		for (IRCChannel ircChannel : joinedRooms.values()) {
			for (IRCUser user : ircChannel.users()) {
				if (user.username().equalsIgnoreCase(username)) {
					user.setUsername(_username);
					break;
				}
			}
		}

		username = _username;
	}

	public void sendRawLine(String data) {
		if (useSendQueue) {
			dataQueue.add(data);
		} else {
			sendDirectLine(data);
		}
	}

	protected WolfIRC instance() {
		return this;
	}

	public WolfIRC useSendQueue() {
		useSendQueue = true;
		return this;
	}

	public WolfIRC useSendQueue(int delay) {
		queueDelay = delay;
		useSendQueue = true;
		return this;
	}

	public String username() {
		return username;
	}

	public String userInfo() {
		return userInfo;
	}

	public boolean isUsingSendQueue() {
		return useSendQueue;
	}
}
