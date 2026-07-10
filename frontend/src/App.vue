<template>
  <main class="app-shell">
    <section v-if="!token" class="auth-layout">
      <div class="brand-panel">
        <div class="brand-copy">
          <p class="eyebrow">aybashim</p>
          <h1>Ay sonunu beklemeden akışı gör.</h1>
          <p class="lead">
            Ekstrelerini içeri al, gelir ve gideri sakin bir panelde izle, neyin nereye gittiğini tek bakışta yakala.
          </p>
        </div>
        <div class="hero-visual" aria-hidden="true">
          <img :src="heroImage" alt="" />
          <div class="floating-card balance-card">
            <span>Bu ay denge</span>
            <strong><SensitiveAmount :value="authPreviewBalance" :reveal="showAmounts" /></strong>
          </div>
          <div class="floating-card flow-card">
            <span>Gelir / gider</span>
            <div class="mini-bars">
              <i class="income" :style="{ height: `${authPreviewIncome}%` }"></i>
              <i class="expense" :style="{ height: `${authPreviewExpense}%` }"></i>
              <i class="saving" :style="{ height: `${authPreviewSaving}%` }"></i>
            </div>
          </div>
        </div>
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
          <button class="ghost" type="button" @click="toggleAccountPanel">Hesap</button>
          <button class="ghost" type="button" @click="logout">Çıkış</button>
        </div>
      </header>

      <section v-if="showAccountPanel" class="account-panel">
        <div>
          <p class="eyebrow">Hesap</p>
          <h2>Profil bilgileri</h2>
        </div>
        <form class="account-form" @submit.prevent="updateProfile">
          <label>
            Ad Soyad
            <input v-model.trim="profileForm.name" autocomplete="name" required />
          </label>
          <label>
            E-posta
            <input v-model.trim="profileForm.email" type="email" autocomplete="email" required />
          </label>
          <label>
            Yeni şifre
            <input v-model="profileForm.password" type="password" autocomplete="new-password" placeholder="Değişmeyecekse boş bırak" />
          </label>
          <button class="primary" type="submit" :disabled="busy">Kaydet</button>
        </form>
      </section>

      <section class="dashboard-hero">
        <div class="dashboard-hero-copy">
          <p class="eyebrow">Aylık durum</p>
          <h2>{{ selectedSummaryMonth }} özeti</h2>
          <p>
            Gelir, gider ve kategori dağılımını aynı ekranda takip et. Tutarlar kapalıyken hover ile geçici olarak görünür.
          </p>
          <div class="hero-stats">
            <span>
              Gelir
              <strong><SensitiveAmount :value="selectedMonthIncomeTotal" :reveal="showAmounts" /></strong>
            </span>
            <span>
              Gider
              <strong><SensitiveAmount :value="selectedMonthExpenseTotal" :reveal="showAmounts" /></strong>
            </span>
            <span>
              Net
              <strong><SensitiveAmount :value="selectedMonthIncomeTotal - selectedMonthExpenseTotal" :reveal="showAmounts" /></strong>
            </span>
          </div>
        </div>
        <div class="dashboard-hero-visual" aria-hidden="true">
          <img :src="heroImage" alt="" />
        </div>
      </section>

      <section class="summary-grid">
        <div class="metric metric-soft-blue">
          <span>Toplam işlem</span>
          <strong>{{ transactions.length }}</strong>
        </div>
        <div class="metric metric-soft-coral">
          <span>Bu ay gider</span>
          <strong><SensitiveAmount :value="currentMonthDebit" :reveal="showAmounts" /></strong>
          <em>{{ currentMonthExpenseLabel }}</em>
        </div>
        <div class="metric metric-soft-green">
          <span>Bu ay gelir</span>
          <strong><SensitiveAmount :value="currentMonthCredit" :reveal="showAmounts" /></strong>
          <em>{{ currentMonthIncomeLabel }}</em>
        </div>
        <div class="metric metric-soft-yellow">
          <span>Kendime transfer</span>
          <strong>{{ selfTransfers.length }}</strong>
        </div>
      </section>

      <section class="control-band">
        <form class="upload-form" @submit.prevent="uploadStatement">
          <div class="bank-picker" role="radiogroup" aria-label="Banka">
            <button
              v-for="option in bankOptions"
              :key="option.value"
              type="button"
              :class="['bank-card', { active: upload.bankName === option.value }]"
              role="radio"
              :aria-checked="upload.bankName === option.value"
              @click="upload.bankName = option.value"
            >
              <span class="bank-mark">{{ option.mark }}</span>
              <span>
                <strong>{{ option.label }}</strong>
                <small>{{ option.detail }}</small>
              </span>
            </button>
          </div>
          <label class="file-input">
            Ekstre
            <span class="file-picker">
              <span class="file-picker-button">Dosya seç</span>
              <span class="file-picker-name">{{ upload.file?.name || 'Henüz dosya seçilmedi' }}</span>
              <input type="file" @change="onFileChange" />
            </span>
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

      <section class="cashflow-panel">
        <div class="cashflow-header">
          <div>
            <p class="eyebrow">Nakit akışı</p>
            <h2>Son aylar</h2>
          </div>
          <div class="cashflow-key">
            <span><i class="credit"></i> Gelir</span>
            <span><i class="debit"></i> Gider</span>
            <span><i class="net"></i> Net</span>
          </div>
        </div>
        <div v-if="cashflowRows.length > 0" class="cashflow-chart">
          <article v-for="row in cashflowRows" :key="row.month" class="cashflow-row">
            <span class="cashflow-month">{{ row.month }}</span>
            <div class="cashflow-bars">
              <span class="cashbar credit" :style="{ width: `${row.creditPercent}%` }"></span>
              <span class="cashbar debit" :style="{ width: `${row.debitPercent}%` }"></span>
              <span class="cashbar net" :style="{ width: `${row.netPercent}%` }"></span>
            </div>
            <strong><SensitiveAmount :value="row.net" :reveal="showAmounts" /></strong>
          </article>
        </div>
        <EmptyState v-else title="Nakit akışı yok" text="Ekstre yüklediğinde aylık gelir, gider ve net denge burada görünecek." />
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
              <div class="source-main">
                <CategoryBadge :category="source.label" />
                <span>
                  <strong>{{ source.label }}</strong>
                  <small>{{ source.count }} işlem · {{ source.bankName }}</small>
                </span>
              </div>
              <b><SensitiveAmount :value="source.total" :reveal="showAmounts" /></b>
            </article>
            <EmptyState v-if="expenseSources.length === 0" title="Gider kaydı yok" text="Filtreleri değiştirdiğinde veya yeni ekstre yüklediğinde burada kaynaklar listelenir." />
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
              <div class="source-main">
                <CategoryBadge :category="source.label" />
                <span>
                  <strong>{{ source.label }}</strong>
                  <small>{{ source.count }} işlem · {{ source.bankName }}</small>
                </span>
              </div>
              <b><SensitiveAmount :value="source.total" :reveal="showAmounts" /></b>
            </article>
            <EmptyState v-if="incomeSources.length === 0" title="Gelir kaydı yok" text="Maaş, iade veya gelen transfer kayıtları burada kaynaklarına göre görünür." />
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
                <CategoryBadge :category="item.name" :color="item.color" />
                <div>
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.percent.toFixed(1) }}%</small>
                </div>
                <b><SensitiveAmount :value="item.total" :reveal="showAmounts" /></b>
              </article>
              <EmptyState v-if="selectedMonthSubCategories.length === 0" title="Bu ay gider yok" text="Seçili ay için gider hareketi bulunamadı." />
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
            <span class="category-name"><CategoryBadge :category="item.name" :color="item.color" />{{ item.name }}</span>
            <strong><SensitiveAmount :value="item.total" :reveal="showAmounts" /></strong>
          </article>
          <EmptyState v-if="selectedMonthSubCategories.length === 0" title="Alt kategori yok" text="Seçili ay için kategori toplamı oluşmadı." />
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
import heroImage from './assets/finance-hero.png';

