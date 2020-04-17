package com.gabrieldev.mapaucb.dijkstra;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node {

    private String name;

    private LatLng latLng;

    private LinkedList<Node> shortestPath = new LinkedList<>();

    private float distance = Float.MAX_VALUE;

    private Map<Node, Float> adjacentNodes = new HashMap<>();

    /*public Node(String name) {
        this.name = name;
    }*/

    public Node(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    public void addDestination(Node destination, float distance ) {
        adjacentNodes.put(destination, distance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Node, Float> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void setAdjacentNodes(Map<Node, Float> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }


    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(LinkedList<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

}
