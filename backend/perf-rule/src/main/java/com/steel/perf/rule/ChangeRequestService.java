package com.steel.perf.rule;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.rule.model.ApprovalStep;
import com.steel.perf.rule.model.ChangeRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则变更单生命周期服务（M3）。
 * 三级串签（部门主管 → HR负责人[或签] → 分管领导），支持委托/加签；
 * 全部通过后按生效方式立即生效或进入待生效；支持退回、到期生效、灰度转全量推广。
 */
public class ChangeRequestService {

    /** 默认三级串签链（HR 节点或签）。 */
    public List<ApprovalStep> defaultChain() {
        List<ApprovalStep> chain = new ArrayList<>();
        chain.add(new ApprovalStep("部门主管", "陈组长"));
        chain.add(new ApprovalStep("HR负责人", "王总监").orSign(List.of("王总监", "张经理")));
        chain.add(new ApprovalStep("分管领导", "周总"));
        return chain;
    }

    /**
     * 审批通过当前节点（推进串签）。
     *
     * @param approverName 审批人（或签节点须为候选之一）
     */
    public void approve(ChangeRequest cr, String approverName, String time) {
        requirePending(cr);
        ApprovalStep step = cr.getChain().get(cr.getCurStep());
        if (step.getMode() == ApprovalStep.SignMode.OR_SIGN
                && (step.getCandidates() == null || !step.getCandidates().contains(approverName))) {
            throw new BizException("或签节点审批人非候选之一: " + approverName);
        }
        step.setStatus(ApprovalStep.StepStatus.PASSED);
        step.setTime(time);
        cr.setCurStep(cr.getCurStep() + 1);
        if (cr.getCurStep() >= cr.getChain().size()) {
            // 全部通过
            if (cr.getEffMode() == ChangeRequest.EffMode.NOW) {
                cr.setStatus(ChangeRequest.Status.EFFECTIVE);
            } else {
                cr.setStatus(ChangeRequest.Status.SCHEDULED);
            }
        }
    }

    public void reject(ChangeRequest cr, String time) {
        requirePending(cr);
        ApprovalStep step = cr.getChain().get(cr.getCurStep());
        step.setStatus(ApprovalStep.StepStatus.REJECTED);
        step.setTime(time);
        cr.setStatus(ChangeRequest.Status.REJECTED);
    }

    /** 委托代理：当前节点换审批人（留痕原审批人）。 */
    public void delegate(ChangeRequest cr, String newApprover) {
        requirePending(cr);
        ApprovalStep step = cr.getChain().get(cr.getCurStep());
        step.setDelegatedFrom(step.getName());
        step.setName(newApprover);
    }

    /** 加签：在当前节点之后插入一个审批人。 */
    public void addSign(ChangeRequest cr, String role, String name) {
        requirePending(cr);
        cr.getChain().add(cr.getCurStep() + 1, new ApprovalStep(role, name).asAdded());
    }

    /** 到期生效（待生效 -> 已生效）。 */
    public void goEffective(ChangeRequest cr) {
        if (cr.getStatus() != ChangeRequest.Status.SCHEDULED) {
            throw new BizException("仅待生效的变更单可到期生效");
        }
        cr.setStatus(ChangeRequest.Status.EFFECTIVE);
    }

    /** 灰度转全量推广：基于已生效的灰度变更单生成全公司推广变更单（重新串签）。 */
    public ChangeRequest promote(ChangeRequest gray, String newId, int newVer) {
        if (gray.getStatus() != ChangeRequest.Status.EFFECTIVE || !gray.isGray()) {
            throw new BizException("仅已生效的灰度试点变更单可推广至全公司");
        }
        ChangeRequest cr = new ChangeRequest();
        cr.setId(newId);
        cr.setVer(newVer);
        cr.setSubmitter(gray.getSubmitter());
        cr.setEffMode(ChangeRequest.EffMode.NOW);
        cr.setEffLabel("立即生效");
        cr.setScopeLabel("全公司");
        cr.setStatus(ChangeRequest.Status.PENDING);
        cr.setChain(defaultChain());
        List<String> details = new ArrayList<>();
        details.add("由灰度试点 " + gray.getId() + "（" + gray.getScopeLabel() + "）推广至全公司");
        if (gray.getDetails() != null) {
            details.addAll(gray.getDetails());
        }
        cr.setDetails(details);
        cr.setParams(gray.getParams());
        return cr;
    }

    private void requirePending(ChangeRequest cr) {
        if (cr.getStatus() != ChangeRequest.Status.PENDING) {
            throw new BizException("变更单当前状态不可操作: " + cr.getStatus());
        }
        if (cr.getChain() == null || cr.getCurStep() >= cr.getChain().size()) {
            throw new BizException("审批链已结束");
        }
    }
}
