package edu.uky.cs.nil.sg;

import edu.uky.cs.nil.tt.world.Effect;
import edu.uky.cs.nil.tt.world.State;

/**
 * Represents a {@link State Tandem Tales world state}. A node object requires
 * only a few bytes of memory, making it possible to generate many of them. Two
 * nodes are considered {@link #equals(Object) equal} if they represent the same
 * state, making it possible to detect and avoid duplicates.
 * 
 * @author Stephen G. Ware
 */
abstract class WorldNode {
	
	/** The story graph node that corresponds to this Tandem Tales node */
	public Node node = null;
	
	@Override
	public boolean equals(Object other) {
		return other instanceof WorldNode otherNode && this.getState().equals(otherNode.getState());
	}
	
	@Override
	public int hashCode() {
		return getState().hashCode();
	}
	
	@Override
	public String toString() {
		return getState().toString();
	}
	
	/**
	 * Returns the node representing that state before this node, or null if
	 * this is a root node.
	 * 
	 * @return the node before this node
	 */
	public abstract WorldNode getParent();
	
	/**
	 * Returns the Tandem Tales world state represented by this node.
	 * 
	 * @return the world state
	 */
	public abstract State getState();
	
	/**
	 * A root node stores a state and has no parent.
	 */
	static class Root extends WorldNode {
		
		private final State state;
		
		public Root(State state) {
			this.state = state;
		}
		
		@Override
		public WorldNode getParent() {
			return null;
		}
		
		@Override
		public State getState() {
			return state;
		}
	}
	
	/**
	 * A delta node stores its parent and the {@link Effect effect} applied by a
	 * single actions, making it possible to calculate this node's state.
	 */
	static class Delta extends WorldNode {
		
		private final WorldNode parent;
		private final Effect[] effects;
		
		public Delta(WorldNode parent, Effect[] effects) {
			this.parent = parent;
			this.effects = effects;
		}
		
		@Override
		public WorldNode getParent() {
			return parent;
		}
		
		@Override
		public State getState() {
			State before = getParent().getState();
			State after = before;
			for(Effect effect : effects)
				after = effect.apply(before, after);
			return after;
		}
	}
}