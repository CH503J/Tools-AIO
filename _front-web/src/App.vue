<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watchEffect} from 'vue'
import {darkTheme, lightTheme, NButton, NConfigProvider} from 'naive-ui'
import HomeView from './views/HomeView.vue'
import SecondView from './views/SecondView.vue'

const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
const stored = localStorage.getItem('isDark')
const isDark = ref(stored === null ? systemPrefersDark : stored === 'true')

const theme = computed(() => (isDark.value ? darkTheme : lightTheme))
const toggleTheme = () => {
  isDark.value = !isDark.value
}

watchEffect(() => {
  localStorage.setItem('isDark', isDark.value ? 'true' : 'false')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
})

let mq: MediaQueryList | null = null
const handleMqChange = (e: MediaQueryListEvent) => {
  if (localStorage.getItem('isDark') === null) {
    isDark.value = e.matches
  }
}

onMounted(() => {
  mq = window.matchMedia('(prefers-color-scheme: dark)')
  mq.addEventListener('change', handleMqChange)
})

onBeforeUnmount(() => {
  mq?.removeEventListener('change', handleMqChange)
})
</script>

<template>
  <NConfigProvider :theme="theme">
    <div id="app-root">
      <div class="theme-toggle">
        <NButton size="small" tertiary @click="toggleTheme">
          {{ isDark ? '🌙' : '☀️' }}
        </NButton>
      </div>

      <!-- 原 translateY/lockScroll 移除，改为原生滚动吸附 -->
      <div class="viewport">
        <section class="page">
          <HomeView/>
        </section>
        <section class="page">
          <SecondView/>
        </section>
      </div>
    </div>
  </NConfigProvider>
</template>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}

#app-root {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 统一主题变量 */
:root {
  --bg-color: #ffffff;
  --text-color: #3a3a3a;
}

:root[data-theme="dark"] {
  --bg-color: #3a3a3a;
  --text-color: #f5f5f7;
}

/* 原生全屏滚动 + 吸附 */
.viewport {
  height: 100vh;
  overflow-y: auto;
  scroll-snap-type: y mandatory;
  scroll-behavior: smooth;
  overscroll-behavior: contain;
  background: var(--bg-color);
  color: var(--text-color);
  -webkit-overflow-scrolling: touch;
}

@media (prefers-reduced-motion: reduce) {
  .viewport {
    scroll-behavior: auto;
  }
}

.page {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 1000;
}
</style>
