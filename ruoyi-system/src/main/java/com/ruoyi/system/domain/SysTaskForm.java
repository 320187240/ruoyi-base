package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 流程任务关联单对象 sys_task_form
 * 
 * @author Tony
 * @date 2021-04-03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysTaskForm extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 表单主键 */
    @Excel(name = "表单主键")
    private Long formId;

    /** 所属任务 */
    @Excel(name = "所属任务")
    private String taskId;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("formId", getFormId())
            .append("taskId", getTaskId())
            .toString();
    }
}
