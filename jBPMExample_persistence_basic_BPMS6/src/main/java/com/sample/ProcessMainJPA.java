package com.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.JBPMHelper;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.UserGroupCallback;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMainJPA {

    private static EntityManagerFactory emf;

	public static final void main(String[] args) throws Exception {

		RuntimeManager manager = getRuntimeManager("sample.bpmn");
		RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
		KieSession ksession = runtime.getKieSession();

		// start a new process instance
		Map<String, Object> params = new HashMap<String, Object>();
		ProcessInstance pi = ksession.startProcess("com.sample.bpmn.hello",
				params);
		System.out.println("A process instance started : pid = " + pi.getId());

		TaskService taskService = runtime.getTaskService();

		// ------------
		{
			List<TaskSummary> list = taskService
					.getTasksAssignedAsPotentialOwner("john", "en-UK");
			for (TaskSummary taskSummary : list) {
				System.out.println("john starts a task : taskId = "
						+ taskSummary.getId());
				taskService.start(taskSummary.getId(), "john");
				taskService.complete(taskSummary.getId(), "john", null);
			}
		}

		// -----------
		{
			List<TaskSummary> list = taskService
					.getTasksAssignedAsPotentialOwner("mary", "en-UK");
			for (TaskSummary taskSummary : list) {
				System.out.println("mary starts a task : taskId = "
						+ taskSummary.getId());
				taskService.start(taskSummary.getId(), "mary");
				taskService.complete(taskSummary.getId(), "mary", null);
			}
		}

		// -----------
		ksession.dispose();

		System.exit(0);
	}

	private static RuntimeManager getRuntimeManager(String process) {
		// load up the knowledge base
		JBPMHelper.startH2Server();
		JBPMHelper.setupDataSource();
		Properties properties = new Properties();
		properties.setProperty("krisv", "");
		properties.setProperty("mary", "");
		properties.setProperty("john", "");
		UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl(
				properties);

		emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa", null);

		RuntimeEnvironment environment = RuntimeEnvironmentBuilder
				.getDefault()
				.persistence(true)
				.entityManagerFactory(emf)
				.userGroupCallback(userGroupCallback)
				.addAsset(ResourceFactory.newClassPathResource(process),
						ResourceType.BPMN2).get();
		return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(
				environment);
	}

}