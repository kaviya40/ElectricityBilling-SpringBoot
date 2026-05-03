// ── Sidebar ──────────────────────────────────────────────────────
function toggleSidebar() {
  const s = document.getElementById('sidebar');
  const l = document.getElementById('layout');
  if (window.innerWidth <= 768) {
    s.classList.toggle('open');
  } else {
    const collapsed = s.style.width === '60px';
    s.style.width = collapsed ? '240px' : '60px';
    s.querySelectorAll('span').forEach(el => el.style.display = collapsed ? '' : 'none');
  }
}

// ── Dark Mode ─────────────────────────────────────────────────────
function toggleDark() {
  document.body.classList.toggle('dark');
  localStorage.setItem('ebms-dark', document.body.classList.contains('dark'));
}

// Persist dark mode
(function () {
  if (localStorage.getItem('ebms-dark') === 'true') document.body.classList.add('dark');
})();

// ── Auto-dismiss alerts ───────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert').forEach(el => {
    setTimeout(() => { el.style.transition = 'opacity .5s'; el.style.opacity = '0';
      setTimeout(() => el.remove(), 500); }, 4000);
  });
});
