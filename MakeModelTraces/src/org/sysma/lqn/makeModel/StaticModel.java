package org.sysma.lqn.makeModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.xml.Model;

import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

public class StaticModel {
	public static Model main(String rootdir) {
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
		Path root = Path.of(rootdir);
		final ProjectRoot projectRoot = 
		        new SymbolSolverCollectionStrategy()
		        .collect(root);
		List<ParseResult<CompilationUnit>> parseResults = projectRoot.getSourceRoots().stream().flatMap(x->{
			try {
				return x.tryToParse().stream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
        //List<ParseResult> parseResults = List.of(null)
        		
        //		projectRoot.tryToParse();
		StaticModelVisitor smdl = new StaticModelVisitor();
		for(var pr : parseResults) {
			pr.getResult().get().findRootNode().accept(smdl, null);
		}
		Model staticMdl = smdl.getModel();
		return staticMdl;
	}
}
