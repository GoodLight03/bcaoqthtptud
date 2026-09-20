const API = '/api/reports';

const CANON_KY = [
  {key:'stt', top:'STT', sub:''},
  {key:'unit', top:'Đơn vị giải quyết TTHC', sub:''},
  {key:'recv_total', top:'Số hồ sơ tiếp nhận', sub:'Tổng số'},
  {key:'recv_direct', top:'Số hồ sơ tiếp nhận', sub:'Tiếp nhận trực tiếp'},
  {key:'recv_online_full', top:'Số hồ sơ tiếp nhận', sub:'Tiếp nhận trực tuyến toàn trình'},
  {key:'recv_online_partial', top:'Số hồ sơ tiếp nhận', sub:'Tiếp nhận trực tuyến một phần'},
  {key:'recv_online_other', top:'Số hồ sơ tiếp nhận', sub:'Tiếp nhận trực tuyến hình thức khác'},
  {key:'recv_non_local', top:'Số hồ sơ tiếp nhận', sub:'Tiếp nhận phi địa giới'},
  {key:'recv_carry', top:'Số hồ sơ tiếp nhận', sub:'Kỳ trước chuyển qua'},
  {key:'recv_postal', top:'Số hồ sơ tiếp nhận', sub:'Bưu chính / BCCI'},
  {key:'res_total', top:'Số hồ sơ đã giải quyết', sub:'Tổng số'},
  {key:'res_ontime', top:'Số hồ sơ đã giải quyết', sub:'Đúng và trước hạn'},
  {key:'res_late_sorry', top:'Số hồ sơ đã giải quyết', sub:'Quá hạn (đã có thư xin lỗi)'},
  {key:'res_late_no_sorry', top:'Số hồ sơ đã giải quyết', sub:'Quá hạn (chưa có thư xin lỗi)'},
  {key:'res_late', top:'Số hồ sơ đã giải quyết', sub:'Quá hạn (gộp)'},
  {key:'proc_total', top:'Số hồ sơ đang giải quyết', sub:'Tổng số'},
  {key:'proc_ontime', top:'Số hồ sơ đang giải quyết', sub:'Chưa đến hạn'},
  {key:'proc_late_sorry', top:'Số hồ sơ đang giải quyết', sub:'Quá hạn (đã có thư xin lỗi)'},
  {key:'proc_late_no_sorry', top:'Số hồ sơ đang giải quyết', sub:'Quá hạn (chưa có thư xin lỗi)'},
  {key:'proc_late', top:'Số hồ sơ đang giải quyết', sub:'Quá hạn (gộp)'},
  {key:'pay_total', top:'Thanh toán hồ sơ', sub:'Tổng số'},
  {key:'pay_online', top:'Thanh toán hồ sơ', sub:'Trực tuyến'},
  {key:'pay_direct', top:'Thanh toán hồ sơ', sub:'Trực tiếp'},
  {key:'stopped', top:'Hồ sơ dừng xử lý', sub:''},
  {key:'pending_ontime', top:'Hồ sơ TT chờ tiếp nhận', sub:'Đúng hạn'},
  {key:'pending_late', top:'Hồ sơ TT chờ tiếp nhận', sub:'Quá hạn'},
  {key:'pending_receive', top:'Hồ sơ TT chờ tiếp nhận', sub:'Tổng (nếu không tách)'},
  {key:'rejected', top:'Hồ sơ không được tiếp nhận', sub:''},
  {key:'mismatch_digital', top:'HS kết thúc nhưng KQ điện tử ≠ giấy', sub:''},
  {key:'receipt_money', top:'HS thu tiền có xuất biên lai', sub:''},
  {key:'dn_verified_rate', top:'Tỷ lệ DN xác thực QLVBDN', sub:''},
  {key:'withdrawn', top:'Hồ sơ rút', sub:''},
  {key:'sources', top:'Nguồn file', sub:''}
];

