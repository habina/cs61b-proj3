package graph;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/* See restrictions in Graph.java. */

/** A partial implementation of Graph containing elements common to
 *  directed and undirected graphs.
 *
 *  @author Dasheng Chen
 */
abstract class GraphObj extends Graph {

    /** A new, empty Graph. */
    GraphObj() {
        // FIXME
        _pqVertex = new PriorityQueue<Integer>();
        _pqEdgeId = new PriorityQueue<Integer>();
        _pqVertex.add(1);
        _pqEdgeId.add(1);
        _nodeMap = new HashMap<Integer, GraphNode>();
        _edgesList = new ArrayList<int[]>();
        _edgesID = new HashMap<SimpleImmutableEntry<Integer, Integer>, Integer>();
    }

    @Override
    public int vertexSize() {
        // FIXME
        return _nodeMap.size();
    }

    @Override
    public int maxVertex() {
        // FIXME
        return _maxVertex;
    }

    @Override
    public int edgeSize() {
        // FIXME
        return _edgesList.size();
    }

    @Override
    public abstract boolean isDirected();

    @Override
    public int outDegree(int v) {
        // FIXME
        GraphNode gn = _nodeMap.get(v);
        if (gn == null) {
            return 0;
        }
        return gn.successor.size();
    }

    @Override
    public abstract int inDegree(int v);

    @Override
    public boolean contains(int u) {
        // FIXME
        return _nodeMap.containsKey(u);
    }

    @Override
    public boolean contains(int u, int v) {
        // FIXME
        if (_nodeMap.containsKey(u) && _nodeMap.containsKey(v)) {
            GraphNode gn = _nodeMap.get(u);
            if (isDirected()) {
                return gn.successor.contains(v);
            } else {
                return gn.successor.contains(v) || gn.predecessor.contains(v);
            }
        }
        return false;
    }

    @Override
    public int add() {
        // FIXME
        int vertex = _pqVertex.poll();
        if (vertex > _maxVertex) {
            _maxVertex = vertex;
        }
        GraphNode gn = new GraphNode();
        gn.value = vertex;
        _nodeMap.put(vertex, gn);
        if (_pqVertex.isEmpty()) {
            _pqVertex.add(_nodeMap.size() + 1);
        }
        return vertex;
    }

    @Override
    public int add(int u, int v) {
        // FIXME
        if (contains(u) && contains(v)) {
            if (containsEdges(u, v)) {
                return u;
            }
            int avalId = _pqEdgeId.poll();
            GraphNode uNode = _nodeMap.get(u);
            GraphNode vNode = _nodeMap.get(v);
            uNode.successor.addFirst(v);
            vNode.predecessor.addFirst(u);
//            uNode.successor.add(v);
//            vNode.predecessor.add(u);
            if (isDirected()) {
                _edgesList.add(new int[]{u, v});
                _edgesID.put(new SimpleImmutableEntry<Integer, Integer>(u, v), avalId);
            } else {
                int larger = Math.max(u, v);
                int smaller = Math.min(u, v);
                _edgesList.add(new int[]{smaller, larger});
                _edgesID.put(new SimpleImmutableEntry<Integer, Integer>(smaller, larger), avalId);
                uNode.predecessor.addFirst(v);
                vNode.successor.addFirst(u);
            }
            if (_pqEdgeId.isEmpty()) {
                _pqEdgeId.offer(_edgesList.size() + 1);
            }
        }
        return u;
    }
    
    private boolean containsEdges(int u, int v) {
        if (isDirected()) {
            return _edgesID.containsKey(new SimpleImmutableEntry<Integer, Integer>(u, v));
        } else {
            int larger = Math.max(u, v);
            int smaller = Math.min(u, v);
            return _edgesID.containsKey(new SimpleImmutableEntry<Integer, Integer>(smaller, larger));
        }
    }

    private int indexEdges(int u, int v) {
        int count = -1;
        for (int[] tuple : _edgesList) {
            count += 1;
            if (tuple[0] == u && tuple[1] == v) {
                break;
            }
        }
        if (!isDirected()) {
            count = -1;
            for (int[] tuple : _edgesList) {
                count += 1;
                if (tuple[0] == v && tuple[1] == u) {
                    break;
                }
            }
        }
        return count;
    }