const session = loadSession();
const token = ref(session.token);
const user = ref(session.user);
const busy = ref(false);
const message = ref('');
const messageType = ref('info');
const authMode = ref('login');
const activeTab = ref('summary');
const showAmounts = ref(false);
const showAccountPanel = ref(false);
const selectedSummaryMonth = ref(new Date().toISOString().slice(0, 7));
const authPreviewIncome = 82;
const authPreviewExpense = 54;
const authPreviewSaving = 38;
const authPreviewBalance = 41850;

const authForm = reactive({
  name: '',
  email: '',
  password: ''
});

const profileForm = reactive({
  name: user.value?.name || '',
  email: user.value?.email || '',
  password: ''
});

const upload = reactive({
  bankName: 'ING_ACCOUNT',
  file: null
});

const bankOptions = [
  { value: 'ING_ACCOUNT', label: 'ING Hesap', detail: 'PDF', mark: 'IA' },
  { value: 'ING_CREDIT', label: 'ING Kredi', detail: 'PDF', mark: 'IK' },
  { value: 'HADI', label: 'A101 Hadi', detail: 'PDF', mark: 'HD' },
  { value: 'GARANTI', label: 'Garanti', detail: 'XLS', mark: 'GB' }
];

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
    const sort = reactive({
      key: 'date',
      direction: 'desc'
    });
    const columns = [
      { key: 'date', label: 'Tarih' },
      { key: 'description', label: 'Açıklama' },
      { key: 'bankName', label: 'Banka' },
      { key: 'type', label: 'Tip' },
      { key: 'category', label: 'Kategori' },
      { key: 'amount', label: 'Tutar', class: 'amount' }
    ];
    const sortedTransactions = computed(() => [...props.transactions].sort((left, right) => {
      const result = compareValues(sortValue(left, sort.key), sortValue(right, sort.key), sort.key);
      return sort.direction === 'asc' ? result : -result;
    }));
    const toggleSort = (key) => {
      if (sort.key === key) {
        sort.direction = sort.direction === 'asc' ? 'desc' : 'asc';
        return;
      }

      sort.key = key;
      sort.direction = key === 'date' || key === 'amount' ? 'desc' : 'asc';
    };
    const header = (column) => h('button', {
      class: ['sort-button', { active: sort.key === column.key }],
      type: 'button',
      onClick: () => toggleSort(column.key),
      'aria-sort': sort.key === column.key ? (sort.direction === 'asc' ? 'ascending' : 'descending') : 'none'
    }, [
      h('span', column.label),
      h('span', { class: 'sort-icon', 'aria-hidden': 'true' }, sort.key === column.key ? (sort.direction === 'asc' ? '↑' : '↓') : '↕')
    ]);

    return () => h('div', { class: 'table-wrap' }, [
      props.transactions.length === 0
        ? h('p', { class: 'empty-state' }, props.emptyText)
        : h('table', [
          h('thead', [
            h('tr', columns.map((column) => h('th', { class: column.class }, header(column))))
          ]),
          h('tbody', sortedTransactions.value.map((tx) => h('tr', { key: tx.id ?? `${tx.date}-${tx.description}` }, [
            h('td', tx.date),
            h('td', { class: 'description' }, tx.description),
            h('td', tx.bankName || '-'),
            h('td', h('span', { class: ['pill', tx.type?.toLowerCase()] }, tx.type)),
            h('td', { class: 'category-cell' }, [
              h(CategoryBadge, { category: tx.subCategory || tx.mainCategory || 'UNKNOWN' }),
              h('span', [
                tx.mainCategory || '-',
                h('small', tx.subCategory || '')
              ])
            ]),
            h('td', { class: 'amount' }, h(SensitiveAmount, {
              value: Number(tx.amount || 0),
              reveal: showAmounts.value
            }))
          ])))
        ])
    ]);
  }
});

