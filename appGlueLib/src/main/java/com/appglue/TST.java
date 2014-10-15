package com.appglue;

import java.util.ArrayList;

public class TST<E> {

    private int N;
    private Node root;
    private ArrayList<String> keys;

    public TST() {
        keys = new ArrayList<String>();
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    public E get(String key) {
        if (key == null)
            return null;
        if (key.length() == 0)
            return null;
        Node n = get(root, key, 0);
        if (n == null)
            return null;

        return n.v;
    }

    public Node get(Node n, String k, int ix) {
        if (k == null)
            return null;
        if (k.length() == 0)
            return null;
        if (n == null)
            return null;

        char c = k.charAt(ix);

        if (c < n.c)
            return get(n.left, k, ix);
        else if (c > n.c)
            return get(n.right, k, ix);
        else if (ix < k.length() - 1)
            return get(n.down, k, ix + 1);
        else
            return n;
    }

    public int size() {
        return N;
    }

    public void put(String s, E val) {
        if (!contains(s)) N++;
        root = put(root, s, val, 0);
        keys.add(s);
    }

    private Node put(Node n, String k, E v, int ix) {
        char c = k.charAt(ix);

        if (n == null) {
            n = new Node();
            n.c = c;
        }

        if (c < n.c) {
            n.left = put(n.left, k, v, ix);
        } else if (c > n.c) {
            n.right = put(n.right, k, v, ix);
        } else if (ix < k.length() - 1) {
            n.down = put(n.down, k, v, ix + 1);
        } else {
            n.v = v;
        }

        return n;
    }

    public String longestPrefixOf(String s) {
        if (s == null || s.length() == 0)
            return null;
        int length = 0;
        Node n = root;
        int i = 0;

        while (n != null && i < s.length()) {
            char c = s.charAt(i);
            if (c < n.c) {
                n = n.left;
            } else if (c > n.c) {
                n = n.right;
            } else {
                i++;
                if (n.v != null)
                    length = i;
                n = n.down;
            }
        }

        return s.substring(0, length);
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    private class Node {

        private char c;

        private Node left;
        private Node right;
        private Node down;

        private E v;

        private Node() {

        }

        private Node(E thing) {
            this.v = thing;
        }
    }
}
