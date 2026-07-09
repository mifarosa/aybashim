<template>
  <main class="app-shell">
    <section v-if="!token" class="auth-layout">
      <div class="brand-panel">
        <p class="eyebrow">aybashim</p>
        <h1>Ekstrelerden okunabilir finans takibine.</h1>
        <p class="lead">
          Banka hareketlerini içeri al, kategorileri kontrol et, aylık akışı tek ekrandan izle.
        </p>
      </div>

      <form class="auth-panel" @submit.prevent="submitAuth">
        <div class="segmented">
          <button type="button" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">
            Giriş
          </button>
          <button type="button" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">
            Kayıt
          </button>
        </div>

        <label v-if="authMode === 'register'">
          Ad Soyad
          <input v-model.trim="authForm.name" autocomplete="name" required />
        </label>
        <label>
          E-posta
          <input v-model.trim="authForm.email" type="email" autocomplete="email" required />
        </label>
        <label>
          Şifre
          <input v-model="authForm.password" type="password" autocomplete="current-password" required />
        </label>

        <button class="primary" type="submit" :disabled="busy">
          {{ busy ? 'Bekleniyor' : authMode === 'login' ? 'Giriş yap' : 'Hesap oluştur' }}
        </button>
        <p v-if="message" class="message error">{{ message }}</p>
      </form>
    </section>

    <section v-else class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">aybashim</p>
          <h1>Finans Paneli</h1>
        </div>
        <div class="user-strip">
          <button class="ghost" type="button" @click="showAmounts = !showAmounts">
            {{ showAmounts ? 'Tutarları gizle' : 'Tutarları göster' }}
          </button>
          <span>{{ user?.name }}</span>
          <button class="ghost" type="button" @click="logout">Çıkış</button>
        </div>
      </header>

      <section class="summary-grid">
        <div class="metric">
          <span>Toplam işlem</span>
          <strong>{{ transactions.length }}</strong>
        </div>
        <div class="metric">
          <span>Bu ay gider</span>
          <strong><SensitiveAmount :value="currentMonthDebit" :reveal="showAmounts" /></strong>
          <em>{{ currentMonthExpenseLabel }}</em>
        </div>
        <div class="metric">
          <span>Bu ay gelir</span>
          <strong><SensitiveAmount :value="currentMonthCredit" :reveal="showAmounts" /></strong>
          <em>{{ currentMonthIncomeLabel }}</em>
        </div>
        <div class="metric">
          <span>Kendime transfer</span>
          <strong>{{ selfTransfers.length }}</strong>
        </div>
      </section>

      <section class="control-band">
        <form class="upload-form" @submit.prevent="uploadStatement">
          <label>
            Banka
            <select v-model="upload.bankName">
              <option value="ING_ACCOUNT">ING Hesap PDF</option>
              <option value="ING_CREDIT">ING Kredi PDF</option>
              <option value="HADI">A101 Hadi Kart PDF</option>
              <option value="GARANTI">Garanti XLS</option>
            </select>
          </label>
          <label class="file-input">
            Ekstre
            <input type="file" @change="onFileChange" />
          </label>
          <button class="primary" type="submit" :disabled="busy || !upload.file">
            Yükle
          </button>
        </form>

        <div class="actions">
          <button class="secondary" type="button" :disabled="busy" @click="loadDashboard">Yenile</button>
          <button class="secondary" type="button" :disabled="busy" @click="recategorize">Yeniden kategorilendir</button>
        </div>
      </section>

      <p v-if="message" :class="['message', messageType]">{{ message }}</p>

      <nav class="tabs" aria-label="Panel görünümü">
        <button :class="{ active: activeTab === 'transactions' }" @click="activeTab = 'transactions'">İşlemler</button>
        <button :class="{ active: activeTab === 'expenses' }" @click="activeTab = 'expenses'">Giderler</button>
        <button :class="{ active: activeTab === 'income' }" @click="activeTab = 'income'">Gelirler</button>
        <button :class="{ active: activeTab === 'summary' }" @click="activeTab = 'summary'">Aylık özet</button>
        <button :class="{ active: activeTab === 'self' }" @click="activeTab = 'self'">Kendime transferler</button>
      </nav>

      <section v-if="activeTab === 'transactions'" class="data-section">
        <div class="filters">
          <input v-model.trim="filters.keyword" placeholder="Açıklamada ara" />
          <select v-model="filters.type">
            <option value="">Tüm tipler</option>
            <option value="DEBIT">Gider</option>
            <option value="CREDIT">Gelir</option>
          </select>
          <select v-model="filters.bankName">
            <option value="">Tüm bankalar</option>
            <option v-for="bank in bankNames" :key="bank" :value="bank">{{ bank }}</option>
          </select>
          <select v-model="filters.mainCategory">
            <option value="">Tüm ana kategoriler</option>
            <option v-for="category in mainCategories" :key="category" :value="category">{{ category }}</option>
          </select>
          <select v-model="filters.subCategory">
            <option value="">Tüm alt kategoriler</option>
            <option v-for="category in subCategories" :key="category.code" :value="category.code">
              {{ category.displayName }}
            </option>
          </select>
          <input v-model="filters.startDate" type="date" />
          <input v-model="filters.endDate" type="date" />
          <button class="secondary compact" type="button" @click="clearFilters">Temizle</button>
        </div>

        <TransactionTable :transactions="filteredTransactions" />
      </section>

      <section v-if="activeTab === 'expenses'" class="data-section income-layout">
        <div class="filters">
          <input v-model.trim="expenseFilters.keyword" placeholder="Gider açıklamasında ara" />
          <select v-model="expenseFilters.bankName">
            <option value="">Tüm bankalar</option>
            <option v-for="bank in expenseBankNames" :key="bank" :value="bank">{{ bank }}</option>
          </select>
          <select v-model="expenseFilters.subCategory">
            <option value="">Tüm gider tipleri</option>
            <option v-for="category in expenseSubCategories" :key="category" :value="category">{{ category }}</option>
          </select>
          <input v-model="expenseFilters.startDate" type="date" />
          <input v-model="expenseFilters.endDate" type="date" />
          <button class="secondary compact" type="button" @click="clearExpenseFilters">Temizle</button>
        </div>

        <div class="income-content">
          <div class="income-sources">
            <h2>Gider kaynakları</h2>
            <article v-for="source in expenseSources" :key="source.key" class="source-row">
              <div>
                <strong>{{ source.label }}</strong>
                <span>{{ source.count }} işlem · {{ source.bankName }}</span>
              </div>
              <b><SensitiveAmount :value="source.total" :reveal="showAmounts" /></b>
            </article>
            <p v-if="expenseSources.length === 0" class="empty-state">Gider kaydı bulunamadı.</p>
          </div>

          <TransactionTable :transactions="filteredExpenseTransactions" empty-text="Gider kaydı bulunamadı." />
        </div>
      </section>

      <section v-if="activeTab === 'income'" class="data-section income-layout">
        <div class="filters">
          <input v-model.trim="incomeFilters.keyword" placeholder="Gelir açıklamasında ara" />
          <select v-model="incomeFilters.bankName">
            <option value="">Tüm bankalar</option>
            <option v-for="bank in incomeBankNames" :key="bank" :value="bank">{{ bank }}</option>
          </select>
          <select v-model="incomeFilters.subCategory">
            <option value="">Tüm gelir tipleri</option>
            <option v-for="category in incomeSubCategories" :key="category" :value="category">{{ category }}</option>
          </select>
          <input v-model="incomeFilters.startDate" type="date" />
          <input v-model="incomeFilters.endDate" type="date" />
          <button class="secondary compact" type="button" @click="clearIncomeFilters">Temizle</button>
        </div>

        <div class="income-content">
          <div class="income-sources">
            <h2>Gelir kaynakları</h2>
            <article v-for="source in incomeSources" :key="source.key" class="source-row">
              <div>
                <strong>{{ source.label }}</strong>
                <span>{{ source.count }} işlem · {{ source.bankName }}</span>
              </div>
              <b><SensitiveAmount :value="source.total" :reveal="showAmounts" /></b>
            </article>
            <p v-if="incomeSources.length === 0" class="empty-state">Gelir kaydı bulunamadı.</p>
          </div>

          <TransactionTable :transactions="filteredIncomeTransactions" empty-text="Gelir kaydı bulunamadı." />
        </div>
      </section>

      <section v-if="activeTab === 'summary'" class="data-section summary-panel">
        <div class="summary-visual">
          <div class="summary-toolbar">
            <div>
              <p class="eyebrow">Aylık dağılım</p>
              <h2>{{ selectedSummaryMonth }}</h2>
            </div>
            <select v-model="selectedSummaryMonth">
              <option v-for="month in monthKeys" :key="month" :value="month">{{ month }}</option>
            </select>
          </div>

          <div class="pie-layout">
            <div class="pie-chart" :style="pieChartStyle">
              <div class="pie-hole">
                <span>Gider</span>
                <strong><SensitiveAmount :value="selectedMonthExpenseTotal" :reveal="showAmounts" /></strong>
              </div>
            </div>

            <div class="pie-legend">
              <article v-for="item in selectedMonthSubCategories" :key="item.name" class="legend-row">
                <span class="legend-color" :style="{ background: item.color }"></span>
                <div>
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.percent.toFixed(1) }}%</small>
                </div>
                <b><SensitiveAmount :value="item.total" :reveal="showAmounts" /></b>
              </article>
              <p v-if="selectedMonthSubCategories.length === 0" class="empty-state">Bu ay gider kaydı yok.</p>
            </div>
          </div>
        </div>

        <div class="month-list">
          <article v-for="row in monthlyRows" :key="row.month" class="month-row">
            <div class="month-title">
              <strong>{{ row.month }}</strong>
              <SensitiveAmount :value="row.credit - row.debit" :reveal="showAmounts" />
            </div>
            <div class="bars">
              <span class="bar credit" :style="{ width: `${row.creditPercent}%` }"></span>
              <span class="bar debit" :style="{ width: `${row.debitPercent}%` }"></span>
            </div>
            <div class="month-values">
              <span>Gelir <SensitiveAmount :value="row.credit" :reveal="showAmounts" /></span>
              <span>Gider <SensitiveAmount :value="row.debit" :reveal="showAmounts" /></span>
            </div>
          </article>
        </div>

        <div class="category-list">
          <h2>{{ selectedSummaryMonth }} alt kategorileri</h2>
          <article v-for="item in selectedMonthSubCategories" :key="item.name" class="category-row">
            <span>{{ item.name }}</span>
            <strong><SensitiveAmount :value="item.total" :reveal="showAmounts" /></strong>
          </article>
          <p v-if="selectedMonthSubCategories.length === 0" class="empty-state">Bu ay gider kaydı yok.</p>
        </div>
      </section>

      <section v-if="activeTab === 'self'" class="data-section">
        <TransactionTable :transactions="selfTransfers" empty-text="Kendime transfer kaydı yok." />
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue';
import { apiRequest, clearSession, loadSession, saveSession } from './api';

