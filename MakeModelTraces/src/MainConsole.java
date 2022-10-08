import java.io.IOException;

public class MainConsole {

	public static void main(String[] args) throws IOException {
		if(args.length == 4 && args[0].equals("make")) {
			MakeModel.main(args[1], args[2], args[3]);
		} else if(args.length == 3 && args[0].equals("rt")) {
			MakeResponseTimes.main(args[1], args[2]);
		} else if(args.length == 3 && args[0].equals("util")) {
			MakeUtilization.main(args[1], args[2]);
		} else {
			System.out.println("Usage:\n"+
				"java -jar modeltraces.jar make <log.json> <code-dir> <model.lqnx>\n"+
				"java -jar modeltraces.jar rt <log.json> <rt.csv>\n"+
				"java -jar modeltraces.jar util <log.json> <util.csv>");
		}
	}

}
