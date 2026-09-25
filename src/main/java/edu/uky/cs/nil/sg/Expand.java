package edu.uky.cs.nil.sg;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.world.Action;
import edu.uky.cs.nil.tt.world.Ending;
import edu.uky.cs.nil.tt.world.LogicalWorld;
import edu.uky.cs.nil.tt.world.State;

/**
 * A {@link Task task} that expands all reachable states of a {@link
 * LogicalWorld Tandem Tales logical story world} from the initial state using
 * breadth-first search and then translates those states into a {@link
 * StoryGraph story graph}.
 * 
 * @author Stephen G. Ware
 */
public class Expand implements Task {
	
	/** Represents no limit on the search depth */
	public static final int UNLIMITED_DEPTH = 0;
	
	private static class Pair {
		
		public final WorldNode parent;
		public final WorldNode child;
		
		public Pair(WorldNode parent, WorldNode child) {
			this.parent = parent;
			this.child = child;
		}
	}
	
	/**
	 * The Tandem Tales logical world that defines what states and actions are
	 * reachable
	 */
	public final LogicalWorld world;
	
	/** The story graph that will reflect the expanded story world */
	public final StoryGraph graph;
	
	/**
	 * A limit on the max depth of the search ({@link #UNLIMITED_DEPTH} for no
	 * limit)
	 */
	public final int depth;
	
	/**
	 * Constructs a Tandem Tales logical world state graph expander task for the
	 * given story world with a limit on the depth of the expansion.
	 * 
	 * @param world the Tandem Tales logical story world to be expanded
	 * @param depth a limit on the max depth of the expansion
	 */
	public Expand(LogicalWorld world, int depth) {
		this.world = world;
		this.graph = new StoryGraph();
		this.depth = depth;
	}
	
	/**
	 * Constructs a Tandem Tales logical world state graph expander task for the
	 * given story world with no limit on the depth of expansion.
	 * 
	 * @param world the Tandem Tales logical story world to be expanded
	 */
	public Expand(LogicalWorld world) {
		this(world, UNLIMITED_DEPTH);
	}
	