const CANON_TUAN = [
  {key:'stt', top:'STT', sub:''},
  {key:'unit', top:'Đơn vị', sub:''},
  {key:'recv_total', top:'Số lượng hồ sơ tiếp nhận', sub:'Tổng số'},
  {key:'recv_online_full', top:'Số lượng hồ sơ tiếp nhận', sub:'Trực tuyến toàn trình'},
  {key:'recv_online_partial', top:'Số lượng hồ sơ tiếp nhận', sub:'Trực tuyến một phần'},
  {key:'recv_postal', top:'Số lượng hồ sơ tiếp nhận', sub:'Qua dịch vụ bưu chính'},
  {key:'recv_direct', top:'Số lượng hồ sơ tiếp nhận', sub:'Trực tiếp'},
  {key:'recv_carry', top:'Số lượng hồ sơ tiếp nhận', sub:'Từ kỳ trước'},
  {key:'res_total', top:'Số lượng hồ sơ đã giải quyết', sub:'Tổng số'},
  {key:'res_before', top:'Số lượng hồ sơ đã giải quyết', sub:'Trước hạn'},
  {key:'res_ontime', top:'Số lượng hồ sơ đã giải quyết', sub:'Đúng hạn'},
  {key:'res_late', top:'Số lượng hồ sơ đã giải quyết', sub:'Quá hạn'},
  {key:'proc_total', top:'Số lượng hồ sơ đang giải quyết', sub:'Tổng số'},
  {key:'proc_ontime', top:'Số lượng hồ sơ đang giải quyết', sub:'Trong hạn'},
  {key:'proc_late', top:'Số lượng hồ sơ đang giải quyết', sub:'Quá hạn'},
  {key:'sources', top:'Nguồn file', sub:''}
];

let parsedFiles = [];
let mergedRows = [];
let lastTotals = {};
let renameTargetId = null;


/** Modal xác nhận tùy chỉnh (thay confirm() mặc định) */
function showConfirm(title, message, okLabel) {
  return new Promise(function(resolve) {
    document.getElementById('confirmTitle').textContent = title || 'Xác nhận';
    document.getElementById('confirmMessage').innerHTML = message || '';
    const okBtn = document.getElementById('confirmOk');
    okBtn.textContent = okLabel || 'Xác nhận';
    const modal = document.getElementById('confirmModal');
    modal.classList.add('show');

    function cleanup(result) {
      modal.classList.remove('show');
      okBtn.onclick = null;
      document.getElementById('confirmCancel').onclick = null;
      document.getElementById('closeConfirm').onclick = null;
      modal.onclick = null;
      resolve(result);
    }
    okBtn.onclick = function() { cleanup(true); };
    document.getElementById('confirmCancel').onclick = function() { cleanup(false); };
    document.getElementById('closeConfirm').onclick = function() { cleanup(false); };
    modal.onclick = function(e) {
      if (e.target.id === 'confirmModal') cleanup(false);
    };
  });
}


function toast(msg, type) {
  type = type || 'info';
  const wrap = document.getElementById('toastWrap');
  const el = document.createElement('div');
  el.className = 'toast ' + type;
  el.textContent = msg;
  wrap.appendChild(el);
  setTimeout(function() {
    el.style.opacity = '0';
    el.style.transition = 'opacity .3s';
    setTimeout(function() { el.remove(); }, 300);
  }, 3800);
}

