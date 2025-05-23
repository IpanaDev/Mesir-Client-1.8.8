package net.minecraft.util;

import java.util.Objects;

public class IntHashMap<V>
{
    private transient Entry[] slots = new Entry[16];

    /** The number of items stored in this map */
    private transient int count;

    /** The grow threshold */
    private int threshold = 12;

    /**
     * Makes the passed in integer suitable for hashing by a number of shifts
     */
    private static int computeHash(int integer)
    {
        integer = integer ^ integer >>> 20 ^ integer >>> 12;
        return integer ^ integer >>> 7 ^ integer >>> 4;
    }

    /**
     * Computes the index of the slot for the hash and slot count passed in.
     */
    private static int getSlotIndex(int hash, int slotCount)
    {
        return hash & slotCount - 1;
    }

    /**
     * Returns the object associated to a key
     */
    public Object lookup(int p_76041_1_)
    {
        int i = computeHash(p_76041_1_);

        for (Entry entry = this.slots[getSlotIndex(i, this.slots.length)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == p_76041_1_)
            {
                return entry.valueEntry;
            }
        }

        return null;
    }

    /**
     * Returns true if this hash table contains the specified item.
     */
    public boolean containsItem(int p_76037_1_)
    {
        return this.lookupEntry(p_76037_1_) != null;
    }

    final Entry lookupEntry(int p_76045_1_)
    {
        int i = computeHash(p_76045_1_);

        for (Entry entry = this.slots[getSlotIndex(i, this.slots.length)]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == p_76045_1_)
            {
                return entry;
            }
        }

        return null;
    }

    /**
     * Adds a key and associated value to this map
     */
    public void addKey(int p_76038_1_, V p_76038_2_)
    {
        int i = computeHash(p_76038_1_);
        int j = getSlotIndex(i, this.slots.length);

        for (Entry entry = this.slots[j]; entry != null; entry = entry.nextEntry)
        {
            if (entry.hashEntry == p_76038_1_)
            {
                entry.valueEntry = p_76038_2_;
                return;
            }
        }

        this.insert(i, p_76038_1_, p_76038_2_, j);
    }

    /**
     * Increases the number of hash slots
     */
    private void grow(int p_76047_1_)
    {
        Entry[] entry = this.slots;
        int i = entry.length;

        if (i == 1073741824)
        {
            this.threshold = Integer.MAX_VALUE;
        }
        else
        {
            Entry[] entry1 = new Entry[p_76047_1_];
            this.copyTo(entry1);
            this.slots = entry1;
            float growFactor = 0.75F;
            this.threshold = (int)((float)p_76047_1_ * growFactor);
        }
    }

    /**
     * Copies the hash slots to a new array
     */
    private void copyTo(Entry[] p_76048_1_)
    {
        Entry[] entry = this.slots;
        int i = p_76048_1_.length;

        for (int j = 0; j < entry.length; ++j)
        {
            Entry entry1 = entry[j];

            if (entry1 != null)
            {
                entry[j] = null;

                while (true)
                {
                    Entry entry2 = entry1.nextEntry;
                    int k = getSlotIndex(entry1.slotHash, i);
                    entry1.nextEntry = p_76048_1_[k];
                    p_76048_1_[k] = entry1;
                    entry1 = entry2;

                    if (entry2 == null)
                    {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes the specified object from the map and returns it
     */
    public Object removeObject(int p_76049_1_)
    {
        Entry entry = this.removeEntry(p_76049_1_);
        return (entry == null ? null : entry.valueEntry);
    }

    final Entry removeEntry(int p_76036_1_)
    {
        int i = computeHash(p_76036_1_);
        int j = getSlotIndex(i, this.slots.length);
        Entry entry = this.slots[j];
        Entry entry1;
        Entry entry2;

        for (entry1 = entry; entry1 != null; entry1 = entry2)
        {
            entry2 = entry1.nextEntry;

            if (entry1.hashEntry == p_76036_1_)
            {
                --this.count;

                if (entry == entry1)
                {
                    this.slots[j] = entry2;
                }
                else
                {
                    entry.nextEntry = entry2;
                }

                return entry1;
            }

            entry = entry1;
        }

        return null;
    }

    /**
     * Removes all entries from the map
     */
    public void clearMap()
    {
        Entry[] entry = this.slots;

        int i;
        for (i = 0; i < entry.length; i++)
        {
            entry[i] = null;
        }

        this.count = 0;
    }

    /**
     * Adds an object to a slot
     */
    private void insert(int p_76040_1_, int p_76040_2_, V p_76040_3_, int p_76040_4_)
    {
        Entry entry = this.slots[p_76040_4_];
        this.slots[p_76040_4_] = new IntHashMap.Entry(p_76040_1_, p_76040_2_, p_76040_3_, entry);

        if (this.count++ >= this.threshold)
        {
            this.grow(2 * this.slots.length);
        }
    }

    static class Entry
    {
        final int hashEntry;
        Object valueEntry;
        IntHashMap.Entry nextEntry;
        final int slotHash;

        Entry(int p_i1552_1_, int p_i1552_2_, Object p_i1552_3_, Entry p_i1552_4_)
        {
            this.valueEntry = p_i1552_3_;
            this.nextEntry = p_i1552_4_;
            this.hashEntry = p_i1552_2_;
            this.slotHash = p_i1552_1_;
        }

        public final int getHash()
        {
            return this.hashEntry;
        }

        public final Object getValue()
        {
            return this.valueEntry;
        }

        public final boolean equals(Object p_equals_1_)
        {
            if (p_equals_1_ instanceof IntHashMap.Entry) {
                Entry entry = (Entry) p_equals_1_;
                Object object = this.getHash();
                Object object1 = entry.getHash();

                if (Objects.equals(object, object1)) {
                    Object object2 = this.getValue();
                    Object object3 = entry.getValue();

                    return Objects.equals(object2, object3);
                }

            }
            return false;
        }

        public final int hashCode()
        {
            return IntHashMap.computeHash(this.hashEntry);
        }

        public final String toString()
        {
            return this.getHash() + "=" + this.getValue();
        }
    }
}
