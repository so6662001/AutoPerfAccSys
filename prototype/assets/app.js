/* 共享导航 / 顶栏渲染 + 通用交互 */
(function () {
  const NAV = [
    { title: "概览", items: [
      { id: "dashboard", label: "管理驾驶舱", icon: "▦", href: "dashboard.html" },
    ]},
    { title: "绩效核算", items: [
      { id: "calc", label: "核算工作台", icon: "⚙", href: "calc-workbench.html" },
      { id: "payslip", label: "个人绩效单", icon: "🧾", href: "payslip.html" },
      { id: "approval", label: "审批流", icon: "✅", href: "approval.html" },
      { id: "appeal", label: "申诉复核", icon: "⚖", href: "appeal.html" },
      { id: "simulator", label: "绩效试算器", icon: "🧮", href: "simulator.html" },
      { id: "sandbox", label: "政策沙盘对比", icon: "⚖", href: "sandbox.html" },
    ]},
    { title: "绩效看板", items: [
      { id: "purchase", label: "采购绩效看板", icon: "🛒", href: "purchase-board.html" },
      { id: "sales", label: "销售绩效看板", icon: "📈", href: "sales-board.html" },
    ]},
    { title: "数据与规则", items: [
      { id: "data", label: "数据集成中心", icon: "🔗", href: "data-integration.html" },
      { id: "market", label: "行情价与竞争力", icon: "💹", href: "market-price.html" },
      { id: "rule", label: "绩效规则配置", icon: "🧩", href: "rule-config.html" },
    ]},
    { title: "员工端", items: [
      { id: "mobile", label: "移动端绩效", icon: "📱", href: "mobile.html" },
    ]},
  ];

  function renderShell() {
    const page = document.body.getAttribute("data-page");
    const crumb = document.body.getAttribute("data-crumb") || "";
    const period = document.body.getAttribute("data-period") || "2026年6月（核算中）";

    const navHtml = NAV.map(g => `
      <div class="nav-group">
        <div class="g-title">${g.title}</div>
        ${g.items.map(it => `
          <a href="${it.href}" class="${it.id === page ? "active" : ""}">
            <span class="ic">${it.icon}</span><span>${it.label}</span>
          </a>`).join("")}
      </div>`).join("");

    const sidebar = document.createElement("aside");
    sidebar.className = "sidebar";
    sidebar.innerHTML = `
      <div class="brand">
        <div class="logo">钢</div>
        <div>
          <div class="t1">钢绩云</div>
          <div class="t2">STEEL PERFORMANCE</div>
        </div>
      </div>
      <nav class="nav">${navHtml}</nav>
      <div class="sb-foot">
        <div class="av">李</div>
        <div>
          <div class="nm">李静 · 绩效专员</div>
          <div class="rl">HR 中心</div>
        </div>
      </div>`;

    const main = document.querySelector(".main");
    const topbar = document.createElement("header");
    topbar.className = "topbar";
    topbar.innerHTML = `
      <div class="crumb">${crumb}</div>
      <div class="spacer"></div>
      <span class="period-pill">📅 核算周期：${period}</span>
      <div class="tb-ic">🔔</div>
      <div class="tb-ic">❔</div>`;

    document.body.insertBefore(sidebar, document.body.firstChild);
    main.insertBefore(topbar, main.firstChild);
  }

  function bindTabs() {
    document.querySelectorAll("[data-tabs]").forEach(group => {
      const btns = group.querySelectorAll(".tabs button");
      const panes = document.querySelectorAll(`[data-tabgroup="${group.getAttribute("data-tabs")}"]`);
      btns.forEach(btn => btn.addEventListener("click", () => {
        btns.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        panes.forEach(p => p.classList.toggle("active", p.getAttribute("data-tab") === btn.getAttribute("data-tab")));
      }));
    });
  }

  function bindDrill() {
    document.querySelectorAll(".drill .row[data-target]").forEach(row => {
      row.addEventListener("click", () => {
        row.classList.toggle("open");
        const t = document.getElementById(row.getAttribute("data-target"));
        if (t) t.classList.toggle("show");
      });
    });
  }

  function bindSwitch() {
    document.querySelectorAll(".sw").forEach(s => s.addEventListener("click", () => s.classList.toggle("on")));
  }

  window.showToast = function (title, msg, type) {
    let wrap = document.querySelector(".toast-wrap");
    if (!wrap) { wrap = document.createElement("div"); wrap.className = "toast-wrap"; document.body.appendChild(wrap); }
    const ic = { green: "✅", red: "⛔", amber: "⚠", blue: "ℹ" }[type] || "ℹ";
    const t = document.createElement("div");
    t.className = "toast " + (type || "");
    t.innerHTML = `<span class="ic2">${ic}</span><div class="tx"><b>${title}</b>${msg ? `<span>${msg}</span>` : ""}</div>`;
    wrap.appendChild(t);
    setTimeout(() => { t.style.animation = "fadeOut .3s forwards"; setTimeout(() => t.remove(), 320); }, 3000);
  };

  document.addEventListener("DOMContentLoaded", () => {
    renderShell();
    bindTabs();
    bindDrill();
    bindSwitch();
  });
})();
