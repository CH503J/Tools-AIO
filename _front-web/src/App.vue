<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {RouterView} from 'vue-router'
import {darkTheme, lightTheme, NButton, NConfigProvider} from 'naive-ui'

// 读取本地存储（没有就默认亮色）
const stored = localStorage.getItem('isDark')
const isDark = ref(stored ? stored === 'true' : false)

// Naive UI 的主题对象
const theme = computed(() => (isDark.value ? darkTheme : lightTheme))

// 切换主题
const toggleTheme = () => {
  isDark.value = !isDark.value
  console.log('切换主题:', isDark.value ? '暗色' : '亮色')
}

// 监听并持久化
watch(isDark, (v) => {
  localStorage.setItem('isDark', v ? 'true' : 'false')
  document.documentElement.classList.toggle('dark', v) // 用 class 做全局 css 变量切换
})
</script>

<template>
  <NConfigProvider :theme="theme">
    <div id="app-root">
      <!-- 固定在右上角的主题切换按钮 -->
      <div class="theme-toggle">
        <NButton size="small" tertiary @click="toggleTheme">
          {{ isDark ? '🌙 暗色' : '☀️ 亮色' }}
        </NButton>
      </div>

      <!-- 页面路由视图 -->
      <RouterView/>
    </div>
  </NConfigProvider>
</template>

<style>
#app-root {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: background-color 0.3s ease;
}

/* 主题切换按钮固定在右上角 */
.theme-toggle {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 1000;
}

/* 提供全局变量，配合 Home.vue 使用 */
:root {
  --bg-color: #ffffff;
  --text-color: #323232;
}

:root.dark {
  --bg-color: #323232;
  --text-color: #ffffff;
}
</style>