const session = loadSession();
const token = ref(session.token);
const user = ref(session.user);
const busy = ref(false);
const message = ref('');
const messageType = ref('info');
const authMode = ref('login');
const activeTab = ref('summary');
const showAmounts = ref(false);
const selectedSummaryMonth = ref(new Date().toISOString().slice(0, 7));

const authForm = reactive({
  name: '',
  email: '',
  password: ''
});

const upload = reactive({
  bankName: 'ING_ACCOUNT',
  file: null
});

const filters = reactive({
  keyword: '',
  type: '',
  bankName: '',
  mainCategory: '',
  subCategory: '',
  startDate: '',
  endDate: ''
});

const expenseFilters = reactive({
  keyword: '',
  bankName: '',
  subCategory: '',
  startDate: '',
  endDate: ''
});

const incomeFilters = reactive({
  keyword: '',
  bankName: '',
  subCategory: '',
  startDate: '',
  endDate: ''
});

const transactions = ref([]);
const selfTransfers = ref([]);
const mainCategories = ref([]);
const subCategories = ref([]);
const monthlySummary = ref({});
const subCategorySummary = ref({});

const TransactionTable = defineComponent({
  props: {
    transactions: { type: Array, required: true },
    emptyText: { type: String, default: 'Kayıt bulunamadı.' }
  },
  setup(props) {
    return () => h('div', { class: 'table-wrap' }, [
      props.transactions.length === 0
        ? h('p', { class: 'empty-state' }, props.emptyText)
        : h('table', [
          h('thead', [
            h('tr', [
              h('th', 'Tarih'),
              h('th', 'Açıklama'),
              h('th', 'Banka'),
              h('th', 'Tip'),
              h('th', 'Kategori'),
              h('th', { class: 'amount' }, 'Tutar')
            ])
          ]),
          h('tbody', props.transactions.map((tx) => h('tr', { key: tx.id ?? `${tx.date}-${tx.description}` }, [
            h('td', tx.date),
            h('td', { class: 'description' }, tx.description),
            h('td', tx.bankName || '-'),
            h('td', h('span', { class: ['pill', tx.type?.toLowerCase()] }, tx.type)),
            h('td', [tx.mainCategory || '-', h('small', tx.subCategory || '')]),
            h('td', { class: 'amount' }, h(SensitiveAmount, {
              value: Number(tx.amount || 0),
              reveal: showAmounts.value
            }))
          ])))
        ])
    ]);
  }
});

