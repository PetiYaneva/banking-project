<script setup>
import { ref } from "vue";

const mobileOpen = ref(false);
const toggleMobile = () => (mobileOpen.value = !mobileOpen.value);
const closeMobile = () => (mobileOpen.value = false);

const features = [
  { title: "Сметки и IBAN", desc: "Автоматично IBAN генериране, начални баланси и типове сметки." },
  { title: "Преводи между IBAN-и", desc: "Вътрешни преводи и история по сметка в реално време." },
  { title: "Трансакции, Приходи/Разходи", desc: "Категории, филтри и износ към CSV/Excel/PDF." },
  { title: "Кредити и риск‑оценка", desc: "DTI, отчети за последните 6 месеца, прогнози и KPI." },
  { title: "Криптовалути", desc: "Покупка/продажба и следене на една валута — live & history графики." },
  { title: "Сигурност", desc: "JWT базирано вписване и stateless сесии." },
];

const howItWorks = [
  { n: 1, title: "Регистрирай се", desc: "Имейл + парола за достъп до демо и онбординг." },
  { n: 2, title: "Създай сметка", desc: "Получаваш IBAN и начален баланс." },
  { n: 3, title: "Първи превод", desc: "Тестов превод и отключване на пълния достъп." },
];
</script>

<template>
  <header class="sticky top-0 z-50 bg-white/90 backdrop-blur border-b">
    <div class="mx-auto max-w-6xl h-14 px-4 flex items-center justify-between">
      <RouterLink to="/" class="font-bold text-base sm:text-lg md:text-xl">BankingApp</RouterLink>

      <nav class="hidden md:flex items-center gap-6 text-sm">
        <a href="#features" class="hover:text-blue-600">Функции</a>
        <a href="#how-it-works" class="hover:text-blue-600">Как работи</a>
        <RouterLink class="hover:text-blue-600" to="/login">Вход</RouterLink>
        <RouterLink
          class="inline-flex items-center rounded-lg bg-blue-600 text-white px-3 py-2 hover:bg-blue-700"
          to="/register"
        >
          Регистрация
        </RouterLink>
      </nav>

      <button
        class="md:hidden inline-flex items-center justify-center w-9 h-9 rounded-lg border"
        @click="toggleMobile"
        aria-label="Toggle navigation"
        :aria-expanded="mobileOpen"
      >
        <span class="i" :class="{ 'rotate-90': mobileOpen }">☰</span>
      </button>
    </div>

    <transition name="fade">
      <nav v-if="mobileOpen" class="md:hidden border-t bg-white" @click.self="closeMobile">
        <div class="px-4 py-3 flex flex-col gap-3 text-sm">
          <a class="py-2" href="#features" @click="closeMobile">Функции</a>
          <a class="py-2" href="#how-it-works" @click="closeMobile">Как работи</a>
          <RouterLink class="py-2" to="/login" @click="closeMobile">Вход</RouterLink>
          <RouterLink class="py-2" to="/register" @click="closeMobile">Регистрация</RouterLink>
        </div>
      </nav>
    </transition>
  </header>

  <section class="bg-gray-50 text-center py-12 sm:py-16 md:py-24">
    <div class="mx-auto max-w-4xl px-4">
      <h3 class="font-extrabold tracking-tight mb-3 sm:mb-4 text-2xl sm:text-4xl md:text-5xl">
        Онлайн банкиране — бързо, сигурно, прозрачно.
      </h3>
      <p class="text-base sm:text-lg text-gray-600 mb-6 sm:mb-8">
        Създай сметка с IBAN, прави преводи, следи приходи/разходи, управлявай кредити и търгувай криптовалути
        с live &amp; history изглед за избрана валута.
      </p>

      <div class="flex items-center justify-center gap-3 sm:gap-4">
        <RouterLink
          to="/register"
          class="inline-block rounded-lg font-medium shadow transition px-4 py-2.5 sm:px-6 sm:py-3 bg-blue-600 text-white hover:bg-blue-700"
        >
          Започни безплатно
        </RouterLink>
      </div>
    </div>
  </section>

  <section
    class="min-h-[160px] sm:min-h-[220px] md:min-h-[280px] bg-center bg-cover md:bg-fixed"
    style="background-image:url('https://picsum.photos/1200/600?grayscale')"
    aria-hidden="true"
  />

  <section id="features" class="mx-auto max-w-6xl py-10 sm:py-12 md:py-14 px-4">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      <div
        v-for="f in features"
        :key="f.title"
        class="p-4 sm:p-6 border rounded-xl bg-white/70 backdrop-blur hover:shadow transition"
      >
        <h3 class="text-base sm:text-lg font-semibold mb-1">{{ f.title }}</h3>
        <p class="text-gray-600 text-sm">{{ f.desc }}</p>
      </div>
    </div>
  </section>

  <section id="how-it-works" class="bg-gray-100">
    <div class="mx-auto max-w-6xl py-10 sm:py-12 md:py-14 px-4">
      <h2 class="text-xl sm:text-2xl font-bold mb-6">Как работи</h2>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-6">
        <div
          v-for="s in howItWorks"
          :key="s.n"
          class="p-4 sm:p-6 border rounded-xl bg-white hover:shadow transition text-center"
        >
          <div class="text-xs sm:text-sm uppercase tracking-wide text-gray-500 mb-1.5 sm:mb-2">
            Стъпка {{ s.n }}
          </div>
          <h3 class="text-base sm:text-lg font-semibold mb-1">{{ s.title }}</h3>
          <p class="text-gray-600 text-sm">{{ s.desc }}</p>
        </div>
      </div>
      <div class="text-center mt-6">
        <RouterLink
          to="/register"
          class="inline-block rounded-lg font-medium shadow transition px-5 py-3 bg-blue-600 text-white hover:bg-blue-700"
        >
          Създай профил
        </RouterLink>
      </div>
    </div>
  </section>

  <section class="py-10 sm:py-12 md:py-14">
    <div class="mx-auto max-w-4xl px-4 text-center">
      <p class="text-gray-700">
        Създадено като част от <strong>дипломна работа</strong>. Проектът включва банкови операции,
        кредитен риск отчет и модул за криптовалути.
      </p>
    </div>
  </section>

  <footer class="bg-gray-900 text-gray-300 py-8 sm:py-10">
    <div class="mx-auto max-w-6xl px-4 text-center">
      <nav class="flex justify-center gap-4 sm:gap-6 mb-3 text-sm">
        <a href="#features" class="hover:underline">Функции</a>
        <a href="#how-it-works" class="hover:underline">Как работи</a>
        <RouterLink to="/login" class="hover:underline">Вход</RouterLink>
        <RouterLink to="/register" class="hover:underline">Регистрация</RouterLink>
      </nav>
      <p class="text-[11px] sm:text-xs">&copy; 2025 BankingApp. Всички права запазени.</p>
      <div class="mt-2 text-[11px] sm:text-xs text-gray-400">
        Документация · Политика за поверителност · Контакти
      </div>
    </div>
  </footer>
</template>

<style scoped>
.i { transition: transform .2s ease; }
.fade-enter-active, .fade-leave-active { transition: opacity .15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
