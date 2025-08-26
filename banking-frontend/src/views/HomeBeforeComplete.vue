<script setup>
import { ref, defineComponent } from "vue";
import { RouterLink } from "vue-router";

const profileCompleted = ref(false);


const steps = [
  { n: 1, title: "Завърши профила", desc: "Име, контакти, доход", route: "/complete-profile", locked: false },
  { n: 2, title: "Създай сметка", desc: "Авто‑IBAN, начален баланс", route: "/create-account", locked: true },
  { n: 3, title: "Първи превод",   desc: "Тестов превод", route: "/transfer", locked: true },
];

const latestTx = ref([
  { date: "Днес",  merchant: "Starbucks Cafe",       type: "Card",     cat: "Food",          amount: -15.00 },
  { date: "Днес",  merchant: "Oxford Street 41",     type: "Card",     cat: "Clothes",       amount: -260.40 },
  { date: "20.05", merchant: "Spotify Premium",      type: "Fee",      cat: "Entertainment", amount: -10.99 },
  { date: "20.05", merchant: "Google Inc.",          type: "Transfer", cat: "Salary",        amount: 9500.00 },
  { date: "19.05", merchant: "Super-Pharm",          type: "Blik",     cat: "Pharmacy",      amount: -98.90 },
]);

const crypto = ref([
  { sym: "BTC", name: "Bitcoin",   price: 42350, change: +2.5,  logo: "https://cryptologos.cc/logos/bitcoin-btc-logo.png" },
  { sym: "ETH", name: "Ethereum",  price: 2450,  change: -1.2,  logo: "https://cryptologos.cc/logos/ethereum-eth-logo.png" },
  { sym: "LTC", name: "Litecoin",  price: 95,    change: +0.8,  logo: "https://cryptologos.cc/logos/litecoin-ltc-logo.png" },
]);

const SidebarItem = defineComponent({
  props: { label: String, icon: String, disabled: Boolean, active: Boolean },
  template: `
    <div
      class="flex items-center justify-between px-3 py-2 rounded-lg mb-1"
      :class="[ active ? 'bg-slate-100 font-medium' : 'hover:bg-slate-50',
                disabled ? 'opacity-60 cursor-not-allowed' : '' ]"
    >
      <div class="flex items-center gap-2">
        <span class="text-base">{{ icon }}</span>
        <span class="text-sm">{{ label }}</span>
      </div>
      <span v-if="disabled" title="Заключено">🔒</span>
    </div>
  `,
});

