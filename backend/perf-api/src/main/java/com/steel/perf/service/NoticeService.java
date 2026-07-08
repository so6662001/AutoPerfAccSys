package com.steel.perf.service;

import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.jpa.NoticeEntity;
import com.steel.perf.repo.jpa.NoticeJpaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** 员工端通知服务（租户广播）。 */
@Service
public class NoticeService {

    private final NoticeJpaRepo repo;

    public NoticeService(NoticeJpaRepo repo) {
        this.repo = repo;
    }

    public void push(String title, String body, String crId) {
        NoticeEntity e = new NoticeEntity();
        e.setTenantId(TenantContext.requireTenantId());
        e.setTitle(title);
        e.setBody(body);
        e.setCrId(crId);
        e.setCreatedAt(LocalDateTime.now());
        repo.save(e);
    }

    public List<NoticeEntity> list() {
        return repo.findByTenantId(TenantContext.requireTenantId(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
