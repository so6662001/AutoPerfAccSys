package com.steel.perf.rule.model;

import java.util.List;

/**
 * 审批链节点（支持或签/委托/加签）。
 */
public class ApprovalStep {

    public enum StepStatus { WAITING, PASSED, REJECTED }

    public enum SignMode { SINGLE, OR_SIGN }

    private String role;
    private String name;
    private SignMode mode = SignMode.SINGLE;
    private List<String> candidates;   // 或签候选
    private StepStatus status = StepStatus.WAITING;
    private String time;
    private String delegatedFrom;      // 委托：原审批人
    private boolean added;             // 加签：动态插入

    public ApprovalStep() {
    }

    public ApprovalStep(String role, String name) {
        this.role = role;
        this.name = name;
    }

    public ApprovalStep orSign(List<String> candidates) {
        this.mode = SignMode.OR_SIGN;
        this.candidates = candidates;
        return this;
    }

    public ApprovalStep asAdded() {
        this.added = true;
        return this;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SignMode getMode() { return mode; }
    public void setMode(SignMode mode) { this.mode = mode; }
    public List<String> getCandidates() { return candidates; }
    public void setCandidates(List<String> candidates) { this.candidates = candidates; }
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDelegatedFrom() { return delegatedFrom; }
    public void setDelegatedFrom(String delegatedFrom) { this.delegatedFrom = delegatedFrom; }
    public boolean isAdded() { return added; }
    public void setAdded(boolean added) { this.added = added; }
}