const SensitiveAmount = defineComponent({
  props: {
    value: { type: Number, required: true },
    reveal: { type: Boolean, default: false }
  },
  setup(props) {
    return () => h('span', {
      class: ['sensitive-amount', { reveal: props.reveal }],
      tabindex: '0',
      'aria-label': 'Gizli tutar'
    }, [
      h('span', { class: 'masked', 'aria-hidden': 'true' }, '••••••'),
      h('span', { class: 'revealed' }, formatMoney(props.value))
    ]);
  }
});

const filteredTransactions = computed(() => transactions.value.filter((tx) => {
  const keyword = filters.keyword.toLocaleLowerCase('tr-TR');
  const description = (tx.description || '').toLocaleLowerCase('tr-TR');
  return (!keyword || description.includes(keyword))
    && (!filters.type || tx.type === filters.type)
    && (!filters.bankName || tx.bankName === filters.bankName)
    && (!filters.mainCategory || tx.mainCategory === filters.mainCategory)
    && (!filters.subCategory || tx.subCategory === filters.subCategory)
    && isInDateRange(tx.date, filters.startDate, filters.endDate);
}));

const bankNames = computed(() => uniqueSorted(transactions.value.map((tx) => tx.bankName).filter(Boolean)));

const expenseTransactions = computed(() => transactions.value.filter(isExpenseTransaction));

