package com.liangzhicheng.test;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * 任务候选人
 *
 *     备注：给任务设置多个候选人，可以从候选人中选择参与者来完成任务
 */
public class Demo5 {

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
        /**
         * 部署：
         *     1.加载流程文件->leave.bpmn
         *
         * 对应数据库表：act_re_deployment
         */
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave-candidate.bpmn")
                .name("请假流程-candidate")
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
                "leaveProcess-candidate");
        //输出流程实例信息
        System.out.println(String.format("流程定义id：%s", processInstance.getProcessDefinitionId()));
        System.out.println(String.format("流程实例id：%s", processInstance.getId()));
    }

    /**
     * 查询候选人员审批任务
     */
    @Test
    public void testSelectCandidateTaskList(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //当前要获取指定候选人员审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess-candidate")
                //查询指定候选人员
                .taskCandidateUser("zhangsan")
                .list();
        if(taskList != null && taskList.size() > 0){
            //处理任务、完成任务、审批任务
            for(Iterator<Task> tasks = taskList.iterator(); tasks.hasNext();){
                Task task = tasks.next();
                System.out.println(String.format("流程定义id：%s", task.getProcessDefinitionId()));
                System.out.println(String.format("流程实例id：%s", task.getProcessInstanceId()));
                System.out.println(String.format("任务id：%s", task.getId()));
                System.out.println(String.format("任务名称：%s", task.getName()));
            }
        }
    }

    /**
     * 领取候选人员审批任务
     */
    @Test
    public void testClaimTask(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //领取候选人员审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        //指定审批任务负责人员
        taskService.claim("2505", "zhangsan");
        //指定审批任务负责人员后，该人员不想操作，可以将assignee设置null
//        taskService.claim("2505", null);
    }

    /**
     * 指定用户去完成待办任务
     */
    @Test
    public void testCompleteTask(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //当前要获取指定用户审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        taskService.complete("2505");
    }

}
