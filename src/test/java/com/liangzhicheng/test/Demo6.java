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
 * 排他网关
 *
 *     备注：用来在流程中实现决策，当流程执行到该网关的时，所有分支都会判断条件是否为true，如果为true则执行该分支
 *           即使有两个分支条件都为true，排他网关也只会选择一条分支去执行，序号小的路径执行
 */
public class Demo6 {

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
                .addClasspathResource("bpmn/leave-exclusivegateway.bpmn")
                .name("请假流程-exclusivegateway")
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
        //如果节点存在条件使用uel变量，那么流程启动之前必须明确指定具体变量值
        Map<String, Object> uelMap = new HashMap<>();
        uelMap.put("day", 4);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "leaveProcess-exclusivegateway",
                uelMap);
        //输出流程实例信息
        System.out.println(String.format("流程定义id：%s", processInstance.getProcessDefinitionId()));
        System.out.println(String.format("流程实例id：%s", processInstance.getId()));
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
        List<Task> taskList = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess-exclusivegateway")
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
