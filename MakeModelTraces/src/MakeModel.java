import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sysma.lqn.makeModel.LQNTraces;
import org.sysma.schedulerExecutor.TaskDump;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class MakeModel {
	public static void main(String infn, String modelFn) throws IOException {
		String log = Files.readString(Path.of(infn));
		var tds = TaskDump.fromJsons(log);
		var lqt = LQNTraces.from(tds);
		var mdl = lqt.getModel();
		Files.writeString(Path.of(modelFn), mdl.toXml());
		XStream xstream = new XStream(new StaxDriver());
		xstream.processAnnotations(org.sysma.lqnexecutor.model.LQN.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.Busy.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.Call.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.CallWithReg.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.Think.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.WaitFor.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.WaitForWithReg.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.ForwardCallWithReg.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.ProbChoice.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.Database.class);
		xstream.processAnnotations(org.sysma.lqnexecutor.model.Query.class);
	}
}
