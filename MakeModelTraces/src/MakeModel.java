import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sysma.lqn.makeModel.LQNTraces;
import org.sysma.lqn.makeModel.StaticModel;
import org.sysma.lqn.xml.Model;
import org.sysma.schedulerExecutor.TaskDump;

public class MakeModel {
	public static void main(String infn, String codeDir, String modelFn) throws IOException {
		Model staticModel = StaticModel.main(codeDir);
		String log = Files.readString(Path.of(infn));
		var tds = TaskDump.fromJsons(log);
		var lqt = LQNTraces.from(staticModel, tds);
		var mdl = lqt.getModel();
		Files.writeString(Path.of(modelFn), mdl.toXml());
	}
}
