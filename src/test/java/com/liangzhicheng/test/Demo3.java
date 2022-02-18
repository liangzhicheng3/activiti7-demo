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
 * UEL表达式分配
 */
public class Demo3 {

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
                .addClasspathResource("bpmn/leave-uel.bpmn")
                .name("请假流程-uel")
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
        //如果节点审批人员使用uel变量，那么流程启动之前必须明确指定具体变量值
        Map<String, Object> uelMap = new HashMap<>();
        uelMap.put("assignee0", "liangwu");
        uelMap.put("assignee1", "guanliu");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "leaveProcess-uel",
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
                .processDefinitionKey("leaveProcess-uel")
                //查询指定负责人员
                .taskAssignee("guanliu")
                .list();
        if(taskList != null && taskList.size() > 0){
            //处理任务、完成任务、审批任务
            for(Iterator<Task> tasks = taskList.iterator(); tasks.hasNext();){
                taskService.complete(tasks.next().getId());
            }
        }
    }

}
