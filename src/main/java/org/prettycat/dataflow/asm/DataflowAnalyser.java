package org.prettycat.dataflow.asm;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.prettycat.examples.test.TestClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DataflowAnalyser {
	
	final static String EXTRACTION_ANNOTATION = "Lorg/senecade/asm/Extract;";
	final static boolean EXTRACT_ALL = false;
	
	public static void main(String[] args) throws IOException {
        ClassNode sourceClassNode = new ClassNode(Opcodes.ASM5);
        String pathToClass = TestClass.class.getName().replace('.', '/') + ".class";
        byte[] bytes = IOUtils.toByteArray(DataflowAnalyser.class.getClassLoader().getResourceAsStream(pathToClass));

        ClassReader sourceClassReader = new ClassReader(bytes);

        sourceClassReader.accept(sourceClassNode, 0);


        Path path = Paths.get("./out.xml");

        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlStandalone(true);

            Element root = XMLProtocol.createASMElement(doc);
            doc.appendChild(root);

            for (MethodNode method: (List<MethodNode>)sourceClassNode.methods) {
                System.out.println(method.name + " " + method.desc);

                try {
                    // methodFlow(sourceClassNode.name, method);
                    MethodAnalysis analysis = new MethodAnalysis(sourceClassNode.name, method);
                    root.appendChild(analysis.writeXML(doc));
                } catch (AnalyzerException e) {
                    System.out.println("analysis failed: "+e);
                }
            }

            try (BufferedWriter backend_writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
                TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(backend_writer));
            } catch (IOException e) {
                System.err.format("failed to write to %s: %s\n", path, e);
            }
        } catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

}
