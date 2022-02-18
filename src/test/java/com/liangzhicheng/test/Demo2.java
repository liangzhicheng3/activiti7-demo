package com.liangzhicheng.test;

import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * 分支流程
 */
public class Demo2 {

    private ProcessEngine processEngine;

    /**
     * 初始化
     */
    @Test
    public void testInit(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(String.format("ProcessEngine：%s", processEngine));
    }

    /**
     * 流程定义
     */
    @Test
    public void testDefinitionQuery(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //流程定义也归属资源范畴，使用RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //流程定义信息对象，对应数据库表：act_re_procdef
        List<ProcessDefinition> processDefinitionList =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey("leaveProcess")
                        //获取最后版本的记录
                        //.latestVersion()
                        .list();
        if(processDefinitionList != null && processDefinitionList.size() > 0){
            for(Iterator<ProcessDefinition> processIterator =
                processDefinitionList.iterator(); processIterator.hasNext();){
                ProcessDefinition processDefinition = processIterator.next();
                System.out.println(String.format("流程定义id：%s", processDefinition.getId()));
                System.out.println(String.format("流程定义名称：%s", processDefinition.getName()));
                System.out.println(String.format("流程定义key：%s", processDefinition.getKey()));
                System.out.println(String.format("流程定义版本：%s", processDefinition.getVersion()));
                System.out.println(String.format("流程部署id：%s", processDefinition.getDeploymentId()));
            }
        }
    }

    /**
     * 资源下载
     */
    @Test
    public void testDownloadResource() throws Exception {
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //资源下载，使用RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //流程定义信息对象，对应数据库表：act_re_procdef
        List<ProcessDefinition> processDefinitionList =
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey("leaveProcess")
                        //获取最后版本的记录
//                        .latestVersion()
                        //根据版本排序
                        .orderByProcessDefinitionVersion()
                        //降序
                        .desc()
//                        .singleResult()
                        .list();
        if(processDefinitionList != null && processDefinitionList.size() > 0){
            //获取最新部署版本
            ProcessDefinition processDefinition = processDefinitionList.get(0);
            //获取bpmn资源
            InputStream bpmnStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(),
                    processDefinition.getResourceName());
            //获取png资源
            InputStream pngStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(),
                    processDefinition.getDiagramResourceName());
            IOUtils.copy(bpmnStream, new FileOutputStream("D:/leave.bpmn"));
            IOUtils.copy(pngStream, new FileOutputStream("D:/leave.png"));
        }
    }

    /**
     * 流程删除
     */
    @Test
    public void testDeleteDeploy(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //资源删除，使用RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //如果该流程定义已有流程实例启动，runtime相关的表会和部署表、流程定义表有外键约束，删除时报错，需先解除外键约束才可以删除
        repositoryService.deleteDeployment("1");
        //设置true，级联删除流程定义，即使该流程有流程实例启动也可以删除；设置为false非级联删除
//        repositoryService.deleteDeployment("1", true);
    }


    /**
     * 流程启动，进行BusinessKey绑定
     */
    @Test
    public void testStartProcess(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveProcess", "1");
        System.out.println(String.format("流程定义id：%s", processInstance.getProcessDefinitionId()));
        System.out.println(String.format("流程实例id：%s", processInstance.getId()));
        System.out.println(String.format("业务标识BusinessKey：%s", processInstance.getBusinessKey()));
    }

    /**
     * 流程进行到下一个节点，需要进行审批，此时需要获取businessKey进而从而获取请假单信息
     */
    @Test
    public void testGetBusinessKey(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //获取待办任务信息
        Task task = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess")
                //查询指定负责人员
                .taskAssignee("lisi")
                .singleResult();
        //获取businessKey，因为流程启动时，对应businessKey是绑定在流程实例中
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        String businessKey = processInstance.getBusinessKey();
        //查询请假单信息
//        leaveService.getById(Long.parseLong(businessKey));
        System.out.println(String.format("业务标识BusinessKey：%s", businessKey));
    }

    /**
     * 流程挂起与激活
     */
    @Test
    public void testSuspendAllProcessInstance(){
        //通过默认方式获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("leaveProcess")
                .singleResult();
        //当前流程定义是否标记为挂起状态
        boolean isSuspended = processDefinition.isSuspended();
        System.out.println(String.format("流程定义状态：%s", isSuspended ? "已挂起" : "已激活"));
        String processDefinitionId = processDefinition.getId();
        if(isSuspended){
            //如果是挂起，可以执行激活操作
            repositoryService.activateProcessDefinitionById(
                    processDefinitionId,
                    true,
                    null);
            System.out.println(String.format("流程id：%s已激活", processDefinitionId));
        }else{
            //如果是激活，可以执行挂起操作
            repositoryService.suspendProcessDefinitionById(
                    processDefinitionId,
                    true,
                    null);
            System.out.println(String.format("流程id：%s已挂起", processDefinitionId));
        }

    }

}