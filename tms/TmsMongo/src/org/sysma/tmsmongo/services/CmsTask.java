package org.sysma.tmsmongo.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name = "cms")
public class CmsTask extends TaskDefinition{
	
	@EntryDef("/createConfiguration")
	public void CreateConfiguration(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("qms", "CreateConfiguration", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getConfigurations")
	public void GetConfigurations(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var ans = comm.asyncCallRegistry("qms", "FindAllConfigurations", (x)->{}).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getCategoryInfoDtos")
	public void GetCategoryInfoDtos(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var ans = comm.asyncCallRegistry("qms", "FindAllCategoryInfos", (x)->{}).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getQuestionsForExam")
	public void GetQuestionsForExam(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("ems", "ListAllQuestionsForExam", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/deleteExam")
	public void DeleteExam(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("ems", "DeleteExam", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/createExam")
	public void CreateExam(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("ems", "CreateExam", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getExams")
	public void GetExams(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var ans = comm.asyncCallRegistry("ems", "ListAllExams", (x)->{}).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/isEmailValid")
	public void IsEmailValid(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("ums", "IsEmailInUse", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getExamineeInfo")
	public void GetExamineeInfo(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		
		var ans = comm.asyncCallRegistry("ums", "UserById", (x)->{}, params).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/getAllUsers")
	public void GetAllUsers(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var ans = comm.asyncCallRegistry("ums", "GetAllUsers", (x)->{}).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
}