    @Override
    public void remove(int v) {
        // FIXME
        if (contains(v)) {
            GraphNode gn = _nodeMap.get(v);
            for (Integer preVertex : gn.predecessor) {
                ArrayDeque<Integer> suc = _nodeMap.get(preVertex).successor;
                SimpleImmutableEntry<Integer, Integer> key = new SimpleImmutableEntry<Integer, Integer>(preVertex, v);
                if (_edgesID.containsKey(key)) {
                    int oldId = _edgesID.get(key);
                    _pqEdgeId.add(oldId);
                    _edgesID.remove(key);
                }
                suc.remove(v);
                _edgesList.remove(indexEdges(preVertex, v));
            }
            for (Integer sucVertex : gn.successor) {
                ArrayDeque<Integer> prd = _nodeMap.get(sucVertex).predecessor;
                SimpleImmutableEntry<Integer, Integer> key = new SimpleImmutableEntry<Integer, Integer>(v, sucVertex);
                if (_edgesID.containsKey(key)) {
                    int oldId = _edgesID.get(key);
                    _pqEdgeId.add(oldId);
                    _edgesID.remove(key);
                }
                prd.remove(v);
                if (isDirected()) {
                    _edgesList.remove(indexEdges(v, sucVertex));
                }
            }
            _nodeMap.remove(v);
            _pqVertex.add(v);
        }
    }

    @Override
    public void remove(int u, int v) {
        // FIXME
        GraphNode uNode = _nodeMap.get(u);
        GraphNode vNode = _nodeMap.get(v);
        if (uNode != null && vNode != null && containsEdges(u, v)) {
            uNode.successor.remove(v);
            vNode.predecessor.remove(u);
            if (isDirected()) {
                _edgesList.remove(indexEdges(u, v));
            } else {
                uNode.predecessor.remove(v);
                vNode.successor.remove(u);
                int larger = Math.max(u, v);
                int smaller = Math.min(u, v);
                SimpleImmutableEntry<Integer, Integer> key = new SimpleImmutableEntry<Integer, Integer>(smaller, larger);
                int oldId = _edgesID.get(key);
                _edgesID.remove(key);
                _pqEdgeId.add(oldId);
                _edgesList.remove(indexEdges(smaller, larger));
            }
        }
    }

    @Override
    public Iteration<Integer> vertices() {
        // FIXME
        return Iteration.iteration(_nodeMap.keySet().iterator());
    }

    @Override
    public int successor(int v, int k) {
        // FIXME
        if (contains(v)) {
            GraphNode gn = _nodeMap.get(v);
            if (k >= gn.successor.size() || k < 0) {
                return 0;
            } else {
                int count = 0;
                for (Integer i : gn.successor) {
                    if (count == k) {
                        return i;
                    }
                    count += 1;
                }
            }
        }
        return 0;
    }

    @Override
    public abstract int predecessor(int v, int k);

    @Override
    public Iteration<Integer> successors(int v) {
        // FIXME
        if (contains(v)) {
            GraphNode gn = _nodeMap.get(v);
            return Iteration.iteration(gn.successor.iterator());
        }
        return null;
    }

    @Override
    public abstract Iteration<Integer> predecessors(int v);

    @Override
    public Iteration<int[]> edges() {
        // FIXME
        return Iteration.iteration(_edgesList.iterator());
    }

    @Override
    protected boolean mine(int v) {
        // FIXME
        return contains(v);
    }

    @Override
    protected void checkMyVertex(int v) {
        // FIXME
        if (!this.contains(v)) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected int edgeId(int u, int v) {
        // FIXME
        SimpleImmutableEntry<Integer, Integer> key = null;
        if (isDirected()) {
            key = new SimpleImmutableEntry<Integer, Integer>(u, v);
        } else {
            int larger = Math.max(u, v);
            int smaller = Math.min(u, v);
            key = new SimpleImmutableEntry<Integer, Integer>(smaller, larger);
        }
        if (_edgesID.containsKey(key)) {
            return _edgesID.get(key).intValue();
        } else {
            return 0;
        }
    }

    // FIXME
    /** A graph node class. */
    class GraphNode {
        int value;
        ArrayDeque<Integer> predecessor = new ArrayDeque<Integer>();
        ArrayDeque<Integer> successor = new ArrayDeque<Integer>();
    }
    /** A PQ, head is the smallest available vertex number. */
    private PriorityQueue<Integer> _pqVertex;
    /** A PQ, head is the smallest available edge id number. */
    private PriorityQueue<Integer> _pqEdgeId;
    /** A hashMap that mapping from integer to corresponding vertex. */
    protected HashMap<Integer, GraphNode> _nodeMap;
    /** Max vertex. */
    private int _maxVertex;
    /** Edges list. */
    private ArrayList<int[]> _edgesList;
    /** Edges ID. */
    private HashMap<SimpleImmutableEntry<Integer, Integer>, Integer> _edgesID;
}