function getCanon(forceType) {
  const t = forceType || (document.querySelector('input[name="reportType"]:checked') || {}).value || 'ky';
  return t === 'tuan' ? CANON_TUAN : CANON_KY;
}
function isTuan(forceType) {
  const t = forceType || (document.querySelector('input[name="reportType"]:checked') || {}).value || 'ky';
  return t === 'tuan';
}
function fieldVal(r, key, forceType) {
  const n = r.numbers || r;
  if (key === 'res_ontime' && !isTuan(forceType)) {
    const a = Number(n.res_ontime) || 0;
    const b = Number(n.res_before) || 0;
    const s = a + b;
    return s || null;
  }
  return n[key] != null ? n[key] : null;
}
function sumKey(key, rows, forceType) {
  const list = rows || mergedRows;
  return list.reduce(function(a, r) {
    const v = fieldVal(r, key, forceType);
    return a + (typeof v === 'number' ? v : 0);
  }, 0);
}
function fmt(n) {
  if (n == null || n === '' || isNaN(n)) return '';
  return Number(n).toLocaleString('vi-VN');
}
function esc(s) {
  return String(s == null ? '' : s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
function sourcesOf(r) {
  if (Array.isArray(r.sources)) return r.sources.join('; ');
  if (typeof r.sources === 'string') return r.sources;
  return '';
}

function buildFullTableHtml(rows, forceType) {
  const list = rows || mergedRows;
  const CANON = getCanon(forceType);
  let top = '<tr>', sub = '<tr class="sub">', i = 0;
  while (i < CANON.length) {
    const t = CANON[i].top;
    let span = 1;
    while (i + span < CANON.length && CANON[i + span].top === t) span++;
    if (span === 1 && !CANON[i].sub) {
      top += '<th rowspan="2">' + esc(t) + '</th>';
    } else {
      top += '<th colspan="' + span + '">' + esc(t) + '</th>';
      for (let k = 0; k < span; k++) sub += '<th>' + esc(CANON[i + k].sub || '') + '</th>';
    }
    i += span;
  }
  top += '</tr>'; sub += '</tr>';

  const body = list.map(function(r) {
    return '<tr>' + CANON.map(function(c) {
      if (c.key === 'stt') return '<td>' + esc(r.stt) + '</td>';
      if (c.key === 'unit') return '<td>' + esc(r.unit) + '</td>';
      if (c.key === 'sources') return '<td style="font-size:11px;color:#667">' + esc(sourcesOf(r)) + '</td>';
      const v = fieldVal(r, c.key, forceType);
      return '<td class="num">' + (v ? fmt(v) : '') + '</td>';
    }).join('') + '</tr>';
  }).join('');

  const total = '<tr class="total">' + CANON.map(function(c) {
    if (c.key === 'stt') return '<td></td>';
    if (c.key === 'unit') return '<td>TỔNG CỘNG</td>';
    if (c.key === 'sources') return '<td></td>';
    return '<td class="num">' + fmt(sumKey(c.key, list, forceType)) + '</td>';
  }).join('') + '</tr>';

  return '<table><thead>' + top + sub + '</thead><tbody>' + body + total + '</tbody></table>';
}

function defaultReportName() {
  const t = isTuan() ? 'Tuần' : 'Kỳ';
  const d = new Date();
  const pad = function(n) { return String(n).padStart(2,'0'); };
  return 'Báo cáo ' + t + ' — ' + pad(d.getDate()) + '/' + pad(d.getMonth()+1) + '/' + d.getFullYear() + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
}

const drop = document.getElementById('drop');
const fileInput = document.getElementById('fileInput');
drop.onclick = function() { fileInput.click(); };
['dragenter','dragover'].forEach(function(e) {
  drop.addEventListener(e, function(ev) { ev.preventDefault(); drop.classList.add('drag'); });
});
['dragleave','drop'].forEach(function(e) {
  drop.addEventListener(e, function(ev) { ev.preventDefault(); drop.classList.remove('drag'); });
});
drop.addEventListener('drop', function(e) { handleFiles(e.dataTransfer.files); });
fileInput.addEventListener('change', function(e) { handleFiles(e.target.files); fileInput.value = ''; });

async function handleFiles(list) {
  const files = Array.from(list).filter(function(f) { return /\.(xlsx|xls)$/i.test(f.name); });
  if (!files.length) { toast('Không có file .xlsx/.xls hợp lệ', 'warn'); return; }
  toast('Đang đọc ' + files.length + ' file...', 'info');
  const fd = new FormData();
  files.forEach(function(f) { fd.append('files', f); });
  try {
    const res = await fetch(API + '/parse', { method: 'POST', body: fd });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    parsedFiles = parsedFiles.concat(data);
    renderFileTable();
    const ok = data.filter(function(f) { return !f.error && f.numericRowCount > 0; }).length;
    const bad = data.length - ok;
    if (ok) toast('Đã đọc ' + ok + ' file thành công' + (bad ? ', ' + bad + ' file lỗi/không có số' : ''), bad ? 'warn' : 'ok');
    else toast('Không đọc được số liệu từ file đã chọn', 'err');
  } catch (e) {
    toast('Lỗi đọc file: ' + e.message, 'err');
  }
}

function renderFileTable() {
  const table = document.getElementById('fileTable');
  const body = document.getElementById('fileTableBody');
  const warns = document.getElementById('fileWarnings');
  table.style.display = parsedFiles.length ? 'table' : 'none';
  body.innerHTML = '';
  const w = [];
  parsedFiles.forEach(function(f, i) {
    const tr = document.createElement('tr');
    tr.innerHTML = '<td>' + (i+1) + '</td><td>' + esc(f.name) + '</td>' +
      '<td><span class="tag ' + (f.familyCode||'unknown') + '">' + esc(f.familyLabel||'') + '</span></td>' +
      '<td>' + (f.numericRowCount||0) + '</td><td>' + esc(f.sheetName||'') + '</td>' +
      '<td><button class="act-btn del" data-i="' + i + '" title="Xoá file">🗑 Xoá</button></td>';
    body.appendChild(tr);
    if (f.error) w.push('<div class="bad-box"><b>' + esc(f.name) + ':</b> ' + esc(f.error) + '</div>');
    else if (!f.numericRowCount) w.push('<div class="warn-box"><b>' + esc(f.name) + ':</b> không đọc được dòng có số.</div>');
  });
  warns.innerHTML = w.join('');
  body.querySelectorAll('button[data-i]').forEach(function(btn) {
    btn.onclick = function() {
      const name = (parsedFiles[+btn.dataset.i] || {}).name || '';
      parsedFiles.splice(+btn.dataset.i, 1);
      renderFileTable();
      toast('Đã xoá file: ' + name, 'info');
    };
  });
  document.getElementById('mergeBtn').disabled = !parsedFiles.length;
  document.getElementById('clearBtn').disabled = !parsedFiles.length;
}

document.getElementById('clearBtn').onclick = function() {
  parsedFiles = []; mergedRows = []; lastTotals = {};
  renderFileTable();
  document.getElementById('resultPanel').style.display = 'none';
  document.getElementById('reportTypePanel').style.display = 'none';
  toast('Đã xoá tất cả file', 'info');
};

document.getElementById('mergeBtn').onclick = async function() {
  const allRows = [];
  parsedFiles.forEach(function(f) { (f.rows || []).forEach(function(r) { allRows.push(r); }); });
  if (!allRows.length) { toast('Không có dòng dữ liệu để tổng hợp', 'warn'); return; }
  const reportType = (document.querySelector('input[name="reportType"]:checked') || {}).value || 'ky';
  toast('Đang tổng hợp theo đơn vị...', 'info');
  try {
    const res = await fetch(API + '/merge', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reportType: reportType, save: false, fileNames: parsedFiles.map(function(f){return f.name;}), rows: allRows })
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    mergedRows = data.rows || [];
    lastTotals = data.totals || {};
    renderResult(data);
    toast('Tổng hợp xong: ' + data.unitCount + ' đơn vị', 'ok');
  } catch (e) {
    toast('Lỗi tổng hợp: ' + e.message, 'err');
  }
};

function renderResult(data) {
  document.getElementById('resultPanel').style.display = 'block';
  document.getElementById('reportTypePanel').style.display = 'block';
  const tuan = isTuan();
  const online = (data.totals && data.totals.recv_online_full || 0) + (data.totals && data.totals.recv_online_partial || 0);
  const direct = (data.totals && data.totals.recv_direct) || 0;
  document.getElementById('statGrid').innerHTML =
    '<div class="stat"><div class="n">' + parsedFiles.length + '</div><div class="l">File nguồn</div></div>' +
    '<div class="stat"><div class="n">' + data.unitCount + '</div><div class="l">Đơn vị đã gộp</div></div>' +
    '<div class="stat"><div class="n">' + fmt(data.totals && data.totals.recv_total) + '</div><div class="l">Tiếp nhận</div></div>' +
    '<div class="stat"><div class="n">' + (tuan ? fmt(online) : fmt(data.totals && data.totals.res_total)) + '</div><div class="l">' + (tuan ? 'Trực tuyến' : 'Đã giải quyết') + '</div></div>' +
    '<div class="stat"><div class="n">' + (tuan ? fmt(direct) : fmt(data.totals && data.totals.proc_total)) + '</div><div class="l">' + (tuan ? 'Trực tiếp' : 'Đang giải quyết') + '</div></div>';
  document.getElementById('colNote').textContent = 'Kiểm tra kỹ trước khi Tải / Lưu báo cáo!';
}

document.querySelectorAll('input[name="reportType"]').forEach(function(r) {
  r.addEventListener('change', function() {
    if (mergedRows.length) {
      toast('Đang tính lại theo loại báo cáo mới...', 'info');
      document.getElementById('mergeBtn').click();
    }
  });
});

document.getElementById('downloadBtn').onclick = async function() {
  if (!parsedFiles.length) { toast('Chưa có dữ liệu để tải', 'warn'); return; }
  const reportType = (document.querySelector('input[name="reportType"]:checked') || {}).value || 'ky';
  const allRows = [];
  parsedFiles.forEach(function(f) { (f.rows || []).forEach(function(r) { allRows.push(r); }); });
  toast('Đang tạo file Excel...', 'info');
  try {
    const res = await fetch(API + '/export', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reportType: reportType, rows: allRows, fileNames: parsedFiles.map(function(f){return f.name;}) })
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const blob = await res.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'Tong-hop-bao-cao.xlsx';
    a.click();
    toast('Đã tải file Excel', 'ok');
  } catch (e) {
    toast('Lỗi tải Excel: ' + e.message, 'err');
  }
};