const expenseBankNames = computed(() => uniqueSorted(expenseTransactions.value.map((tx) => tx.bankName).filter(Boolean)));

const expenseSubCategories = computed(() => uniqueSorted(expenseTransactions.value.map((tx) => tx.subCategory).filter(Boolean)));

const filteredExpenseTransactions = computed(() => expenseTransactions.value.filter((tx) => {
  const keyword = expenseFilters.keyword.toLocaleLowerCase('tr-TR');
  const description = (tx.description || '').toLocaleLowerCase('tr-TR');
  return (!keyword || description.includes(keyword))
    && (!expenseFilters.bankName || tx.bankName === expenseFilters.bankName)
    && (!expenseFilters.subCategory || tx.subCategory === expenseFilters.subCategory)
    && isInDateRange(tx.date, expenseFilters.startDate, expenseFilters.endDate);
}));

const expenseSources = computed(() => {
  return groupSources(filteredExpenseTransactions.value, expenseSourceLabel);
});

const incomeTransactions = computed(() => transactions.value.filter((tx) => tx.mainCategory === 'INCOME'));

const incomeBankNames = computed(() => uniqueSorted(incomeTransactions.value.map((tx) => tx.bankName).filter(Boolean)));

const incomeSubCategories = computed(() => uniqueSorted(incomeTransactions.value.map((tx) => tx.subCategory).filter(Boolean)));

const filteredIncomeTransactions = computed(() => incomeTransactions.value.filter((tx) => {
  const keyword = incomeFilters.keyword.toLocaleLowerCase('tr-TR');
  const description = (tx.description || '').toLocaleLowerCase('tr-TR');
  return (!keyword || description.includes(keyword))
    && (!incomeFilters.bankName || tx.bankName === incomeFilters.bankName)
    && (!incomeFilters.subCategory || tx.subCategory === incomeFilters.subCategory)
    && isInDateRange(tx.date, incomeFilters.startDate, incomeFilters.endDate);
}));

const incomeSources = computed(() => {
  return groupSources(filteredIncomeTransactions.value, incomeSourceLabel);
});

const currentMonth = computed(() => new Date().toISOString().slice(0, 7));

const currentMonthDebit = computed(() => sumTransactions(
  expenseTransactions.value.filter((tx) => tx.date?.startsWith(currentMonth.value))
));
const currentMonthCredit = computed(() => sumTransactions(
  incomeTransactions.value.filter((tx) => tx.date?.startsWith(currentMonth.value))
));
const monthKeys = computed(() => uniqueSorted(transactions.value
  .map((tx) => tx.date?.slice(0, 7))
  .filter(Boolean))
  .reverse());
