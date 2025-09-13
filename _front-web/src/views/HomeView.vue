<template>
  <main class="home-page">
    <div class="content">
      <h1>Welcome to Tools-AIO</h1>
      <h2>不要温和地走进那个良夜</h2>

      <div class="icons">
        <HomeIcon />
        <CashIcon />
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import HomeIcon from '@/components/icons/HomeIcon.vue'
import { CashOutline as CashIcon } from '@vicons/ionicons5'
</script>

<style scoped>
.home-page {
  width: 100%;
}

.content {
  text-align: center;
  padding: 1rem;
  color: var(--text-color);
}

.icons {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

/* 给所有 svg 提供一致的交互反馈 */
:deep(svg) {
  width: 40px;
  height: 40px;
  cursor: pointer;
  transition: transform 0.2s ease, filter 0.2s ease;
}
:deep(svg:hover),
:deep(svg:focus) {
  transform: translateY(-2px) scale(1.03);
  filter: drop-shadow(0 2px 6px rgba(0,0,0,.15));
}
</style>
