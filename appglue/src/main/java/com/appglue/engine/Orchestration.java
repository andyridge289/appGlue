package com.appglue.engine;

import com.appglue.IOValue;
import com.appglue.ServiceIO;
import com.appglue.description.ServiceDescription;
import com.appglue.library.IOFilter;
import com.appglue.TST;

import java.util.ArrayList;

/**
 * Created by andyridge on 03/06/2014.
 */
public class Orchestration {
    private TST<ServiceDescription> components;
    private ArrayList<ArrayList<Edge>> edges;

    private ArrayList<Node> nodes;

    public Orchestration() {
        components = new TST<ServiceDescription>();
        edges = new ArrayList<ArrayList<Edge>>();
        nodes = new ArrayList<Node>();
    }

    public void addComponent(ServiceDescription component, int position) {

        // Save it to the list of components that are in this composite, we don't care if there are duplicates for this case.
        if (!this.contains(component)) {
            components.put(component.getClassName(), component);
        }

        while (nodes.size() <= position) {
            // If we've got fewer things in it than where we want to end up, add more empty ones
            nodes.add(new Node());
            edges.add(new ArrayList<Edge>());
        }

        Node n = nodes.get(position);
        n.addComponent(component);
    }

    public void addConnection(int startIndex, int endIndex,
                              ServiceDescription start, ServiceDescription end,
                              ServiceIO out, ServiceIO in) {
        Edge e = new Edge(startIndex, endIndex, start, end, out, in);

        this.edges.get(startIndex).add(e);
    }

    public void addFilter(int index, ServiceDescription component, ServiceIO io) {
        Edge e = new Edge(index, component, io, new IOFilter());
        // FIXME This needs to actually do something with the filter information
    }

    public void addValue(int index, ServiceDescription component, ServiceIO io) {
        Edge e = new Edge(index, component, io, new IOValue());
        // FIXME This actually needs to do something with the values from the thing
    }

    public boolean contains(String className) {
        return components.get(className) != null;
    }

    public boolean contains(ServiceDescription component) {
        return contains(component.getClassName());
    }

    private class Node {

        private ArrayList<ServiceDescription> components;
        private int position; // This is the LONGEST path from the root to this node

        private Node() {
            this.components = new ArrayList<ServiceDescription>();
        }

        private void addComponent(ServiceDescription component) {
            this.components.add(component);
        }
    }

    private class Edge {

        private int startIndex;
        private int endIndex;

        private ServiceDescription start;
        private ServiceDescription end;

        private ServiceIO out;
        private ServiceIO in;

        private IOFilter filter;
        private IOValue value;

        private Edge(int startIndex, int endIndex, ServiceDescription start, ServiceDescription end,
                     ServiceIO out, ServiceIO in) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.start = start;
            this.end = end;
            this.out = out;
            this.in = in;
        }

        private Edge(int index, ServiceDescription component, ServiceIO io, IOFilter filter) {
            this.startIndex = index;
            this.start = component;
            this.out = io;
        }

        private Edge(int index, ServiceDescription component, ServiceIO io, IOValue value) {
            this.endIndex = index;
            this.end = component;
            this.in = io;
        }

    }
}
