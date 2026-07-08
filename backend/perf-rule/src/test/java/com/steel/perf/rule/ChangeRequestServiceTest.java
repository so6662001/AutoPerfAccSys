package com.steel.perf.rule;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.rule.model.ApprovalStep;
import com.steel.perf.rule.model.ChangeRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChangeRequestServiceTest {

    private final ChangeRequestService svc = new ChangeRequestService();

    private ChangeRequest newCr(ChangeRequest.EffMode mode, String scope) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId("CR-1");
        cr.setVer(4);
        cr.setSubmitter("李静");
        cr.setEffMode(mode);
        cr.setScopeLabel(scope);
        cr.setStatus(ChangeRequest.Status.PENDING);
        cr.setChain(svc.defaultChain());
        cr.setDetails(new ArrayList<>(List.of("方案变更")));
        cr.setParams(Map.of("销售权重", "40/20/20/20"));
        return cr;
    }

    @Test
    void threeLevelSignThenEffective() {
        ChangeRequest cr = newCr(ChangeRequest.EffMode.NOW, "全公司");
        svc.approve(cr, "陈组长", "t1");
        assertEquals(ChangeRequest.Status.PENDING, cr.getStatus());
        svc.approve(cr, "张经理", "t2");   // HR 或签，张经理为候选
        assertEquals(ChangeRequest.Status.PENDING, cr.getStatus());
        svc.approve(cr, "周总", "t3");
        assertEquals(ChangeRequest.Status.EFFECTIVE, cr.getStatus());
    }

    @Test
    void scheduledThenGoEffective() {
        ChangeRequest cr = newCr(ChangeRequest.EffMode.SCHEDULED, "全公司");
        svc.approve(cr, "陈组长", "t1");
        svc.approve(cr, "王总监", "t2");
        svc.approve(cr, "周总", "t3");
        assertEquals(ChangeRequest.Status.SCHEDULED, cr.getStatus());
        svc.goEffective(cr);
        assertEquals(ChangeRequest.Status.EFFECTIVE, cr.getStatus());
    }

    @Test
    void rejectStopsFlow() {
        ChangeRequest cr = newCr(ChangeRequest.EffMode.NOW, "全公司");
        svc.approve(cr, "陈组长", "t1");
        svc.reject(cr, "t2");
        assertEquals(ChangeRequest.Status.REJECTED, cr.getStatus());
        assertThrows(BizException.class, () -> svc.approve(cr, "周总", "t3"));
    }

    @Test
    void orSignRejectsNonCandidate() {
        ChangeRequest cr = newCr(ChangeRequest.EffMode.NOW, "全公司");
        svc.approve(cr, "陈组长", "t1");
        assertThrows(BizException.class, () -> svc.approve(cr, "路人甲", "t2"));
    }

    @Test
    void delegateAndAddSign() {
        ChangeRequest cr = newCr(ChangeRequest.EffMode.NOW, "全公司");
        svc.approve(cr, "陈组长", "t1");
        // 当前为 HR 节点，委托给李副经理
        svc.delegate(cr, "李副经理");
        assertEquals("李副经理", cr.getChain().get(1).getName());
        assertEquals("王总监", cr.getChain().get(1).getDelegatedFrom());
        // 加签：在 HR 之后插入法务
        int before = cr.getChain().size();
        svc.addSign(cr, "法务", "孙顾问");
        assertEquals(before + 1, cr.getChain().size());
        assertTrue(cr.getChain().get(2).isAdded());
        // 继续走完：HR(李副经理·或签候选? 李副经理非候选 -> 用候选审批)。这里 HR 或签仍需候选：
        svc.approve(cr, "王总监", "t2"); // 或签候选之一
        svc.approve(cr, "孙顾问", "t3"); // 加签(SINGLE)
        svc.approve(cr, "周总", "t4");
        assertEquals(ChangeRequest.Status.EFFECTIVE, cr.getStatus());
    }

    @Test
    void grayPromoteToFull() {
        ChangeRequest gray = newCr(ChangeRequest.EffMode.NOW, "灰度·超扑越销售部");
        svc.approve(gray, "陈组长", "t1");
        svc.approve(gray, "王总监", "t2");
        svc.approve(gray, "周总", "t3");
        assertEquals(ChangeRequest.Status.EFFECTIVE, gray.getStatus());
        ChangeRequest full = svc.promote(gray, "CR-2", 5);
        assertEquals("全公司", full.getScopeLabel());
        assertEquals(ChangeRequest.Status.PENDING, full.getStatus());
        assertTrue(full.getDetails().get(0).contains("推广至全公司"));
    }
}
