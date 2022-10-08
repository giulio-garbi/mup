package org.sysma.lqn.makeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.xml.Activity;
import org.sysma.lqn.xml.Entry;
import org.sysma.lqn.xml.Model;
import org.sysma.lqn.xml.Precedence;
import org.sysma.lqn.xml.Processor;
import org.sysma.lqn.xml.Task;
import org.sysma.lqn.xml.TaskActivities;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.PatternExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class StaticModelVisitor implements VoidVisitor<Void>{
	
	private ArrayList<Processor> procs = new ArrayList<Processor>();
	private ArrayList<Activity> activities;
	private ArrayList<Precedence> precedences;
	private ArrayList<String> replyActivities;
	
	String preKind;
	String[] pre;
	
	int actprogr = 0;
	
	int unrollLimit = 5;
	private HashMap<String, String> postAnds = new HashMap<String, String>();
	private Expression assigner;
	
	public Model getModel() {
		return new Model("mdl", procs);
	}

	@Override
	public void visit(NodeList n, Void arg) {
		// TODO Auto-generated method stub
		n.forEach(k->{((Node)k).accept(this, null);});
	}

	@Override
	public void visit(AnnotationDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnnotationMemberDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ArrayAccessExpr n, Void arg) {
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(ArrayCreationExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(ArrayCreationLevel n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(ArrayInitializerExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(ArrayType n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(AssertStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(AssignExpr n, Void arg) {
		// TODO Auto-generated method stub
		assigner = n.getTarget();
		n.getChildNodes().forEach(k->{k.accept(this, null);});
		this.addCompActivity();
	}

	@Override
	public void visit(BinaryExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(BlockComment n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BlockStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}
	
	private void addCompActivity() {
		if(!preKind.equals("pre")) {
			Precedence pr = new Precedence();
			pr.pre = pre;
			pr.preKind = preKind;
			Activity act = new Activity("act+"+(actprogr++),0,0,0,null,false);
			activities.add(act);
			pr.post = new String[] {act.name};
			pr.postKind = "post";
			precedences.add(pr);
			pre = new String[] {act.name};
			preKind = "pre";
		}
	}
	
	private void branchCond(Node[] condsNodes) {
		Precedence pr = new Precedence();
		pr.pre = pre;
		pr.preKind = preKind;
		Activity[] acts = new Activity[condsNodes.length];
		pr.postKind = "post-OR";
		pr.post = new String[condsNodes.length];
		precedences.add(pr);
		for(int i=0; i<acts.length; i++) {
			acts[i] = new Activity("act+"+(actprogr++),0,0,0,null,false);
			pr.post[i] = acts[i].name;
			activities.add(acts[i]);
		}
		ArrayList<String> preAfterOr = new ArrayList<>();
		for(int i=0; i<acts.length; i++) {
			pre = new String[] {acts[i].name};
			preKind = "pre";
			condsNodes[i].accept(this, null);
			for(var an : pre)
				preAfterOr.add(an);
		}
		preKind = "pre-OR";
		pre = preAfterOr.toArray(new String[0]);
	}

	@Override
	public void visit(BooleanLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BreakStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CastExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(CatchClause n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(k->{k.accept(this, null);});
	}

	@Override
	public void visit(CharLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ClassExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}
	
	private static boolean isSuffix(String s1, String s2)
    {
        int n1 = s1.length(), n2 = s2.length();
        if (n1 > n2)
        return false;
        for (int i=0; i<n1; i++)
        if (s1.charAt(n1 - i - 1) != s2.charAt(n2 - i - 1))
            return false;
        return true;
    }

	@Override
	public void visit(ClassOrInterfaceDeclaration n, Void arg) {
		String taskdefClassName = "org.sysma.schedulerExecutor.TaskDefinition";
		if(!n.getExtendedTypes().stream().anyMatch(t->isSuffix(t.getName().toString(), taskdefClassName)))
			// not a microservice
			return;
		String taskAnnType="org.sysma.schedulerExecutor.TaskDef";
		var taskAnns = n.getAnnotations().stream().filter(a->isSuffix(a.getNameAsString(),taskAnnType)).collect(Collectors.toList());
		if(taskAnns.isEmpty())
			return;
		String taskName = taskAnns.get(0).asNormalAnnotationExpr()
				.findAll(MemberValuePair.class, x->((MemberValuePair)x).getNameAsString().equals("name"))
				.get(0).getValue().asStringLiteralExpr().getValue();
		
		List<Entry> entries = new ArrayList<>();
		activities = new ArrayList<Activity>();
		precedences = new ArrayList<Precedence>();
		TaskActivities ta = new TaskActivities(activities, precedences, entries);
		Task task = new Task(taskName, 1, 1, entries, ta, false);
		Processor proc = new Processor("proc_"+taskName, task);
		procs.add(proc);

		String epAnnType="org.sysma.schedulerExecutor.EntryDef";
		for(var m : n.getMethods().stream()
				.filter(x->x.getAnnotations().stream()
						.anyMatch(a->isSuffix(a.getNameAsString(),epAnnType))).collect(Collectors.toList())) {
			replyActivities = new ArrayList<String>();
			String entName = taskName+"-"+m.getName().asString();
			Entry ent = new Entry(entName, replyActivities);
			entries.add(ent);
			Activity firstAct = new Activity("act+"+(actprogr++),0,0,0,entName,false);
			activities.add(firstAct);
			preKind = "pre";
			pre = new String[] {firstAct.name};
			m.accept(this, null);
		}
	}

	@Override
	public void visit(ClassOrInterfaceType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CompilationUnit n, Void arg) {
		for(var c:n.getChildNodes()) c.accept(this, null);
	}

	@Override
	public void visit(ConditionalExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getCondition().accept(this, null);
		this.branchCond(new Node[] {n.getThenExpr(), n.getElseExpr()});
	}

	@Override
	public void visit(ConstructorDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ContinueStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}
	
	private Statement unroll(int n, Statement init, Statement preCond, Expression cond, Statement body){
		if(n == 0) {
			BlockStmt bs0 = new BlockStmt();
			bs0.addStatement(init);
			bs0.addStatement(preCond);
			return bs0;
		}
		Statement then = body;
		for(int i=0; i<n; i++) {
			BlockStmt bs = new BlockStmt();
			bs.addStatement(preCond);
			bs.addStatement(new IfStmt(cond, then, new EmptyStmt()));
			then = bs;
		}
		BlockStmt bsN = new BlockStmt();
		bsN.addStatement(init);
		bsN.addStatement(then);
		return bsN;
	}
	
	private Statement nl2stmt(NodeList<Expression> nl) {
		BlockStmt bs0 = new BlockStmt();
		for(var e: nl)
			bs0.addStatement(e);
		return bs0;
	}

	@Override
	public void visit(DoStmt n, Void arg) {
		// TODO Auto-generated method stub
		unroll(this.unrollLimit-1, n.getBody(), new EmptyStmt(), n.getCondition(), n.getBody())
			.accept(this, null);
	}

	@Override
	public void visit(DoubleLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EmptyStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EnclosedExpr n, Void arg) {
		// TODO Auto-generated method stub
		for(var c:n.getChildNodes()) c.accept(this, null);
	}

	@Override
	public void visit(EnumConstantDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EnumDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
		n.getArguments().forEach(a -> a.accept(this, null));
		try {
			n.resolve().toAst().get().getBody().accept(this, null);
		} catch (Exception e) {
			this.addCompActivity();
		}
	}

	@Override
	public void visit(ExpressionStmt n, Void arg) {
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(FieldAccessExpr n, Void arg) {
	}

	@Override
	public void visit(FieldDeclaration n, Void arg) {
		
	}

	@Override
	public void visit(ForStmt n, Void arg) {
		unroll(this.unrollLimit, nl2stmt(n.getInitialization()), nl2stmt(n.getUpdate()), n.getCompare().orElse(new BooleanLiteralExpr(true)), n.getBody())
			.accept(this, null);
		
	}

	@Override
	public void visit(ForEachStmt n, Void arg) {
		// TODO Auto-generated method stub
		unroll(this.unrollLimit, new EmptyStmt(), new EmptyStmt(), new BooleanLiteralExpr(true), n.getBody())
			.accept(this, null);
	}

	@Override
	public void visit(IfStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getCondition().accept(this, null);
		this.branchCond(new Node[] {n.getThenStmt(), n.getElseStmt().orElse(new EmptyStmt())});
	}

	@Override
	public void visit(ImportDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InitializerDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InstanceOfExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntegerLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IntersectionType n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(JavadocComment n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LabeledStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(LambdaExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LineComment n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LocalClassDeclarationStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LocalRecordDeclarationStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MarkerAnnotationExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MemberValuePair n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MethodCallExpr n, Void arg) {
		String commClass = "org.sysma.schedulerExecutor.Communication";
		String futClass = "org.apache.hc.client5.http.impl.classic.CloseableHttpResponse";
		
		String metName = n.getNameAsString();
		boolean tryresolve = true;
		try {
			String clName = n.getScope().get().calculateResolvedType().asReferenceType().getQualifiedName();
			if (isSuffix(clName, commClass)) {
				tryresolve = false;
				switch(clName) {
				case "respond":
					this.addCompActivity();
					this.replyActivities.add(pre[0]);
					break;
				case "asyncCallRegistry":
					var ms = n.getArgument(0).asStringLiteralExpr().toString();
					var ep = n.getArgument(1).asStringLiteralExpr().toString();
					var regAct = new Activity("act+"+(actprogr++),0,0,0,null,false);
					var callAct = new Activity("act+"+(actprogr++),0,0,0,null,false);
					callAct.whoCall = ms+"-"+ep;
					var thenAct = new Activity("act+"+(actprogr++),0,0,0,null,false);
					
					Precedence p1 = new Precedence();
					p1.preKind = preKind;
					p1.pre = pre;
					p1.postKind = "post-AND";
					p1.post = new String[] {regAct.name, thenAct.name};
					precedences.add(p1);
					
					Precedence p2 = new Precedence();
					p2.preKind = "pre";
					p2.pre = new String[] {regAct.name};
					p2.postKind = "post";
					p2.post = new String[] {callAct.name};
					precedences.add(p2);
					
					postAnds.put(assigner.toString(), callAct.name);
					
					preKind = "pre";
					pre = new String[] {thenAct.name};
				}
			} else if (isSuffix(clName, futClass)) {
				tryresolve = false;
				if(metName.equals("getEntity")) {
					var thenAct = new Activity("act+"+(actprogr++),0,0,0,null,false);
					Precedence p2 = new Precedence();
					p2.preKind = "pre-AND";
					p2.pre = new String[] {pre[0], postAnds.get(n.getScope().get().toString())};
					p2.postKind = "post";
					p2.post = new String[] {thenAct.name};
					precedences.add(p2);
					preKind = "pre";
					pre = new String[] {thenAct.name};
				}
			}
			
		} catch (Exception e) {
			
		}
		
		if(tryresolve) {
			try {
				n.resolve().toAst().get().getBody().get().accept(this, null);
			} catch (Exception e) {
				this.addCompActivity();
			}
		}
	}

	@Override
	public void visit(MethodDeclaration n, Void arg) {
		n.getBody().get().accept(this, null);
	}

	@Override
	public void visit(MethodReferenceExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NameExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Name n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NormalAnnotationExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NullLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(PackageDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parameter n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(PrimitiveType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RecordDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CompactConstructorDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ReturnStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SimpleName n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SingleMemberAnnotationExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SuperExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SwitchEntry n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(SwitchStmt n, Void arg) {
		Node[] brs = new Node[n.getEntries().size()];
		for(int i =0; i<brs.length; i++) {
			brs[i] = n.getEntry(i);
		}
		this.branchCond(brs);
	}

	@Override
	public void visit(SynchronizedStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThisExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ThrowStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TryStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(TypeExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TypeParameter n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(UnaryExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(UnionType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(UnknownType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VariableDeclarationExpr n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(VariableDeclarator n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(VoidType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhileStmt n, Void arg) {
		// TODO Auto-generated method stub
		unroll(this.unrollLimit,  new EmptyStmt(), new EmptyStmt(), n.getCondition(), n.getBody())
			.accept(this, null);
	}

	@Override
	public void visit(WildcardType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ModuleDeclaration n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(ModuleRequiresDirective n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(ModuleExportsDirective n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(ModuleProvidesDirective n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(ModuleUsesDirective n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(ModuleOpensDirective n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(UnparsableStmt n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ReceiverParameter n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(VarType n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Modifier n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SwitchExpr n, Void arg) {
		// TODO Auto-generated method stub
		Node[] brs = new Node[n.getEntries().size()];
		for(int i =0; i<brs.length; i++) {
			brs[i] = n.getEntry(i);
		}
		this.branchCond(brs);
	}

	@Override
	public void visit(TextBlockLiteralExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(YieldStmt n, Void arg) {
		// TODO Auto-generated method stub
		n.getChildNodes().forEach(x->x.accept(this, null));
	}

	@Override
	public void visit(PatternExpr n, Void arg) {
		// TODO Auto-generated method stub
		
	}

}
