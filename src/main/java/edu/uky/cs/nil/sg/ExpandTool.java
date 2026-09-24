package edu.uky.cs.nil.sg;

import java.io.File;
import java.util.List;

import edu.uky.cs.nil.tt.world.LogicalWorld;

/**
 * A {@link StoryGraphTool story graph tool} that provides an interface to the
 * {@link Expand} task.
 * 
 * @author Stephen G. Ware
 */
public class ExpandTool extends StoryGraphTool {
	
	/** An argument specifying the input story graph file or directory */
	protected static final Argument<File> INPUT = new Argument.Index<>(0, Argument.FILE, "a Tandem Tales Logical World JSON file");
	
	/** An argument specifying the output story graph file or directory */
	protected static final Argument<File> OUTPUT = new Argument.Key<>("out", Argument.FILE, "story graph output file (default: world name)");
	
	/** An argument specifying the max search depth */
	protected static final Argument<Integer> DEPTH_LIMIT = new Argument.Key<>("depth", new Argument.Integer(0), "max depth of the search (default: " + Expand.UNLIMITED_DEPTH + " for unlimited)");
	
	/**
	 * The main entry point for the Tandem Tales logical world story graph
	 * expander tool.
	 * 
	 * @param args the arguments passed to this tool from the terminal
	 * @throws Exception if a problem occurs while running this tool
	 */
	public static void main(String[] args) throws Exception {
		new ExpandTool().run(new Arguments(args));
	}
	
	/**
	 * Constructs a Tandem Tales Logical World story graph expansion tool.
	 */
	public ExpandTool() {
		// default constructor
	}
	
	@Override
	public String getName() {
		return "Expand Tandem Tales Logical World Story Graph";
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public String getAuthors() {
		return "Stephen G. Ware";
	}
	
	@Override
	public String getDescription() {
		return "Expands every possible state of a Tandem Tales Logical World story using breadth-first search from the initial state.";
	}
	
	@Override
	public List<Argument<?>> getRequiredArguments() {
		List<Argument<?>> required = super.getRequiredArguments();
		required.add(INPUT);
		return required;
	}
	
	@Override
	public List<Argument<?>> getOptionalArguments() {
		List<Argument<?>> optional = super.getRequiredArguments();
		optional.add(HELP);
		optional.add(OUTPUT);
		optional.add(DEPTH_LIMIT);
		return optional;
	}
	
	/**
	 * Configures and runs the {@link Expand} task according to the given
	 * arguments.
	 * 
	 * @param arguments the arguments passed to this tool from the terminal
	 * @throws Exception if a problem occurs while configuring or running the
	 * task
	 */
	public void run(Arguments arguments) throws Exception {
		if(arguments.size() == 0 || arguments.get(HELP)) {
			System.out.println(getDocumentation());
			return;
		}
		File input = arguments.get(INPUT);
		arguments.get(OUTPUT);
		int depth = Expand.UNLIMITED_DEPTH;
		if(arguments.get(DEPTH_LIMIT) != null)
			depth = arguments.get(DEPTH_LIMIT);
		arguments.checkUnused();
		LogicalWorld world = LogicalWorld.read(input);
		Expand expand = new Expand(world, depth);
		File output;
		if(arguments.get(OUTPUT) == null)
			output = new File(world.name + ".zip");
		else
			output = arguments.get(OUTPUT);
		Task.run(status -> {
			expand.run(status);
			String message = status.getMessage();
			if(expand.graph.nodes.size() > 0)
				expand.graph.write(output);
			status.setMessage(message);
		});
		if(expand.graph.nodes.size() > 0)
			System.out.println(new StoryGraphSummary(expand.graph));
	}
}
