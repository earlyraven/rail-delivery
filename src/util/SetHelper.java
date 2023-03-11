package traingame.util;

import java.util.Collection;

public class SetHelper {
    public static <T> boolean hasOverlap(Collection<T> collectionA, Collection<T> collectionB) {
        for (T thing : collectionA) {
            if (collectionB.contains(thing)) {
                return true;
            }
        }
        return false;
    }
}
