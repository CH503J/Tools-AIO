<!-- src/App.vue -->
<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { RouterView } from 'vue-router'
import {
  NConfigProvider,
  NButton,
  darkTheme,
  lightTheme
} from 'naive-ui'

// 读 localStorage（如果没有则默认暗色）
const stored = localStorage.getItem('isDark')
const isDark = ref(stored ? stored === 'true' : true)

// 计算出要传给 NConfigProvider 的 theme 对象
const theme = computed(() => (isDark.value ? darkTheme : lightTheme))

// 监听变化并持久化（方便刷新后保留）
watch(isDark, (v) => {
  console.log('[theme] isDark ->', v)
  localStorage.setItem('isDark', v ? 'true' : 'false')
})
</script>

<template>
  <!-- 使用 PascalCase 组件名，确保 <NConfigProvider> 是你导入的组件 -->
  <NConfigProvider :theme="theme">
    <div id="app-root">
      <!-- 固定在右上角的主题切换按钮 -->
      <div class="theme-toggle">
        <NButton size="small" @click="isDark = !isDark">
          {{ isDark ? '🌙 暗色' : '☀️ 亮色' }}
        </NButton>
      </div>

      <!-- 页面路由视图 -->
      <RouterView />
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
}

/* 主题切换按钮固定在右上角 */
.theme-toggle {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 1000;
}
</style>