	@Override
	public void run(Status status) throws Exception {
		// Expand safe states.
		Map<WorldNode, WorldNode> safe = new HashMap<>();
		Set<WorldNode> unsafe = new HashSet<>();
		WorldNode root = new WorldNode.Root(world.start(Role.GAME_MASTER).getState());
		Queue<WorldNode> queue = new ArrayDeque<>();
		unsafe.add(root);
		queue.offer(root);
		int depth = 0;
		status.set("Expanding safe states at depth " + depth, (long) queue.size());
		while(queue.size() > 0) {
			WorldNode parent = queue.poll();
			State state = parent.getState();
			if(terminal(state))
				safe(parent, safe, unsafe);
			else if(this.depth == UNLIMITED_DEPTH || depth < this.depth) {
				for(Action action : world.getActions()) {
					if(world.getPrecondition(action).test(state)) {
						WorldNode child = new WorldNode.Delta(parent, world.getEffects(action));
						if(safe.containsKey(child))
							safe(parent, safe, unsafe);
						else if(unsafe.add(child))
							queue.offer(child);
					}
				}
			}
			status.increment();
			if(status.getCount() == status.getTotal())
				status.set("Expanding safe states at depth " + (++depth), (long) queue.size());
		}
		// Check unsafe states.
		int round = 1;
		status.set("Checking unsafe states, round " + round, (long) unsafe.size());
		Queue<Pair> pairs = new ArrayDeque<>();
		for(Iterator<WorldNode> iterator = unsafe.iterator(); iterator.hasNext();) {
			WorldNode parent = iterator.next();
			State state = parent.getState();
			for(Action action : world.getActions()) {
				if(world.getPrecondition(action).test(state)) {
					WorldNode child = new WorldNode.Delta(parent, world.getEffects(action));
					if(safe.containsKey(child))
						safe(parent, safe, null);
					else if(unsafe.contains(child))
						pairs.offer(new Pair(parent, child));
				}
			}
			iterator.remove();
			status.increment();
		}
		int before = 0;
		while(safe.size() > before) {		
			before = safe.size();
			status.set("Checking unsafe states, round " + (++round), (long) pairs.size());
			while(status.getCount() < status.getTotal()) {
				Pair pair = pairs.poll();
				if(safe.containsKey(pair.child))
					safe(pair.parent, safe, null);
				else if(!safe.containsKey(pair.parent))
					pairs.offer(pair);
				status.increment();
			}
		}
		pairs = null;
		// Check if graph is empty.
		if(safe.size() == 0) {
			status.setMessage("No safe states found");
			return;
		}
		// Create graph symbols.
		status.setMessage("Creating story graph symbols");
		graph.setTitle(world.name);
		for(edu.uky.cs.nil.tt.world.Action action : world.getActions()) {
			for(edu.uky.cs.nil.tt.world.Entity entity : action.getConsenting()) {
				edu.uky.cs.nil.sg.Character character = graph.characters.add(entity.name);
				if(entity == world.getPlayer())
					graph.characters.setPlayer(character, true);
			}
		}
		for(edu.uky.cs.nil.tt.world.Variable variable : world.getVariables())
			graph.fluents.add(variable.name);
		graph.values.add(Objects.toString(true));
		graph.values.add(Objects.toString(false));
		for(edu.uky.cs.nil.tt.world.Entity value : world.getEntities())
			graph.values.add(value.name);
		for(edu.uky.cs.nil.tt.world.Action action : world.getActions()) {
			edu.uky.cs.nil.sg.Action act = graph.actions.add(action.name);
			for(edu.uky.cs.nil.tt.world.Entity character : action.getConsenting()) {
				edu.uky.cs.nil.sg.Character consenting = graph.characters.get(character.name);
				graph.actions.add(act, consenting);
			}
		}
		// Expand graph.
		root.node = translate(root.getState());
		queue.offer(root);
		depth = 0;
		status.set("Expanding story graph at depth " + depth, (long) queue.size());
		while(queue.size() > 0) {
			WorldNode parent = queue.poll();
			State state = parent.getState();
			if(!terminal(state)) {
				for(Action action : world.getActions()) {
					if(world.getPrecondition(action).test(state)) {
						WorldNode child = new WorldNode.Delta(parent, world.getEffects(action));
						child = safe.get(child);
						if(child != null) {
							if(child.node == null) {
								child.node = translate(child.getState());
								queue.offer(child);
							}
							edu.uky.cs.nil.sg.Action label = graph.actions.get(action.toString());
							graph.edges.temporal.add(parent.node, label, child.node);
						}
					}
				}
			}
			status.increment();
			if(status.getCount() == status.getTotal())
				status.set("Expanding story graph at depth " + (++depth), (long) queue.size());
		}
		status.setMessage("Generated " + graph.nodes.size() + " nodes and " + graph.edges.size() + " edges");
	}
	
	private final boolean terminal(State state) {
		for(Ending ending : world.getEndings())
			if(world.getCondition(ending).test(state))
				return true;
		return false;
	}
	
	private final void safe(WorldNode node, Map<WorldNode, WorldNode> safe, Set<WorldNode> unsafe) {
		if(!safe.containsKey(node)) {
			if(unsafe != null)
				unsafe.remove(node);
			safe.put(node, node);
			if(node.getParent() != null)
				safe(node.getParent(), safe, unsafe);
		}
	}
	
	private final Node translate(edu.uky.cs.nil.tt.world.State state) {
		Function<edu.uky.cs.nil.sg.Fluent, Object> values = fluent -> {
			edu.uky.cs.nil.tt.world.Variable variable = world.getVariable(fluent.getID());
			Object value = state.get(variable);
			if(value instanceof edu.uky.cs.nil.tt.world.Constant constant)
				value = constant.value;
			return value;
		};
		boolean terminal = terminal(state);
		Function<edu.uky.cs.nil.sg.Character, Object> utilities = character -> {
			if(character == null && terminal)
				return 1;
			else
				return 0;
		};
		return graph.nodes.add(values, utilities);
	}
}