package utils;

import java.util.LinkedList;

/**
 * Created by miha.novak on 10/08/2017.
 */

/**
 * A LinkedList that deletes the last element
 * when the list contains 4 elements.
 *
 * @param <E>
 */
public class AutoDeleteList<E> extends LinkedList<E> {

    public int maxNumberOfEntries = 3;

    public void setMaxNumberOfEntries(int maxNumberOfEntries) {
        this.maxNumberOfEntries = maxNumberOfEntries;
    }

    @Override
    public void addFirst(E o) {
        super.addFirst(o);
        if (size() > maxNumberOfEntries) {
            removeLast();
        }
    }
}