const currentMonthIncomeLabel = computed(() => {
  const sources = groupSources(incomeTransactions.value.filter((tx) => tx.date?.startsWith(currentMonth.value)), incomeSourceLabel);
  if (sources.length === 0) {
    return 'Gelir kaydı yok';
  }

  const primary = sources[0];
  return sources.length > 1 ? `${primary.label} +${sources.length - 1}` : primary.label;
});
const currentMonthExpenseLabel = computed(() => {
  const sources = groupSources(expenseTransactions.value.filter((tx) => tx.date?.startsWith(currentMonth.value)), expenseSourceLabel);
  if (sources.length === 0) {
    return 'Gider kaydı yok';
  }

  const primary = sources[0];
  return sources.length > 1 ? `${primary.label} +${sources.length - 1}` : primary.label;
});

const monthlyRows = computed(() => monthKeys.value.map((month) => {
  const debit = sumTransactions(expenseTransactions.value.filter((tx) => tx.date?.startsWith(month)));
  const credit = sumTransactions(incomeTransactions.value.filter((tx) => tx.date?.startsWith(month)));
  const max = Math.max(debit, credit, 1);
  return {
    month,
    debit,
    credit,
    debitPercent: Math.max((debit / max) * 100, debit > 0 ? 4 : 0),
    creditPercent: Math.max((credit / max) * 100, credit > 0 ? 4 : 0)
  };
}));

const selectedMonthSubCategories = computed(() => {
  const totals = new Map();

  expenseTransactions.value
    .filter((tx) => tx.date?.startsWith(selectedSummaryMonth.value))
    .forEach((tx) => {
      const category = tx.subCategory || 'UNKNOWN';
      totals.set(category, (totals.get(category) || 0) + Number(tx.amount || 0));
  });

  const total = [...totals.values()].reduce((sum, value) => sum + value, 0);

  return [...totals.entries()]
    .map(([name, value], index) => ({
      name,
      total: value,
      percent: total > 0 ? (value / total) * 100 : 0,
      color: pieColors[index % pieColors.length]
    }))
    .sort((a, b) => b.total - a.total)
    .slice(0, 10);
});

const selectedMonthExpenseTotal = computed(() => sumTransactions(
  expenseTransactions.value.filter((tx) => tx.date?.startsWith(selectedSummaryMonth.value))
));

const pieChartStyle = computed(() => {
  if (selectedMonthSubCategories.value.length === 0) {
    return { background: '#eef2f5' };
  }

  let cursor = 0;
  const segments = selectedMonthSubCategories.value.map((item) => {
    const start = cursor;
    cursor += item.percent;
    return `${item.color} ${start}% ${cursor}%`;
  });

  return { background: `conic-gradient(${segments.join(', ')})` };
});

const pieColors = [
  '#116b5f',
  '#b54708',
  '#2563eb',
  '#9333ea',
  '#c2410c',
  '#0f766e',
  '#be123c',
  '#4d7c0f',
  '#7c3aed',
  '#64748b'
];

onMounted(() => {
  if (token.value) {
    loadDashboard();
  }
});