document.getElementById('saveBtn').onclick = function() {
  if (!parsedFiles.length) { toast('Chưa có dữ liệu để lưu', 'warn'); return; }
  document.getElementById('reportNameInput').value = defaultReportName();
  document.getElementById('saveNameModal').classList.add('show');
  setTimeout(function() { document.getElementById('reportNameInput').focus(); }, 100);
};
document.getElementById('closeSaveName').onclick = function() { document.getElementById('saveNameModal').classList.remove('show'); };
document.getElementById('cancelSaveName').onclick = function() { document.getElementById('saveNameModal').classList.remove('show'); };
document.getElementById('saveNameModal').onclick = function(e) {
  if (e.target.id === 'saveNameModal') document.getElementById('saveNameModal').classList.remove('show');
};

document.getElementById('confirmSaveName').onclick = async function() {
  const name = document.getElementById('reportNameInput').value.trim();
  if (!name) { toast('Vui lòng nhập tên báo cáo', 'warn'); return; }
  document.getElementById('saveNameModal').classList.remove('show');
  const reportType = (document.querySelector('input[name="reportType"]:checked') || {}).value || 'ky';
  const allRows = [];
  parsedFiles.forEach(function(f) { (f.rows || []).forEach(function(r) { allRows.push(r); }); });
  toast('Đang lưu báo cáo (có thể mất vài giây)...', 'info');
  try {
    const res = await fetch(API + '/merge', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        reportType: reportType, save: true, name: name,
        fileNames: parsedFiles.map(function(f){return f.name;}),
        rows: allRows
      })
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    if (data.savedId) {
      toast('Đã lưu: «' + name + '»', 'ok');
      if (document.getElementById('historyBox').querySelector('table')) {
        document.getElementById('loadHistoryBtn').click();
      }
    } else {
      toast('Không lưu được. Kiểm tra cấu hình lưu trữ trên server.', 'err');
    }
  } catch (e) {
    toast('Lỗi lưu: ' + e.message, 'err');
  }
};

