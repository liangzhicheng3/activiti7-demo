package com.liangzhicheng.test;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * 主干流程
 */
public class Demo1 {

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
         *     2.加载流程文件->leave.png
         *
         * 对应数据库表：act_re_deployment
         */
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave.bpmn")
                .addClasspathResource("bpmn/leave.png")
                .name("请假流程")
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
        //流程启动之后，返回流程实例（key：在制定流程图bpmn时候设置）
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveProcess");
        //输出流程实例信息
        System.out.println(String.format("流程定义id：%s", processInstance.getProcessDefinitionId()));
        System.out.println(String.format("流程实例id：%s", processInstance.getId()));
    }

    /**
     * 查询指定用户将要执行审批任务
     */
    @Test
    public void testSelectTaskList(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //当前要获取指定用户审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        //自定义sql查询
//        taskService.createNativeTaskQuery().sql("");
        //使用activiti7提供面向对象方式查询数据库
        List<Task> taskList = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess")
                //查询指定负责人员
                .taskAssignee("lisi")
                .list();
        if(taskList != null && taskList.size() > 0){
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
                .processDefinitionKey("leaveProcess")
                //查询指定负责人员
                .taskAssignee("lisi")
                .list();
        if(taskList != null && taskList.size() > 0){
            //处理任务、完成任务、审批任务
            for(Iterator<Task> tasks = taskList.iterator(); tasks.hasNext();){
                taskService.complete(tasks.next().getId());
            }
        }
    }

    /**
     * 处理任务时，添加审批意见
     */
    @Test
    public void testAddComment(){
        //任务负责人员
        String assignee = "zhangsan";
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //当前要获取指定用户审批任务，使用TaskService
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                //查询指定流程key
                .processDefinitionKey("leaveProcess")
                //查询指定负责人员
                .taskAssignee(assignee)
                .list();
        if(taskList != null && taskList.size() > 0){
            for(Iterator<Task> tasks = taskList.iterator(); tasks.hasNext();){
                Task task = tasks.next();
                //添加审批意见
                taskService.addComment(
                        task.getId(),
                        task.getProcessInstanceId(),
                        String.format("%s表示同意", assignee));
                taskService.complete(task.getId());
            }
        }
    }

    /**
     * 查询历史审批任务
     */
    @Test
    public void testSelectHistoryTaskList(){
        //获取activiti7的流程引擎
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取历史任务，使用HistoryService
        HistoryService historyService = processEngine.getHistoryService();
        TaskService taskService = processEngine.getTaskService();
        //历史节点实例信息对象，对应数据库表：act_hi_actinst
        List<HistoricActivityInstance> historyList = historyService.createHistoricActivityInstanceQuery()
                //只查询指定实例id
                .processInstanceId("2501")
                //只查询审批节点信息
                .activityType("userTask")
                //只查询指定审批人员
                .taskAssignee("zhangsan")
                //通过sql语句查看，只查询end_time不为空（表示审批任务整体流程已完成）
                .finished()
                .list();
        if(historyList != null && historyList.size() > 0){
            for(Iterator<HistoricActivityInstance> historyIterator =
                historyList.iterator(); historyIterator.hasNext();){
                HistoricActivityInstance history = historyIterator.next();
                System.out.println(String.format("任务名称：%s", history.getActivityName()));
                System.out.println(String.format("任务开始时间：%s", history.getStartTime()));
                System.out.println(String.format("任务结束时间%s", history.getEndTime()));
                System.out.println(String.format("任务耗时时间：%s", history.getDurationInMillis()));
                /**
                 * 获取审批意见信息
                 *
                 * Comment出现过时，建议自己创建Comment表
                 */
                List<Comment> commentList = taskService.getTaskComments(history.getTaskId());
                if(commentList != null && commentList.size() > 0){
                    System.out.println(String.format("审批意见：%s", commentList.get(0).getFullMessage()));
                }
            }
        }
    }

}