const CategoryBadge = defineComponent({
  props: {
    category: { type: String, default: 'UNKNOWN' },
    color: { type: String, default: '' }
  },
  setup(props) {
    return () => {
      const meta = categoryMeta(props.category);
      return h('span', {
        class: 'category-badge',
        style: { '--badge-bg': props.color || meta.color },
        title: meta.label
      }, meta.short);
    };
  }
});

const EmptyState = defineComponent({
  props: {
    title: { type: String, required: true },
    text: { type: String, default: '' }
  },
  setup(props) {
    return () => h('div', { class: 'empty-state' }, [
      h('span', { class: 'empty-visual', 'aria-hidden': 'true' }, 'AY'),
      h('strong', props.title),
      props.text ? h('p', props.text) : null
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

const cashflowRows = computed(() => monthlyRows.value.slice(0, 6).map((row) => {
  const net = row.credit - row.debit;
  const max = Math.max(row.credit, row.debit, Math.abs(net), 1);
  return {
    ...row,
    net,
    netPercent: Math.max((Math.abs(net) / max) * 100, net !== 0 ? 4 : 0)
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

const selectedMonthIncomeTotal = computed(() => sumTransactions(
  incomeTransactions.value.filter((tx) => tx.date?.startsWith(selectedSummaryMonth.value))
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
  '#72c6b4',
  '#f2a477',
  '#8fb4f5',
  '#c5a3ef',
  '#f3c86f',
  '#83d39a',
  '#ee8fa2',
  '#a6c873',
  '#b7a7f1',
  '#9fb0bf'
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
    syncProfileForm();
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

async function updateProfile() {
  withMessage('');
  busy.value = true;
  try {
    const payload = {
      name: profileForm.name,
      email: profileForm.email,
      password: profileForm.password
    };
    const auth = await apiRequest('/api/auth/me', {
      method: 'PUT',
      body: JSON.stringify(payload)
    }, token.value);
    saveSession(auth);
    token.value = auth.token;
    user.value = { id: auth.userId, name: auth.name, email: auth.email };
    profileForm.password = '';
    syncProfileForm();
    showAccountPanel.value = false;
    withMessage('Hesap bilgileri güncellendi.', 'success');
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

function toggleAccountPanel() {
  if (!showAccountPanel.value) {
    syncProfileForm();
  }
  showAccountPanel.value = !showAccountPanel.value;
}

function syncProfileForm() {
  profileForm.name = user.value?.name || '';
  profileForm.email = user.value?.email || '';
  profileForm.password = '';
}

function logout() {
  clearSession();
  token.value = null;
  user.value = null;
  showAccountPanel.value = false;
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

function categoryMeta(category = 'UNKNOWN') {
  const key = String(category).toUpperCase();
  const meta = categoryMetaMap[key] || categoryMetaMap.UNKNOWN;
  return {
    ...meta,
    label: category || meta.label
  };
}

const categoryMetaMap = {
  SALARY: { short: 'GL', color: '#83d39a', label: 'Gelir' },
  EXTRA_INCOME: { short: 'EK', color: '#83d39a', label: 'Ek gelir' },
  RENT: { short: 'EV', color: '#b7a7f1', label: 'Konut' },
  APARTMENT_FEE: { short: 'EV', color: '#b7a7f1', label: 'Aidat' },
  HOME_GOODS: { short: 'ES', color: '#c5a3ef', label: 'Ev esyasi' },
  INTERNET: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  MOBILE_PHONE: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  ELECTRICITY: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  WATER: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  NATURAL_GAS: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  MARKET: { short: 'MR', color: '#72c6b4', label: 'Market' },
  RESTAURANT: { short: 'YM', color: '#f2a477', label: 'Yemek' },
  COFFEE: { short: 'KF', color: '#f3c86f', label: 'Kafe' },
  E_COMMERCE: { short: 'AL', color: '#c5a3ef', label: 'Alisveris' },
  GENERAL_SHOPPING: { short: 'AL', color: '#c5a3ef', label: 'Alisveris' },
  CLOTHING: { short: 'GY', color: '#c5a3ef', label: 'Giyim' },
  ELECTRONICS: { short: 'EL', color: '#c5a3ef', label: 'Elektronik' },
  PUBLIC_TRANSPORT: { short: 'UL', color: '#8fb4f5', label: 'Ulasim' },
  TAXI: { short: 'TX', color: '#8fb4f5', label: 'Taksi' },
  FUEL: { short: 'YT', color: '#8fb4f5', label: 'Yakit' },
  FLIGHT: { short: 'SY', color: '#8fb4f5', label: 'Seyahat' },
  TRAVEL: { short: 'SY', color: '#8fb4f5', label: 'Seyahat' },
  PHARMACY: { short: 'SG', color: '#ee8fa2', label: 'Saglik' },
  HOSPITAL: { short: 'SG', color: '#ee8fa2', label: 'Saglik' },
  SPORTS_FITNESS: { short: 'SP', color: '#a6c873', label: 'Spor' },
  ONLINE_COURSE: { short: 'EG', color: '#f3c86f', label: 'Egitim' },
  BOOK: { short: 'KT', color: '#f3c86f', label: 'Kitap' },
  EXAM_FEE: { short: 'EG', color: '#f3c86f', label: 'Sinav' },
  DIGITAL_SUBSCRIPTION: { short: 'AB', color: '#b7a7f1', label: 'Abonelik' },
  GOLD: { short: 'YA', color: '#f3c86f', label: 'Altin' },
  SILVER: { short: 'GM', color: '#9fb0bf', label: 'Gumus' },
  FOREIGN_CURRENCY: { short: 'DV', color: '#9fb0bf', label: 'Doviz' },
  STOCK_FUND: { short: 'FN', color: '#9fb0bf', label: 'Fon' },
  MONEY_SENT: { short: 'TR', color: '#9fb0bf', label: 'Transfer' },
  MONEY_RECEIVED: { short: 'TR', color: '#83d39a', label: 'Gelen transfer' },
  DEBT_PAYMENT: { short: 'OD', color: '#9fb0bf', label: 'Odeme' },
  SELF_TRANSFER: { short: 'KT', color: '#9fb0bf', label: 'Kendime transfer' },
  ATM_WITHDRAWAL: { short: 'NK', color: '#f2a477', label: 'Nakit' },
  ATM_DEPOSIT: { short: 'NK', color: '#83d39a', label: 'Nakit' },
  BANK_FEE: { short: 'BK', color: '#ee8fa2', label: 'Banka kesintisi' },
  PET_CARE: { short: 'PC', color: '#a6c873', label: 'Evcil hayvan' },
  GIFT: { short: 'HD', color: '#ee8fa2', label: 'Hediye' },
  UNKNOWN: { short: '??', color: '#9fb0bf', label: 'Bilinmeyen' },
  INCOME: { short: 'GL', color: '#83d39a', label: 'Gelir' },
  HOUSING: { short: 'EV', color: '#b7a7f1', label: 'Konut' },
  BILLS: { short: 'FT', color: '#8fb4f5', label: 'Fatura' },
  FOOD: { short: 'YM', color: '#f2a477', label: 'Yemek' },
  SHOPPING: { short: 'AL', color: '#c5a3ef', label: 'Alisveris' },
  TRANSPORTATION: { short: 'UL', color: '#8fb4f5', label: 'Ulasim' },
  HEALTH: { short: 'SG', color: '#ee8fa2', label: 'Saglik' },
  EDUCATION: { short: 'EG', color: '#f3c86f', label: 'Egitim' },
  SUBSCRIPTION: { short: 'AB', color: '#b7a7f1', label: 'Abonelik' },
  INVESTMENT: { short: 'YT', color: '#9fb0bf', label: 'Yatirim' },
  TRANSFER: { short: 'TR', color: '#9fb0bf', label: 'Transfer' },
  CASH: { short: 'NK', color: '#f2a477', label: 'Nakit' },
  BANK_FEES: { short: 'BK', color: '#ee8fa2', label: 'Banka kesintisi' },
  OTHER: { short: 'DG', color: '#9fb0bf', label: 'Diger' }
};

function sortValue(tx, key) {
  if (key === 'amount') {
    return Number(tx.amount || 0);
  }
  if (key === 'category') {
    return `${tx.mainCategory || ''} ${tx.subCategory || ''}`;
  }

  return tx[key] || '';
}

function compareValues(left, right, key) {
  if (key === 'amount') {
    return left - right;
  }
  if (key === 'date') {
    return String(left).localeCompare(String(right));
  }

  return String(left).localeCompare(String(right), 'tr', { sensitivity: 'base' });
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
