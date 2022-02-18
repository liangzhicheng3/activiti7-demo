package com.liangzhicheng.test;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 并行网关
 *
 *     备注：允许将流程分成多条分支，也可以把多条分支汇聚到一起，并行网关的功能是基于进入和外出的顺序流
 *           不会解析条件，即使顺序流中定义了条件，也会被忽略
 */
public class Demo7 {

    private ProcessEngine processEngine;

    /**
     * 流程部署
     */
    @Test
    public void testDeploy(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取流程部署涉及资源操作，使用RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave-parallelgateway.bpmn")
                .name("请假流程-parallelgateway")
                .deploy();
        //输出部署信息
        System.out.println(String.format("流程部署id：%s", deployment.getId()));
        System.out.println(String.format("流程部署名称：%s", deployment.getName()));
    }

    /**
     * 流程启动
     */
    @Test
    public void testStartProcess(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取流程启动，使用RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "leaveProcess-parallelgateway");
        //输出流程实例信息
        System.out.println(String.format("流程定义id：%s", processInstance.getProcessDefinitionId()));
        System.out.println(String.format("流程实例id：%s", processInstance.getId()));
    }

    /**
     * 指定用户去完成待办任务（备注：发起请假申请后，多个审批人员审批完成后才能往下执行）
     */
    @Test
    public void testCompleteTask(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //当前要获取指定用户审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess-parallelgateway")
                //查询指定负责人员
                .taskAssignee("zhangsan")
                .list();
        if(taskList != null && taskList.size() > 0){
            //处理任务、完成任务、审批任务
            for(Iterator<Task> tasks = taskList.iterator(); tasks.hasNext();){
                taskService.complete(tasks.next().getId());
            }
        }
    }

}