document.getElementById('previewBtn').onclick = function() {
  if (!mergedRows.length) { toast('Hãy bấm Tổng hợp trước khi xem trước', 'warn'); return; }
  const typeLabel = isTuan() ? 'Theo Tuần' : 'Theo kỳ';
  document.getElementById('previewTitle').textContent = 'Xem trước — ' + typeLabel + ' (' + mergedRows.length + ' đơn vị)';
  document.getElementById('modalBody').innerHTML = buildFullTableHtml(mergedRows);
  document.getElementById('previewModal').classList.add('show');
  toast('Đã mở bảng xem trước đầy đủ', 'ok');
};
document.getElementById('closeModal').onclick = function() { document.getElementById('previewModal').classList.remove('show'); };
document.getElementById('previewModal').onclick = function(e) {
  if (e.target.id === 'previewModal') document.getElementById('previewModal').classList.remove('show');
};

document.getElementById('loadHistoryBtn').onclick = async function() {
  const box = document.getElementById('historyBox');
  toast('Đang tải lịch sử...', 'info');
  try {
    const res = await fetch(API + '/history?limit=50');
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const list = await res.json();
    if (!list.length) {
      box.textContent = 'Chưa có báo cáo nào được lưu.';
      toast('Chưa có lịch sử', 'warn');
      return;
    }
    box.innerHTML = '<table class="filelist"><thead><tr>' +
      '<th>Tên báo cáo</th><th>Loại</th><th>Thời gian</th><th>File</th><th>Đơn vị</th><th>Thao tác</th>' +
      '</tr></thead><tbody>' + list.map(function(h) {
        return '<tr data-id="' + esc(h.id) + '">' +
          '<td><strong>' + esc(h.name || '(Không tên)') + '</strong></td>' +
          '<td>' + esc(h.reportType === 'tuan' ? 'Tuần' : 'Kỳ') + '</td>' +
          '<td style="font-size:11.5px">' + esc(h.createdAt || '') + '</td>' +
          '<td>' + (h.fileCount || 0) + '</td>' +
          '<td>' + (h.unitCount || 0) + '</td>' +
          '<td style="white-space:nowrap">' +
          '<button class="act-btn view" data-act="view" data-id="' + esc(h.id) + '" data-type="' + esc(h.reportType || 'ky') + '" data-name="' + esc(h.name || '') + '" title="Xem">👁 Xem</button>' +
          '<button class="act-btn rename" data-act="rename" data-id="' + esc(h.id) + '" data-name="' + esc(h.name || '') + '" title="Sửa tên">✏️ Sửa</button>' +
          '<a class="act-btn dl" href="' + API + '/' + h.id + '/excel?type=' + (h.reportType || 'ky') + '" target="_blank" title="Tải Excel">⬇ Tải</a>' +
          '<button class="act-btn del" data-act="del" data-id="' + esc(h.id) + '" data-name="' + esc(h.name || '') + '" title="Xoá">🗑 Xoá</button>' +
          '</td></tr>';
      }).join('') + '</tbody></table>';

    box.querySelectorAll('[data-act]').forEach(function(btn) {
      btn.onclick = function() {
        const act = btn.dataset.act;
        const id = btn.dataset.id;
        const name = btn.dataset.name || '';
        const type = btn.dataset.type || 'ky';
        if (act === 'view') viewHistory(id, name, type);
        else if (act === 'rename') openRename(id, name);
        else if (act === 'del') deleteHistory(id, name);
      };
    });
    toast('Đã tải ' + list.length + ' báo cáo', 'ok');
  } catch (e) {
    box.textContent = 'Lỗi: ' + e.message;
    toast('Lỗi tải lịch sử: ' + e.message, 'err');
  }
};