const DemoAccountCard = defineComponent({
  props: { bank: String, balance: String },
  template: `
    <div class="bg-white border rounded-2xl p-4">
      <div class="flex items-center justify-between">
        <div>
          <div class="text-sm text-slate-500">{{ bank }}</div>
          <div class="text-xs text-slate-400">** **** ****</div>
        </div>
        <button class="text-slate-400" title="Меню" disabled>⋮</button>
      </div>
      <div class="mt-3 text-2xl font-semibold tabular-nums">{{ balance }}</div>
    </div>
  `,
});
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <!-- Topbar -->
    <header class="sticky top-0 z-40 bg-white/90 backdrop-blur border-b">
      <div class="mx-auto max-w-7xl h-14 px-4 flex items-center justify-between">
        <RouterLink to="/" class="font-bold">BankingApp</RouterLink>

        <div class="flex items-center gap-3">
          <span
            class="inline-flex items-center gap-2 text-xs px-2.5 py-1 rounded-full border"
            :class="profileCompleted ? 'text-emerald-700 border-emerald-300 bg-emerald-50' : 'text-amber-700 border-amber-300 bg-amber-50'"
          >
            <span class="w-1.5 h-1.5 rounded-full" :class="profileCompleted ? 'bg-emerald-500' : 'bg-amber-500'"></span>
            {{ profileCompleted ? 'Профилът е завършен' : 'Профилът не е завършен' }}
          </span>
        </div>
      </div>
    </header>

    <div class="mx-auto max-w-7xl px-4 py-6 grid grid-cols-12 gap-4 lg:gap-6">
      <!-- Sidebar -->
      <aside class="col-span-12 md:col-span-3 lg:col-span-2">
        <nav class="bg-white border rounded-2xl p-3">
          <SidebarItem label="Dashboard" icon="🏠" active />
          <SidebarItem label="Accounts" icon="💳" disabled />
          <SidebarItem label="Transfer" icon="🔁" disabled />
          <SidebarItem label="Transactions" icon="📄" disabled />
          <SidebarItem label="Loans" icon="📈" disabled />
          <SidebarItem label="Reports" icon="📊" disabled />
        </nav>

        <div class="mt-4 p-4 rounded-2xl border bg-emerald-50 text-emerald-800 text-sm">
          <div class="font-medium mb-1">Съвет</div>
          Започни със стъпка <strong>„Завърши профила“</strong>, за да се отключат всички секции.
        </div>
      </aside>

      <!-- Main -->
      <main class="col-span-12 md:col-span-9 lg:col-span-10 space-y-6">
        <!-- Welcome + Checklist -->
        <section class="grid grid-cols-12 gap-4">
          <div class="col-span-12 lg:col-span-8">
            <div class="bg-white border rounded-2xl p-5">
              <div class="flex items-center justify-between mb-3">
                <h2 class="text-lg font-semibold">Добре дошъл!</h2>
                <span class="text-xs text-slate-500">Demo изглед</span>
              </div>

              <div class="grid sm:grid-cols-3 gap-3">
                <div
                  v-for="s in steps"
                  :key="s.n"
                  class="border rounded-xl p-4"
                  :class="s.locked ? 'opacity-60 bg-slate-50' : 'bg-white'"
                >
                  <div class="text-xs uppercase tracking-wide text-slate-500">Стъпка {{ s.n }}</div>
                  <div class="font-medium mt-1">{{ s.title }}</div>
                  <p class="text-slate-600 text-sm mt-1">{{ s.desc }}</p>

                  <RouterLink
                    v-if="!s.locked"
                    :to="{ name: 'complete-profile' }"
                    class="inline-flex mt-3 items-center gap-2 text-sm px-3 py-1.5 rounded-md bg-blue-600 text-white hover:bg-blue-700"
                  >
                    Продължи
                  </RouterLink>


                  <button
                    v-else
                    disabled
                    class="inline-flex mt-3 items-center gap-2 text-sm px-3 py-1.5 rounded-md border"
                    title="Отключва се след предходната стъпка"
                  >
                    🔒 Заключено
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Info banner -->
          <div class="col-span-12 lg:col-span-4">
            <div class="rounded-2xl border bg-teal-700 text-white p-5 min-h-[160px]">
              <div class="text-sm opacity-80 mb-1">Информация</div>
              <div class="text-lg font-semibold">Някои секции са заключени</div>
              <p class="text-white/90 text-sm mt-2">
                Това помага да валидираме кредитния риск и да генерираме точни отчети.
              </p>
            </div>
          </div>
        </section>

        <section class="grid grid-cols-12 gap-4">
          <DemoAccountCard class="col-span-12 sm:col-span-6 lg:col-span-3" bank="Santander" balance="12 220,65 $" />
          <DemoAccountCard class="col-span-12 sm:col-span-6 lg:col-span-3" bank="CityBank" balance="25 070,65 $" />
          <DemoAccountCard class="col-span-12 sm:col-span-6 lg:col-span-3" bank="Deutsche Bank" balance="570,00 $" />
          <DemoAccountCard class="col-span-12 sm:col-span-6 lg:col-span-3" bank="Credit Agricole" balance="2 680,50 $" />
        </section>

        <section class="grid grid-cols-12 gap-4">
          <div class="col-span-12 lg:col-span-7">
            <div class="bg-white border rounded-2xl p-5">
              <div class="flex items-center justify-between mb-3">
                <h3 class="font-semibold">Последни транзакции</h3>
                <span class="text-xs text-slate-500">read‑only demo</span>
              </div>

              <div class="overflow-x-auto">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="text-left text-slate-500">
                      <th class="py-2">Дата</th>
                      <th class="py-2">Контрагент</th>
                      <th class="py-2">Тип</th>
                      <th class="py-2">Категория</th>
                      <th class="py-2 text-right">Сума</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="t in latestTx" :key="t.merchant + t.date" class="border-t">
                      <td class="py-2">{{ t.date }}</td>
                      <td class="py-2">{{ t.merchant }}</td>
                      <td class="py-2">{{ t.type }}</td>
                      <td class="py-2">{{ t.cat }}</td>
                      <td class="py-2 text-right" :class="t.amount < 0 ? 'text-rose-600' : 'text-emerald-600'">
                        {{ t.amount.toLocaleString('bg-BG', { minimumFractionDigits: 2 }) }} $
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="text-right mt-3">
                <button class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted" disabled>
                  Виж повече (заключено)
                </button>
              </div>
            </div>
          </div>

          <!-- Crypto demo (замества donut) -->
          <div class="col-span-12 lg:col-span-5">
            <div class="bg-white border rounded-2xl p-5 h-full">
              <div class="flex items-center justify-between mb-3">
                <h3 class="font-semibold">Криптовалути</h3>
                <span class="text-xs text-slate-500">demo view</span>
              </div>

              <div class="space-y-3">
                <div v-for="c in crypto" :key="c.sym" class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <img :src="c.logo" :alt="c.sym" class="w-6 h-6" />
                    <span class="font-medium">{{ c.name }} ({{ c.sym }})</span>
                  </div>
                  <div class="text-right">
                    <div class="font-semibold">
                      {{ c.price.toLocaleString('bg-BG') }} $
                    </div>
                    <div class="text-sm" :class="c.change >= 0 ? 'text-emerald-600' : 'text-rose-600'">
                      {{ c.change >= 0 ? '+' : '' }}{{ c.change }}%
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-4 text-right">
                <button class="text-sm text-slate-600 hover:text-slate-900 underline decoration-dotted" disabled>
                  Виж история (заключено)
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
</style>
