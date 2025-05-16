package ipana.managements.friend;

import java.util.ArrayList;

public class FriendManager {
    public static ArrayList<Friend> fList = new ArrayList<>();

    public static void add(String name) {
        fList.add(new Friend(name));
    }

    public static void del(String name) {
        fList.removeIf(friend -> friend.name.equals(name));
    }

    public static String getName(String name) {
        String is = null;
        for (Friend friend : fList) {
            if (friend.name.equals(name)) {
                is = friend.name;
            }
        }
        return is;
    }

    public static boolean isFriend(String name) {
        for (Friend friend : fList) {
            if (friend.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Friend> getFriends() {
        return fList;
    }
}
