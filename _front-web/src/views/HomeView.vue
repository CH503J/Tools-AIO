<template>
  <main class="home-page">
    <div class="content">
      <h1>Welcome to Tools-AIO</h1>
      <p>这是一个最小首页占位。</p>

      <div class="icons">
        <HomeIcon />
        <CashIcon />
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import HomeIcon from "@/components/icons/HomeIcon.vue";
import { CashOutline as CashIcon } from "@vicons/ionicons5";
</script>

<style scoped>
.icons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

:deep(svg) {
  width: 40px;
  height: 40px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.home-page {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color, #fff); /* 这里先用 CSS 变量，稍后配合全局切换 */
  color: var(--text-color, #000);
}

.content {
  text-align: center;
  padding: 1rem;
}
</style>