async function submitAuth() {
  withMessage('');
  busy.value = true;
  try {
    const path = authMode.value === 'login' ? '/api/auth/login' : '/api/auth/register';
    const payload = authMode.value === 'login'
      ? { email: authForm.email, password: authForm.password }
      : { name: authForm.name, email: authForm.email, password: authForm.password };
    const auth = await apiRequest(path, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    saveSession(auth);
    token.value = auth.token;
    user.value = { id: auth.userId, name: auth.name, email: auth.email };
    await loadDashboard();
  } catch (error) {
    withMessage(error.message, 'error');
  } finally {
    busy.value = false;
  }
}

async function loadDashboard() {
  busy.value = true;
  try {
    const [txs, self, mains, subs, monthly, subMonthly] = await Promise.all([
      apiRequest('/api/transactions', {}, token.value),
      apiRequest('/api/transactions/self-transfers', {}, token.value),
      apiRequest('/api/transactions/categories/main', {}, token.value),
      apiRequest('/api/transactions/categories/sub', {}, token.value),
      apiRequest('/api/transactions/summary/monthly', {}, token.value),
      apiRequest('/api/transactions/summary/monthly/sub-category', {}, token.value)
    ]);
    transactions.value = txs;
    selfTransfers.value = self;
    mainCategories.value = mains;
    subCategories.value = subs;
    monthlySummary.value = monthly;
    subCategorySummary.value = subMonthly;
    const availableMonths = uniqueSorted(txs.map((tx) => tx.date?.slice(0, 7)).filter(Boolean)).reverse();
    if (availableMonths.length > 0 && !availableMonths.includes(selectedSummaryMonth.value)) {
      selectedSummaryMonth.value = availableMonths[0];
    }
  } catch (error) {
    withMessage(error.message, 'error');
  } finally {
    busy.value = false;
  }
}

async function uploadStatement() {
  if (!upload.file) return;
  withMessage('');
  busy.value = true;
  try {
    const formData = new FormData();
    formData.append('bankName', upload.bankName);
    formData.append('file', upload.file);
    const path = upload.bankName === 'GARANTI' ? '/api/transactions/upload/excel' : '/api/transactions/upload';
    const result = await apiRequest(path, { method: 'POST', body: formData }, token.value);
    withMessage(`${result.savedCount} kayıt eklendi, ${result.duplicateCount} duplicate atlandı.`, 'success');
    upload.file = null;
    await loadDashboard();
  } catch (error) {
    withMessage(error.message, 'error');
  } finally {
    busy.value = false;
  }
}

async function recategorize() {
  withMessage('');
  busy.value = true;
  try {
    const result = await apiRequest('/api/transactions/recategorize', { method: 'POST' }, token.value);
    withMessage(`${result.length} kayıt yeniden kategorilendirildi.`, 'success');
    await loadDashboard();
  } catch (error) {
    withMessage(error.message, 'error');
  } finally {
    busy.value = false;
  }
}

function onFileChange(event) {
  upload.file = event.target.files?.[0] || null;
}

function logout() {
  clearSession();
  token.value = null;
  user.value = null;
  transactions.value = [];
  selfTransfers.value = [];
}

function clearFilters() {
  Object.assign(filters, {
    keyword: '',
    type: '',
    bankName: '',
    mainCategory: '',
    subCategory: '',
    startDate: '',
    endDate: ''
  });
}

function clearExpenseFilters() {
  Object.assign(expenseFilters, {
    keyword: '',
    bankName: '',
    subCategory: '',
    startDate: '',
    endDate: ''
  });
}

function clearIncomeFilters() {
  Object.assign(incomeFilters, {
    keyword: '',
    bankName: '',
    subCategory: '',
    startDate: '',
    endDate: ''
  });
}

function withMessage(text, type = 'info') {
  message.value = text;
  messageType.value = type;
}

function formatMoney(value) {
  return new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency: 'TRY',
    maximumFractionDigits: 2
  }).format(Number(value || 0));
}

function isInDateRange(date, startDate, endDate) {
  return (!startDate || date >= startDate) && (!endDate || date <= endDate);
}

function uniqueSorted(values) {
  return [...new Set(values)].sort((a, b) => a.localeCompare(b, 'tr'));
}

function sumTransactions(items) {
  return items.reduce((total, tx) => total + Number(tx.amount || 0), 0);
}

function groupSources(items, labelFactory) {
  const groups = new Map();

  items.forEach((tx) => {
    const label = labelFactory(tx);
    const bankName = tx.bankName || '-';
    const key = `${bankName}|${label}`;
    const current = groups.get(key) || { key, label, bankName, total: 0, count: 0 };
    current.total += Number(tx.amount || 0);
    current.count += 1;
    groups.set(key, current);
  });

  return [...groups.values()].sort((a, b) => b.total - a.total);
}

function isExpenseTransaction(tx) {
  if (tx.type !== 'DEBIT') {
    return false;
  }

  return !['TRANSFER', 'CASH', 'INVESTMENT'].includes(tx.mainCategory);
}

function expenseSourceLabel(tx) {
  return tx.subCategory || tx.description || 'Bilinmeyen gider';
}

function incomeSourceLabel(tx) {
  if (tx.subCategory === 'SALARY') {
    return 'Maaş';
  }
  if (tx.subCategory === 'DEBT_PAYMENT') {
    return 'Kart ödemesi';
  }
  if (tx.subCategory === 'MONEY_RECEIVED') {
    return tx.description || 'Gelen transfer';
  }
  return tx.description || tx.subCategory || 'Bilinmeyen gelir';
}
</script>
