package com.steel.perf.rule.model;

import java.util.List;
import java.util.Map;

/**
 * 规则变更单。
 */
public class ChangeRequest {

    public enum Status { PENDING, SCHEDULED, EFFECTIVE, REJECTED }

    public enum EffMode { NOW, SCHEDULED }

    private String id;
    private int ver;
    private String submitter;
    private EffMode effMode = EffMode.NOW;
    private String effLabel = "立即生效";
    private String scopeLabel = "全公司";
    private Status status = Status.PENDING;
    private int curStep;
    private List<ApprovalStep> chain;
    private List<String> details;
    private Map<String, String> params;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getVer() { return ver; }
    public void setVer(int ver) { this.ver = ver; }
    public String getSubmitter() { return submitter; }
    public void setSubmitter(String submitter) { this.submitter = submitter; }
    public EffMode getEffMode() { return effMode; }
    public void setEffMode(EffMode effMode) { this.effMode = effMode; }
    public String getEffLabel() { return effLabel; }
    public void setEffLabel(String effLabel) { this.effLabel = effLabel; }
    public String getScopeLabel() { return scopeLabel; }
    public void setScopeLabel(String scopeLabel) { this.scopeLabel = scopeLabel; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getCurStep() { return curStep; }
    public void setCurStep(int curStep) { this.curStep = curStep; }
    public List<ApprovalStep> getChain() { return chain; }
    public void setChain(List<ApprovalStep> chain) { this.chain = chain; }
    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params; }

    public boolean isGray() {
        return scopeLabel != null && scopeLabel.startsWith("灰度");
    }
}