async function viewHistory(id, name, type) {
  toast('Đang tải nội dung báo cáo...', 'info');
  try {
    const res = await fetch(API + '/' + id);
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const rec = await res.json();
    const rows = rec.rows || [];
    if (!rows.length) { toast('Báo cáo không có dữ liệu chi tiết', 'warn'); return; }
    document.getElementById('previewTitle').textContent =
      (name || rec.name || 'Báo cáo') + ' — ' + (type === 'tuan' ? 'Tuần' : 'Kỳ') + ' (' + rows.length + ' đơn vị)';
    document.getElementById('modalBody').innerHTML = buildFullTableHtml(rows, type);
    document.getElementById('previewModal').classList.add('show');
    toast('Đã mở báo cáo «' + (name || rec.name || id) + '»', 'ok');
  } catch (e) {
    toast('Không mở được báo cáo: ' + e.message, 'err');
  }
}

function openRename(id, name) {
  renameTargetId = id;
  document.getElementById('renameInput').value = name || '';
  document.getElementById('renameModal').classList.add('show');
  setTimeout(function() { document.getElementById('renameInput').focus(); }, 100);
}
document.getElementById('closeRename').onclick = function() { document.getElementById('renameModal').classList.remove('show'); };
document.getElementById('cancelRename').onclick = function() { document.getElementById('renameModal').classList.remove('show'); };
document.getElementById('renameModal').onclick = function(e) {
  if (e.target.id === 'renameModal') document.getElementById('renameModal').classList.remove('show');
};
document.getElementById('confirmRename').onclick = async function() {
  const name = document.getElementById('renameInput').value.trim();
  if (!name) { toast('Tên không được để trống', 'warn'); return; }
  if (!renameTargetId) return;
  toast('Đang cập nhật tên...', 'info');
  try {
    const res = await fetch(API + '/' + renameTargetId + '/name', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: name })
    });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    document.getElementById('renameModal').classList.remove('show');
    toast('Đã đổi tên thành «' + name + '»', 'ok');
    document.getElementById('loadHistoryBtn').click();
  } catch (e) {
    toast('Lỗi đổi tên: ' + e.message, 'err');
  }
};

