package org.sysma.whatifengine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MainConsole {
	
	public static Document parseModel(String filename) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new File(filename));
	}
	
	public static void saveModel(Document doc, String filename) throws IOException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		FileWriter writer = new FileWriter(new File(filename));
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}
	
	public static void horz(Document doc, NodeList tasks, String mserv, int replicas) {
		for(int i=0; i<tasks.getLength(); i++) {
			var taskAttr = tasks.item(i).getAttributes();
			if(taskAttr == null) continue;
			var taskName = taskAttr.getNamedItem("name");
			if(taskName == null || !taskName.getTextContent().equals(mserv)) continue;
			var replicasAttr = doc.createAttribute("replicas");
			replicasAttr.setValue(replicas+"");
			taskAttr.setNamedItem(replicasAttr);
		}
	}
	
	public static void vert(Document doc, NodeList tasks, String mserv, int tpool) {
		for(int i=0; i<tasks.getLength(); i++) {
			var taskAttr = tasks.item(i).getAttributes();
			if(taskAttr == null) continue;
			var taskName = taskAttr.getNamedItem("name");
			if(taskName == null || !taskName.getTextContent().equals(mserv)) continue;
			var tpoolAttr = doc.createAttribute("multiplicity");
			tpoolAttr.setValue(tpool+"");
			taskAttr.setNamedItem(tpoolAttr);
		}
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		if(args.length < 2 || (args.length-2)%3 != 0) {
			System.out.println("Usage:\n"+
					"java -jar whatif.jar <inputmodel> (<whatif_decl>)* <outputmodel>\n"+
					"where whatif_decl is a whatif declaration among the following ones:\n"+
					"   - h <msname> <repl> : horizontal scaling of microservice msname to have repl replicas\n"+
					"   - v <msname> <tpool> : vertical scaling of microservice msname to have a threadpool of size tpool");
			return;
		}
		var inpFn = args[0];
		var outFn = args[args.length-1];
		var doc = parseModel(inpFn);
		var tasks = doc.getElementsByTagName("task");
		for(int i=1; i<args.length-1; i+=3) {
			if(args[i].equals("h")) {
				horz(doc, tasks, args[i+1], Integer.parseInt(args[i+2]));
			} else if(args[i].equals("v")) {
				vert(doc, tasks, args[i+1], Integer.parseInt(args[i+2]));
			} else {
				System.out.println("Invalid whatif declaration!");
				System.out.println("Usage:\n"+
						"java -jar whatif.jar <inputmodel> (<whatif_decl>)* <outputmodel>\n"+
						"where whatif_decl is a whatif declaration among the following ones:\n"+
						"   - h <msname> <repl> : horizontal scaling of microservice msname to have repl replicas\n"+
						"   - v <msname> <tpool> : vertical scaling of microservice msname to have a threadpool of size tpool");
				return;
			}
		}
		saveModel(doc, outFn);
	}
}

