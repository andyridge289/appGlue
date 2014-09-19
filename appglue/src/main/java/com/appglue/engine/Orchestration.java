package com.appglue.engine;

import android.app.Service;

import com.appglue.IODescription;
import com.appglue.description.IOValue;
import com.appglue.description.ServiceDescription;
import com.appglue.library.IOFilter;
import com.appglue.TST;

import java.util.ArrayList;

public class Orchestration {
    private TST<ServiceDescription> components;
//    private ArrayList<ArrayList<Edge>> edges;

    private ArrayList<Node> nodes;

    public Orchestration() {
        components = new TST<ServiceDescription>();
//        edges = new ArrayList<ArrayList<Edge>>();
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
//            edges.add(new ArrayList<Edge>());
        }

        Node n = nodes.get(position);
        n.addComponent(component);
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
        private ArrayList<ServiceDescription> getComponents() {
            return components;
        }

        // FIXME Work out what these nodes and edges are and if we need to keep them. Maybe add it as a future project to git
    }

    private class Edge {

        private int startIndex;
        private int endIndex;

        private ServiceDescription start;
        private ServiceDescription end;

        private IODescription out;
        private IODescription in;

        private IOFilter filter;
        private IOValue value;

        private Edge(int startIndex, int endIndex, ServiceDescription start, ServiceDescription end,
                     IODescription out, IODescription in) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.start = start;
            this.end = end;
            this.out = out;
            this.in = in;
        }

        private Edge(int index, ServiceDescription component, IODescription io, IOFilter filter) {
            this.startIndex = index;
            this.start = component;
            this.out = io;
        }

        private Edge(int index, ServiceDescription component, IODescription io, IOValue value) {
            this.endIndex = index;
            this.end = component;
            this.in = io;
        }

    }
}