async function deleteHistory(id, name) {
  const ok = await showConfirm(
    'Xoá báo cáo',
    'Bạn có chắc muốn xoá báo cáo <strong>«' + esc(name || id) + '»</strong>?<p class="warn-text">Thao tác không hoàn tác được.</p>',
    'Xoá'
  );
  if (!ok) { toast('Đã huỷ xoá', 'info'); return; }
  toast('Đang xoá...', 'info');
  try {
    const res = await fetch(API + '/' + id, { method: 'DELETE' });
    if (!res.ok) throw new Error('HTTP ' + res.status);
    toast('Đã xoá «' + (name || id) + '»', 'ok');
    document.getElementById('loadHistoryBtn').click();
  } catch (e) {
    toast('Lỗi xoá: ' + e.message, 'err');
  }
}

fetch(API + '/health').then(function(r) { return r.json(); }).then(function(h) {
  if (h.build) {
    var el = document.getElementById('buildId');
    if (el) el.textContent = h.build;
  }
  if (h.storage) toast('Hệ thống sẵn sàng · ' + (h.build || '') + ' · Lưu lịch sử khả dụng', 'ok');
  else toast('Hệ thống sẵn sàng · ' + (h.build || '') + ' · Chưa cấu hình lưu lịch sử', 'warn');
  document.getElementById('loadHistoryBtn').click();
}).catch(function() { toast('Không kết nối được server', 'err'); });
